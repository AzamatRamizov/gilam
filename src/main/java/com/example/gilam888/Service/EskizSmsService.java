package com.example.gilam888.Service;

import com.example.gilam888.Configurations.EskizProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Eskiz.uz SMS shlyuzi bilan past darajali ishlash.
 *
 * - Token'ni xotirada saqlaydi (Eskiz token ~30 kun amal qiladi).
 * - 401 (token eskirgan) bo'lsa avtomatik qayta login qilib, bir marta qayta yuboradi.
 * - Telefon raqamni 998XXXXXXXXX formatiga keltiradi.
 * - Xatolarni yutadi (log qiladi) — SMS ketmasa ham asosiy amal (to'lov/shartnoma) buzilmaydi.
 *
 * MUHIM (Spring Boot 4 / Jackson 3):
 *   HTTP javobni to'g'ridan-to'g'ri JsonNode ga o'girish O'RNIGA String olib,
 *   keyin JsonMapper bilan parse qilamiz. Sabab: Spring Boot 4 Jackson 3 ni
 *   (tools.jackson.*) ishlatadi, eski com.fasterxml.jackson.databind.JsonNode esa
 *   "Type definition error" beradi. String orqali o'qish ikkala versiyada ham ishlaydi
 *   va Eskiz'ning xom javobini log'da ko'rish imkonini beradi.
 *
 * DIQQAT: Eskiz'da xabar MATNI (shablon) va `from` (jo'natuvchi nomi) moderatsiyadan
 * o'tgan bo'lishi shart. Test rejimidagi akkaunt faqat Eskiz bergan test matnlarini yuboradi.
 */
@Slf4j
@Service
public class EskizSmsService {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final EskizProperties props;
    private final RestClient rest;

    // xotiradagi token keshi
    private volatile String token;

    public EskizSmsService(EskizProperties props) {
        this.props = props;
        this.rest = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .build();
    }

    /**
     * SMS yuborish. Xatolarni yutadi.
     * @return true — Eskiz qabul qilgan bo'lsa, aks holda false
     */
    public boolean sendSms(String telefon, String matn) {
        if (!props.isEnabled()) {
            log.info("Eskiz o'chirilgan (eskiz.enabled=false). SMS yuborilmadi: {} -> {}", telefon, matn);
            return false;
        }
        String phone = normalizePhone(telefon);
        if (phone == null) {
            log.warn("Noto'g'ri telefon raqam, SMS yuborilmadi: {}", telefon);
            return false;
        }
        try {
            return doSend(phone, matn, false);
        } catch (Exception e) {
            // to'liq stacktrace bilan — sababni ko'rish uchun
            log.error("Eskiz SMS yuborishda xato ({})", phone, e);
            return false;
        }
    }

    private boolean doSend(String phone, String matn, boolean retriedAfterAuth) {
        String tkn = ensureToken();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("mobile_phone", phone);
        body.add("message", matn);
        body.add("from", props.getFrom());

        try {
            String javob = rest.post()
                    .uri("/message/sms/send")
                    .header("Authorization", "Bearer " + tkn)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            log.info("Eskiz SMS yuborildi -> {} | javob: {}", phone, javob);
            return true;

        } catch (RestClientResponseException e) {
            // token eskirgan bo'lishi mumkin — bir marta qayta login qilib urinamiz
            if (e.getStatusCode().value() == 401 && !retriedAfterAuth) {
                log.warn("Eskiz 401 — token yangilanmoqda va qayta urinilyapti.");
                this.token = null;
                return doSend(phone, matn, true);
            }
            log.error("Eskiz javob xatosi ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        }
    }

    /** Token bor bo'lsa qaytaradi, bo'lmasa login qilib oladi. */
    private String ensureToken() {
        String local = this.token;
        if (local != null && !local.isBlank()) {
            return local;
        }
        synchronized (this) {
            if (this.token == null || this.token.isBlank()) {
                this.token = login();
            }
            return this.token;
        }
    }

    /** Eskiz'ga login qilib, JWT token oladi. */
    private String login() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("email", props.getEmail());
        body.add("password", props.getPassword());

        String javob;
        try {
            javob = rest.post()
                    .uri("/auth/login")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            // 401 bu yerda = email/parol xato
            throw new IllegalStateException(
                    "Eskiz login xatosi (" + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        }

        log.info("Eskiz login javobi: {}", javob);

        String tkn = tokenniAjrat(javob);
        if (tkn == null || tkn.isBlank()) {
            throw new IllegalStateException("Eskiz token olinmadi. Javob: " + javob);
        }
        log.info("Eskiz token olindi.");
        return tkn;
    }

    /** {"data":{"token":"..."}} ichidan tokenni oladi. */
    private String tokenniAjrat(String javob) {
        if (javob == null || javob.isBlank()) return null;
        try {
            JsonNode root = MAPPER.readTree(javob);
            JsonNode data = root.get("data");
            if (data != null && data.get("token") != null) {
                return data.get("token").asString();
            }
            // ba'zi javoblarda token yuqori darajada bo'lishi mumkin
            if (root.get("token") != null) {
                return root.get("token").asString();
            }
            return null;
        } catch (Exception e) {
            log.error("Eskiz login javobini parse qilib bo'lmadi: {}", javob, e);
            return null;
        }
    }

    /**
     * Telefon raqamni 998XXXXXXXXX formatiga keltiradi.
     * Qabul qiladi: +998901234567, 998901234567, 901234567, "90 123 45 67" va h.k.
     */
    private String normalizePhone(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() == 9) {
            digits = "998" + digits;
        } else if (!(digits.length() == 12 && digits.startsWith("998"))) {
            return null;
        }
        return digits;
    }
}