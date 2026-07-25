package com.example.gilam888.Service;

import com.example.gilam888.Entity.SmsBalans;
import com.example.gilam888.Entity.SmsTarix;
import com.example.gilam888.Repository.SmsBalansRepository;
import com.example.gilam888.Repository.SmsTarixRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Eskiz SMS xarajatlarini nazorat qilish:
 *
 *  - Hisobga tashlangan pul (balans) tizimda yuritiladi.
 *  - Har bir yuborilgan SMS SmsTarix'ga yoziladi va narxi balansdan ayriladi.
 *  - Narx qabul qiluvchi raqam prefiksidan (operator) aniqlanadi.
 *  - Uzun SMS bir nechta qismga bo'linadi — Eskiz har qism uchun alohida pul oladi,
 *    shuning uchun qismlar soni ham hisoblanadi.
 *
 * Narxlar — Eskiz'ning SERVIS turdagi tariflari (bizning shablonlar xizmat SMSlari):
 *   Mobiuz 110, Beeline 115, Ucell 160, Humans 95, Uzmobile 145, Perfectum 95 so'm.
 * Tarif o'zgarsa — quyidagi NARXLAR jadvalini yangilash kifoya.
 */
@Slf4j
@Service
public class SmsHisobService {

    // Sort maydonlarining oq ro'yxati (inyeksiya/xato oldini olish uchun)
    private static final Set<String> RUXSAT_SORT = Set.of("vaqt", "operator", "turi", "narx", "holat", "id");

    // Operator narxi topilmagan prefiks uchun (masalan OQ +998 20 —
    // rasmda narxi ko'rsatilmagan) eng yuqori tarif olinadi, xavfsiz tomonga.
    private static final long DEFAULT_NARX = 160;
    private static final String DEFAULT_OPERATOR = "Noma'lum";

    // prefiks (998 dan keyingi 2 raqam) -> [operator nomi, 1 qism narxi]
    private static final Map<String, Object[]> NARXLAR = Map.ofEntries(
            Map.entry("90", new Object[]{"Beeline", 115L}),
            Map.entry("91", new Object[]{"Beeline", 115L}),
            Map.entry("92", new Object[]{"Beeline", 115L}),
            Map.entry("99", new Object[]{"Uzmobile", 145L}),
            Map.entry("77", new Object[]{"Uzmobile", 145L}),
            Map.entry("70", new Object[]{"Uzmobile", 145L}),
            Map.entry("95", new Object[]{"Uzmobile", 145L}),
            Map.entry("97", new Object[]{"Mobiuz", 110L}),
            Map.entry("88", new Object[]{"Mobiuz", 110L}),
            Map.entry("87", new Object[]{"Mobiuz", 110L}),
            Map.entry("93", new Object[]{"Ucell", 160L}),
            Map.entry("94", new Object[]{"Ucell", 160L}),
            Map.entry("50", new Object[]{"Ucell", 160L}),
            Map.entry("33", new Object[]{"Humans", 95L}),
            Map.entry("98", new Object[]{"Perfectum", 95L}),
            Map.entry("80", new Object[]{"Perfectum", 95L}),
            Map.entry("20", new Object[]{"OQ", DEFAULT_NARX})
    );

    // GSM-7 asosiy alifbosi — matnda faqat shu belgilar bo'lsa,
    // bitta SMS 160 belgigacha sig'adi; boshqa belgi (kirillcha, emoji)
    // bo'lsa Unicode rejimga o'tadi va 70 belgigacha sig'adi.
    private static final String GSM7 =
            "@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?"
                    + "¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà"
                    + "^{}\\[~]|€";

    private final SmsTarixRepository smsTarixRepository;
    private final SmsBalansRepository smsBalansRepository;
    private final AmalService amalService;

    public SmsHisobService(SmsTarixRepository smsTarixRepository,
                           SmsBalansRepository smsBalansRepository,
                           AmalService amalService) {
        this.smsTarixRepository = smsTarixRepository;
        this.smsBalansRepository = smsBalansRepository;
        this.amalService = amalService;
    }

    // ─────────────────────────── YOZISH ───────────────────────────

