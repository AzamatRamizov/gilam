package com.example.gilam888.Service;

import com.example.gilam888.Configurations.ApiResponse;
import com.example.gilam888.Dto.KatmSorovDto;
import com.example.gilam888.Dto.KatmSorovKirimDto;
import com.example.gilam888.Entity.KatmSorov;
import com.example.gilam888.Entity.Magazin;
import com.example.gilam888.Repository.KatmSorovRepository;
import com.example.gilam888.Repository.MagazinRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ombor (baza) dasturidan kelgan KATM so'rovlari.
 *
 * OQIM:
 *   1. Omborda sotuvchi mahsulotni "KATMga o'tkazish" bilan chiqaradi
 *   2. So'rov POST /api/katm/sorov orqali shu yerga tushadi (holat = YANGI)
 *   3. Operator "KATM so'rovlari" oynasida so'rovni bosadi -> yangi shartnoma
 *      formasi to'ldirilgan holda ochiladi
 *   4. Shartnoma saqlangach so'rov BAJARILDI bo'ladi va omborga javob ketadi
 *      (u yerda sotuv yakunlanadi). Rad etilsa — ombordagi qoldiq tiklanadi.
 */
@Service
public class KatmSorovService {

    private static final Logger log = LoggerFactory.getLogger(KatmSorovService.class);

    private final KatmSorovRepository katmSorovRepository;
    private final MagazinRepository magazinRepository;
    private final AmalService amalService;

    private final RestClient restClient = RestClient.create();

    @Value("${baza.url:http://localhost:8080}")
    private String bazaUrl;

    @Value("${katm.token:}")
    private String token;

    public KatmSorovService(KatmSorovRepository katmSorovRepository,
                            MagazinRepository magazinRepository,
                            AmalService amalService) {
        this.katmSorovRepository = katmSorovRepository;
        this.magazinRepository = magazinRepository;
        this.amalService = amalService;
    }

    // ================= QABUL QILISH =================

    /** Ombordan kelgan so'rovni saqlaydi. @return saqlangan so'rov id */
    @Transactional
    public long qabulQil(KatmSorovKirimDto dto) {
        // Bir xil sotuv ikki marta yuborilsa — eskisini yangilaymiz
        KatmSorov s = dto.sotuvId() == null ? new KatmSorov()
                : katmSorovRepository.findBySotuvId(dto.sotuvId()).orElseGet(KatmSorov::new);

        s.setSotuvId(dto.sotuvId());
        s.setMijozIsmi(dto.mijozIsmi());
        s.setMijozTel(dto.mijozTel());
        s.setMahsulotNomi(dto.mahsulotNomi());
        s.setMahsulotKod(dto.mahsulotKod());
        s.setMiqdor(dto.miqdor());
        s.setBirlik(dto.birlik());
        s.setBoyi(dto.boyi());
        s.setEni(dto.eni());
        s.setKv(dto.kv());
        s.setTannarx(dto.tannarx());
        s.setSotuvNarxi(dto.sotuvNarxi());
        s.setBirlikNarxi(dto.birlikNarxi());
        s.setOldindanTulov(dto.oldindanTulov());
        s.setMuddat(dto.muddat());
        s.setDokonNomi(dto.dokonNomi());
        s.setIzoh(dto.izoh());
        s.setSotuvchi(dto.sotuvchi());
        s.setSotuvVaqti(dto.vaqt());
        s.setKelganVaqt(LocalDateTime.now());
        if (s.getHolat() == null || s.getId() == 0) {
            s.setHolat(KatmSorov.YANGI);
        }

        katmSorovRepository.save(s);
        log.info("KATM so'rovi qabul qilindi: sotuv #{} / {}", dto.sotuvId(), dto.mahsulotNomi());
        return s.getId();
    }

    // ================= RO'YXAT =================

