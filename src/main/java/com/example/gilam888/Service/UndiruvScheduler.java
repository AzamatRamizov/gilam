package com.example.gilam888.Service;

import com.example.gilam888.Entity.Jadval;
import com.example.gilam888.Entity.Shartnoma;
import com.example.gilam888.Repository.ShartnomaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HAR KUNI 1 MARTA to'lovi kechikkan shartnomalarni avtomatik undiruvga o'tkazadi.
 *
 * Mantiq:
 *   1. Sozlamadan "undiruvga chiqish kuni" (N) o'qiladi  → /admin/sozlama sahifasi
 *   2. Sanasi bugundan N kun oldin bo'lgan va hali to'lanmagan jadvallar topiladi
 *   3. Ularning shartnomasi status = "ochiq" bo'lsa → status = "undiruv"
 *
 * Undiruvga faqat "ochiq" shartnomalar o'tadi. "yopilgan" va allaqachon
 * "undiruv" dagilar tegilmaydi. Qo'lda undiruvdan chiqarilgan shartnoma
 * qarzi to'lanmasa ertasi kuni yana tushib qoladi — bu ataylab shunday,
 * chunki qarz haqiqatdan ham kechikkan bo'lib qolyapti.
 *
 * Sozlamalar (application.properties):
 *   undiruv.scheduler.cron=0 30 6 * * *      (default: har kuni 06:30)
 */
@Slf4j
@Component
public class UndiruvScheduler {

    private final ShartnomaRepository shartnomaRepository;
    private final SozlamaService sozlamaService;
    private final AmalService amalService;

    public UndiruvScheduler(ShartnomaRepository shartnomaRepository,
                            SozlamaService sozlamaService,
                            AmalService amalService) {
        this.shartnomaRepository = shartnomaRepository;
        this.sozlamaService = sozlamaService;
        this.amalService = amalService;
    }

    /** Har kuni 06:30 (Asia/Tashkent). SMS eslatmadan oldin ishlaydi. */
    @Scheduled(cron = "${undiruv.scheduler.cron:0 30 6 * * *}", zone = "Asia/Tashkent")
    public void kunlikTekshiruv() {
        if (!sozlamaService.undiruvAvtoYoqilgan()) {
            log.info("Avtomatik undiruv o'chirilgan (sozlama: undiruv.avto=false).");
            return;
        }
        Map<String, Object> natija = otkaz(false);
        log.info("Avtomatik undiruv yakunlandi: {}", natija);
    }

    /**
     * Kechikkan shartnomalarni undiruvga o'tkazadi.
     *
     * @param faqatKorish true bo'lsa hech narsa saqlanmaydi — faqat ro'yxat qaytadi
     *                    (sozlama sahifasidagi "Tekshirib ko'rish" tugmasi uchun)
     */
    @Transactional
    public Map<String, Object> otkaz(boolean faqatKorish) {
        int kun = sozlamaService.undiruvKun();
        // Bugundan N kun oldingi kun boshi. Sanasi shundan OLDIN bo'lgan
        // to'lanmagan jadval = N kundan ko'p kechikkan degani.
        LocalDateTime chegara = LocalDate.now().minusDays(kun).atStartOfDay();

        List<Long> idlar = shartnomaRepository.findAvtoUndiruvNomzodlari(chegara);
        log.info("Avtomatik undiruv: chegara={} ({} kun), {} ta nomzod topildi.",
                chegara.toLocalDate(), kun, idlar.size());

        List<Map<String, Object>> royxat = new ArrayList<>();
        int otkazildi = 0;

        for (Long id : idlar) {
            try {
                Shartnoma s = shartnomaRepository.findById(id).orElse(null);
                if (s == null) continue;
                // Ikkinchi tekshiruv: so'rov va saqlash orasida status o'zgargan bo'lishi mumkin
                if (!"ochiq".equalsIgnoreCase(s.getStatus())) continue;

                // Eng eski to'lanmagan jadvalni topamiz — kechikkan kunlarni hisoblash uchun
                LocalDateTime engEski = null;
                long qarz = 0;
                if (s.getJadvalList() != null) {
                    for (Jadval j : s.getJadvalList()) {
                        if (!"tulanmagan".equalsIgnoreCase(j.getHolat())) continue;
                        if (j.getSana() == null) continue;
                        qarz += Math.max(0, j.getSumma() - j.getTulangan());
                        if (engEski == null || j.getSana().isBefore(engEski)) engEski = j.getSana();
                    }
                }
                if (engEski == null) continue;

                long kechikkan = ChronoUnit.DAYS.between(engEski.toLocalDate(), LocalDate.now());
                String sabab = "Avtomatik: to'lov " + kechikkan + " kun kechikdi (chegara: " + kun + " kun)";

                if (!faqatKorish) {
                    s.setStatus("undiruv");
                    s.setUndiruvSababi(sabab);
                    s.setUndiruvVaqti(LocalDateTime.now());
                    shartnomaRepository.save(s);

                    amalService.log("UNDIRUV",
                            "Shartnoma #" + s.getId() + " AVTOMATIK undiruvga o'tkazildi. " + sabab,
                            s.getMijoz() != null ? s.getMijoz().getId() : null, s.getId(), null);
                    otkazildi++;
                }

                Map<String, Object> qator = new LinkedHashMap<>();
                qator.put("shartnomaId", s.getId());
                qator.put("fish", mijozIsmi(s));
                qator.put("engEskiTulov", engEski.toLocalDate().toString());
                qator.put("kechikkanKun", kechikkan);
                qator.put("qarz", qarz);
                royxat.add(qator);

            } catch (Exception e) {
                // bitta shartnomadagi xato butun jarayonni to'xtatmasin
                log.error("Avtomatik undiruvda xato (shartnoma id={}): {}", id, e.getMessage());
            }
        }

        Map<String, Object> natija = new LinkedHashMap<>();
        natija.put("faqatKorish", faqatKorish);
        natija.put("kun", kun);
        natija.put("chegaraSana", chegara.toLocalDate().toString());
        natija.put("topildi", royxat.size());
        natija.put("otkazildi", otkazildi);
        natija.put("royxat", royxat);
        return natija;
    }

    private String mijozIsmi(Shartnoma s) {
        if (s.getMijoz() == null) return "—";
        String f = s.getMijoz().getFamiliya() != null ? s.getMijoz().getFamiliya() : "";
        String i = s.getMijoz().getIsm() != null ? s.getMijoz().getIsm() : "";
        return (f + " " + i).trim();
    }
}