    /**
     * Yuborilgan (yoki yuborishga urinilgan) SMSni tarixga yozadi.
     * Muvaffaqiyatli bo'lsa narxi balansdan ayriladi.
     * MUHIM: bu yerdagi xato asosiy amalni (to'lov/shartnoma) buzmasligi kerak —
     * barcha istisnolar yutiladi.
     *
     * @param telefon     qabul qiluvchi raqam (istalgan formatda)
     * @param matn        SMS matni
     * @param turi        YANGI_SHARTNOMA / TOLOV / ESLATMA
     * @param yuborildi   Eskiz qabul qildimi
     * @param mijozId     bog'liq mijoz (bo'lishi shart emas)
     * @param shartnomaId bog'liq shartnoma (bo'lishi shart emas)
     * @param mijozFish   mijoz ismi (snapshot)
     */
    public void smsYozish(String telefon, String matn, String turi, boolean yuborildi,
                          Long mijozId, Long shartnomaId, String mijozFish) {
        try {
            String prefiks = prefiksAjrat(telefon);
            Object[] tarif = prefiks != null ? NARXLAR.get(prefiks) : null;
            String operator = tarif != null ? (String) tarif[0] : DEFAULT_OPERATOR;
            long birQismNarx = tarif != null ? (Long) tarif[1] : DEFAULT_NARX;
            int qismlar = qismlarSoni(matn);
            long jamiNarx = birQismNarx * qismlar;

            SmsTarix sms = new SmsTarix();
            sms.setTelefon(telefon);
            sms.setOperator(operator);
            sms.setMatn(matn);
            sms.setQismlar(qismlar);
            sms.setBirQismNarx(birQismNarx);
            sms.setNarx(yuborildi ? jamiNarx : 0L);
            sms.setTuri(turi);
            sms.setHolat(yuborildi ? "YUBORILDI" : "XATO");
            sms.setMijozId(mijozId);
            sms.setShartnomaId(shartnomaId);
            sms.setMijozFish(mijozFish);
            sms.setVaqt(LocalDateTime.now());
            smsTarixRepository.save(sms);

            // Faqat haqiqatda ketgan SMS uchun balansdan ayiramiz
            if (yuborildi) {
                SmsBalans balans = balansOlish();
                smsBalansRepository.ayirish(balans.getId(), jamiNarx);
            }
        } catch (Exception e) {
            log.error("SMS tarixga yozishda xato ({}): {}", telefon, e.getMessage());
        }
    }

    // ─────────────────────────── BALANS ───────────────────────────

    /** Balans qatorini oladi, yo'q bo'lsa 0 bilan yaratadi (birinchi ishga tushishda). */
    public SmsBalans balansOlish() {
        return smsBalansRepository.findAll().stream().findFirst()
                .orElseGet(() -> smsBalansRepository.save(new SmsBalans(0L, 0L)));
    }

    /**
     * Balansni yangilash — ikki rejim:
     *  - "qoshish"   : hisob to'ldirildi, mavjud balansga summa qo'shiladi
     *  - "belgilash" : balans Eskiz kabinetidagi aniq qiymatga tenglashtiriladi
     *                  (tuzatish uchun — masalan boshida yoki farq yig'ilib qolganda)
     */
    public Map<String, Object> balansYangilash(long summa, String rejim) {
        SmsBalans balans = balansOlish();
        if ("belgilash".equals(rejim)) {
            long eski = balans.getBalans() != null ? balans.getBalans() : 0L;
            balans.setBalans(summa);
            smsBalansRepository.save(balans);
            amalService.log("SMS_BALANS",
                    "Eskiz balansi qo'lda to'g'irlandi: " + eski + " → " + summa + " so'm",
                    null, null, summa);
        } else {
            smsBalansRepository.toldirish(balans.getId(), summa);
            amalService.log("SMS_BALANS",
                    "Eskiz hisobi to'ldirildi: +" + summa + " so'm",
                    null, null, summa);
        }
        Map<String, Object> javob = new LinkedHashMap<>();
        javob.put("balans", balansOlish().getBalans());
        return javob;
    }

    // ─────────────────────────── O'QISH ───────────────────────────