    @Transactional(readOnly = true)
    public List<KatmSorovDto> royxat(String holat) {
        List<KatmSorov> list = (holat == null || holat.isBlank())
                ? katmSorovRepository.findAllByOrderByIdDesc()
                : katmSorovRepository.findByHolatOrderByIdDesc(holat);
        return list.stream().map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public KatmSorovDto bitta(Long id) {
        return katmSorovRepository.findById(id).map(this::dto).orElse(null);
    }

    /** Bir nechta so'rovni birga olish — 2 ta mahsulotni 1 shartnomaga qo'shish uchun */
    @Transactional(readOnly = true)
    public List<KatmSorovDto> bittalar(List<Long> idlar) {
        if (idlar == null || idlar.isEmpty()) return List.of();
        return katmSorovRepository.findAllById(idlar).stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .map(this::dto)
                .toList();
    }

    /** Sidebar/bildirishnoma uchun — yangi so'rovlar soni */
    @Transactional(readOnly = true)
    public long yangilarSoni() {
        return katmSorovRepository.countByHolat(KatmSorov.YANGI);
    }

    // ================= JAVOB =================

    /** Shartnoma tuzildi — so'rov yopiladi va omborga xabar ketadi */
    @Transactional
    public ApiResponse bajarildi(Long id, Long shartnomaId, String kim) {
        KatmSorov s = katmSorovRepository.findById(id).orElse(null);
        if (s == null) return new ApiResponse("So'rov topilmadi", false);
        if (KatmSorov.BAJARILDI.equals(s.getHolat())) {
            return new ApiResponse("Bu so'rov allaqachon bajarilgan", false);
        }

        s.setHolat(KatmSorov.BAJARILDI);
        s.setShartnomaId(shartnomaId);
        s.setJavobBergan(kim);
        s.setJavobVaqti(LocalDateTime.now());
        katmSorovRepository.save(s);

        amalService.log("KATM", "KATM so'rovi bo'yicha shartnoma tuzildi: " +
                s.getMahsulotNomi() + (s.getMijozIsmi() == null ? "" : " / " + s.getMijozIsmi()));

        bazagaJavob(s, true, "Shartnoma tuzildi", kim);
        return new ApiResponse("So'rov yopildi" +
                (Boolean.TRUE.equals(s.getBazagaYuborildi()) ? " va omborga xabar berildi"
                        : ", lekin omborga xabar bormadi: " + s.getBazaXato()), true);
    }

    /** Rad etildi — ombordagi mahsulot qoldig'i tiklanadi */
    @Transactional
    public ApiResponse radEtildi(Long id, String izoh, String kim) {
        KatmSorov s = katmSorovRepository.findById(id).orElse(null);
        if (s == null) return new ApiResponse("So'rov topilmadi", false);
        if (!KatmSorov.YANGI.equals(s.getHolat())) {
            return new ApiResponse("Bu so'rov allaqachon yopilgan", false);
        }

        s.setHolat(KatmSorov.RAD_ETILDI);
        s.setJavobIzohi(izoh);
        s.setJavobBergan(kim);
        s.setJavobVaqti(LocalDateTime.now());
        katmSorovRepository.save(s);

        amalService.log("KATM", "KATM so'rovi rad etildi: " + s.getMahsulotNomi() +
                (izoh == null || izoh.isBlank() ? "" : " (" + izoh + ")"));

        bazagaJavob(s, false, izoh, kim);
        return new ApiResponse("So'rov rad etildi" +
                (Boolean.TRUE.equals(s.getBazagaYuborildi()) ? " va omborga xabar berildi"
                        : ", lekin omborga xabar bormadi: " + s.getBazaXato()), true);
    }

    /** Omborga xabar bormay qolgan bo'lsa — qayta urinish */
    @Transactional
    public ApiResponse qaytaXabar(Long id) {
        KatmSorov s = katmSorovRepository.findById(id).orElse(null);
        if (s == null) return new ApiResponse("So'rov topilmadi", false);
        if (KatmSorov.YANGI.equals(s.getHolat())) {
            return new ApiResponse("Bu so'rov hali yopilmagan", false);
        }
        if (Boolean.TRUE.equals(s.getBazagaYuborildi())) {
            return new ApiResponse("Omborga allaqachon xabar berilgan", false);
        }

        bazagaJavob(s, KatmSorov.BAJARILDI.equals(s.getHolat()), s.getJavobIzohi(), s.getJavobBergan());
        return Boolean.TRUE.equals(s.getBazagaYuborildi())
                ? new ApiResponse("Omborga xabar berildi", true)
                : new ApiResponse("Yuborilmadi: " + s.getBazaXato(), false);
    }

    /**
     * Ombor (baza) dasturiga javob:
     *   tasdiq = true  -> u yerda sotuv yakunlanadi
     *   tasdiq = false -> mahsulot qoldig'i tiklanadi
     */
    private void bazagaJavob(KatmSorov s, boolean tasdiq, String izoh, String kim) {
        if (s.getSotuvId() == null) {
            s.setBazagaYuborildi(false);
            s.setBazaXato("Sotuv id yo'q");
            katmSorovRepository.save(s);
            return;
        }
        try {
            Map<String, Object> tana = new HashMap<>();
            tana.put("sotuvId", s.getSotuvId());
            tana.put("tasdiq", tasdiq);
            tana.put("izoh", izoh);
            tana.put("shartnomaId", s.getShartnomaId());
            tana.put("kim", kim);

            restClient.post()
                    .uri(bazaUrl.replaceAll("/+$", "") + "/api/katm/javob")
                    .header("X-Katm-Token", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(tana)
                    .retrieve()
                    .toBodilessEntity();

            s.setBazagaYuborildi(true);
            s.setBazaXato(null);
        } catch (Exception e) {
            String xato = e.getMessage() == null ? e.toString() : e.getMessage();
            if (xato.length() > 280) xato = xato.substring(0, 280);
            s.setBazagaYuborildi(false);
            s.setBazaXato(xato);
            log.warn("Omborga javob yuborilmadi (sotuv #{}): {}", s.getSotuvId(), xato);
        }
        katmSorovRepository.save(s);
    }

    // ================= YORDAMCHI =================

    private KatmSorovDto dto(KatmSorov s) {
        Long dokonId = 0L;
        if (s.getDokonNomi() != null && !s.getDokonNomi().isBlank()) {
            Magazin m = magazinRepository.findByNomi(s.getDokonNomi().trim()).orElse(null);
            if (m == null) {
                m = magazinRepository.findAll().stream()
                        .filter(x -> x.getNomi() != null
                                && x.getNomi().trim().equalsIgnoreCase(s.getDokonNomi().trim()))
                        .findFirst().orElse(null);
            }
            if (m != null) dokonId = m.getId();
        }

        return new KatmSorovDto(
                s.getId(), s.getSotuvId(),
                s.getMijozIsmi(), s.getMijozTel(),
                s.getMahsulotNomi(), s.getMahsulotKod(),
                s.getMiqdor(), s.getBirlik(), s.getBoyi(), s.getEni(), s.getKv(),
                aboutMatn(s),
                s.getTannarx(), s.getSotuvNarxi(), s.getOldindanTulov(), s.getMuddat(),
                s.getDokonNomi(), dokonId,
                s.getIzoh(), s.getSotuvchi(), s.getSotuvVaqti(), s.getKelganVaqt(),
                s.getHolat(), s.getShartnomaId(),
                s.getJavobIzohi(), s.getJavobBergan(), s.getJavobVaqti());
    }

    /** "Mahsulot haqida" maydoniga tayyor matn: nomi, kodi, o'lchami */
    private String aboutMatn(KatmSorov s) {
        StringBuilder sb = new StringBuilder();
        if (s.getMahsulotNomi() != null) sb.append(s.getMahsulotNomi());
        if (s.getMahsulotKod() != null && !s.getMahsulotKod().isBlank()) {
            sb.append(" (").append(s.getMahsulotKod()).append(")");
        }
        if (s.getEni() != null && s.getBoyi() != null) {
            sb.append(" — ").append(son(s.getEni())).append(" × ").append(son(s.getBoyi())).append(" m");
        }
        if (s.getMiqdor() != null) {
            sb.append(", ").append(son(s.getMiqdor())).append(" ")
                    .append(s.getBirlik() == null ? "dona" : s.getBirlik());
        }
        return sb.toString().trim();
    }

    private String son(Double d) {
        if (d == null) return "0";
        return d == Math.rint(d) ? String.valueOf((long) (double) d)
                : String.valueOf(Math.round(d * 100.0) / 100.0);
    }
}
