package com.example.gilam888.Service;

import com.example.gilam888.Entity.Jadval;
import com.example.gilam888.Entity.Shartnoma;
import com.example.gilam888.Repository.JadvalRepository;
import com.example.gilam888.Repository.ShartnomaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * To'lov kunidan 1 KUN OLDIN mijozga SMS eslatma yuboradi.
 *
 * Har kuni belgilangan vaqtda (default: 10:00) ishlaydi:
 *   1. ERTAGA sanasi tushadigan va hali "tulanmagan" jadval qatorlarini oladi
 *   2. Har biri uchun shartnomani topadi
 *   3. Mijozga 3-shablon bo'yicha SMS yuboradi
 *
 * Sozlamalar (application.properties):
 *   sms.eslatma.enabled=true
 *   sms.eslatma.cron=0 0 10 * * *
 *
 * Eslatma: bir mijozda ertaga bir nechta shartnoma to'lov kuni bo'lsa,
 * har bir shartnoma uchun alohida SMS ketadi (matnda shartnoma raqami bor).
 */
@Slf4j
@Component
public class SmsEslatmaScheduler {

    private final JadvalRepository jadvalRepository;
    private final ShartnomaRepository shartnomaRepository;
    private final SmsXabarService smsXabarService;

    @Value("${sms.eslatma.enabled:true}")
    private boolean enabled;

    public SmsEslatmaScheduler(JadvalRepository jadvalRepository,
                               ShartnomaRepository shartnomaRepository,
                               SmsXabarService smsXabarService) {
        this.jadvalRepository = jadvalRepository;
        this.shartnomaRepository = shartnomaRepository;
        this.smsXabarService = smsXabarService;
    }

    /** Har kuni 10:00 da (Toshkent vaqti bilan, server zonasiga qarab). */
    @Scheduled(cron = "${sms.eslatma.cron:0 0 10 * * *}", zone = "Asia/Tashkent")
    public void ertangiTulovlarUchunEslatma() {
        if (!enabled) {
            log.info("SMS eslatma o'chirilgan (sms.eslatma.enabled=false).");
            return;
        }
        yubor(LocalDate.now().plusDays(1));
    }

    /**
     * Berilgan sanadagi to'lovlar uchun eslatma yuboradi.
     * Alohida metod — qo'lda (masalan admin tugmasidan) ham chaqirish mumkin.
     *
     * @return yuborilgan SMS soni
     */
    @Transactional(readOnly = true)
    public int yubor(LocalDate sana) {
        LocalDateTime boshi = sana.atStartOfDay();
        LocalDateTime oxiri = boshi.plusDays(1);

        // holat = 'tulanmagan' AND sana >= boshi AND sana < oxiri
        List<Jadval> jadvallar = jadvalRepository.findCurrentMonthUnpaid(boshi, oxiri);
        log.info("SMS eslatma: {} sanasiga {} ta to'lanmagan jadval topildi.", sana, jadvallar.size());

        int yuborildi = 0;
        for (Jadval jadval : jadvallar) {
            try {
                Optional<Shartnoma> opt = shartnomaRepository.findByJadvalListContaining(jadval);
                if (opt.isEmpty()) {
                    log.warn("Jadval id={} uchun shartnoma topilmadi — o'tkazib yuborildi.", jadval.getId());
                    continue;
                }
                Shartnoma shartnoma = opt.get();

                // yopilgan shartnomalarga eslatma yubormaymiz
                if ("yopilgan".equalsIgnoreCase(shartnoma.getStatus())) continue;

                // o'sha oy uchun QOLGAN summa (qisman to'langan bo'lsa hisobga olinadi)
                long qolgan = jadval.getSumma() - jadval.getTulangan();
                if (qolgan <= 0) continue;

                boolean ok = smsXabarService.tulovEslatmaYuborish(
                        shartnoma.getMijoz(), shartnoma.getId(), qolgan);
                if (ok) yuborildi++;

            } catch (Exception e) {
                // bitta xato butun jarayonni to'xtatmasin
                log.error("Eslatma yuborishda xato (jadval id={}): {}", jadval.getId(), e.getMessage());
            }
        }

        log.info("SMS eslatma yakunlandi: {} ta SMS yuborildi.", yuborildi);
        return yuborildi;
    }
}