    /** "SMS nazorati" sahifasi uchun stat kartalar: balans + davrlar bo'yicha sarf. */
    public Map<String, Object> getSmsStat() {
        LocalDateTime hozir = LocalDateTime.now();
        LocalDateTime kunBoshi = LocalDate.now().atStartOfDay();
        LocalDateTime oyBoshi = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime eng = LocalDateTime.of(2000, 1, 1, 0, 0);

        SmsBalans balans = balansOlish();

        Map<String, Object> javob = new LinkedHashMap<>();
        javob.put("balans", balans.getBalans());
        javob.put("jamiToldirilgan", balans.getJamiToldirilgan());

        javob.put("bugunSoni", smsTarixRepository.yuborilganSoni(kunBoshi, hozir));
        javob.put("bugunSarf", smsTarixRepository.sarflanganSumma(kunBoshi, hozir));
        javob.put("oySoni", smsTarixRepository.yuborilganSoni(oyBoshi, hozir));
        javob.put("oySarf", smsTarixRepository.sarflanganSumma(oyBoshi, hozir));
        javob.put("jamiSoni", smsTarixRepository.yuborilganSoni(eng, hozir));
        javob.put("jamiSarf", smsTarixRepository.sarflanganSumma(eng, hozir));
        javob.put("xatoSoni", smsTarixRepository.countByHolat("XATO"));

        // Balans tugab qolayotgani haqida ogohlantirish chegarasi:
        // o'rtacha eng qimmat SMS (160 so'm) bilan ~60 ta SMS'ga yetmasa
        javob.put("kamQoldi", balans.getBalans() != null && balans.getBalans() < 10_000);
        return javob;
    }

    /** Yuborilgan SMSlar ro'yxati — filtrlangan, tartiblangan, sahifalangan. */
    public Map<String, Object> getSmsTarix(String turi, String holat, LocalDate sana,
                                           LocalDate boshi, LocalDate oxiri,
                                           int page, int size, String sort, String dir) {
        LocalDateTime dan;
        LocalDateTime gacha;
        if (sana != null) {
            dan = sana.atStartOfDay();
            gacha = sana.atTime(23, 59, 59);
        } else {
            dan = boshi != null ? boshi.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
            gacha = oxiri != null ? oxiri.atTime(23, 59, 59) : LocalDateTime.of(2100, 1, 1, 0, 0);
        }

        String sortMaydon = RUXSAT_SORT.contains(sort) ? sort : "vaqt";
        Sort.Direction yonalish = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort tartib = Sort.by(yonalish, sortMaydon).and(Sort.by(Sort.Direction.DESC, "id"));

        if (turi != null && turi.isBlank()) turi = null;
        if (holat != null && holat.isBlank()) holat = null;

        Page<SmsTarix> natija = smsTarixRepository.filter(
                turi, holat, dan, gacha,
                PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 200), tartib));

        Map<String, Object> javob = new LinkedHashMap<>();
        javob.put("content", natija.getContent());
        javob.put("page", natija.getNumber());
        javob.put("totalPages", natija.getTotalPages());
        javob.put("totalElements", natija.getTotalElements());
        return javob;
    }

    // ─────────────────────────── YORDAMCHI ───────────────────────────

    /** 998XXXXXXXXX yoki 9 xonali raqamdan operator prefiksini (2 raqam) ajratadi. */
    private String prefiksAjrat(String telefon) {
        if (telefon == null) return null;
        String digits = telefon.replaceAll("[^0-9]", "");
        if (digits.length() == 12 && digits.startsWith("998")) {
            return digits.substring(3, 5);
        }
        if (digits.length() == 9) {
            return digits.substring(0, 2);
        }
        return null;
    }

    /**
     * SMS necha qismga bo'linishini hisoblaydi.
     * GSM-7 (oddiy lotin) matn: 1 qism ≤160 belgi, ko'p qismli — har biri 153 belgi.
     * Unicode (kirillcha va h.k.): 1 qism ≤70 belgi, ko'p qismli — har biri 67 belgi.
     */
    static int qismlarSoni(String matn) {
        if (matn == null || matn.isEmpty()) return 1;
        boolean gsm = matn.chars().allMatch(c -> GSM7.indexOf(c) >= 0);
        int len = matn.length();
        if (gsm) {
            return len <= 160 ? 1 : (int) Math.ceil(len / 153.0);
        }
        return len <= 70 ? 1 : (int) Math.ceil(len / 67.0);
    }
}
