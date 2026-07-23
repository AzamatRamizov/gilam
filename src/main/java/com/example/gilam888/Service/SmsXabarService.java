package com.example.gilam888.Service;

import com.example.gilam888.Entity.Mijoz;
import com.example.gilam888.Entity.Shartnoma;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Eskiz.uz orqali mijozga yuboriladigan SMS matnlari.
 *
 * MUHIM: Bu yerdagi 3 ta matn Eskiz kabinetida TASDIQLANGAN shablonga
 * so'zma-so'z mos bo'lishi shart. Matnni o'zgartirsangiz — Eskiz'da ham
 * shablonni qayta tasdiqlatish kerak, aks holda SMS ketmaydi.
 *
 * Shablonlar:
 *  1) Yangi shartnoma        -> yangiShartnomaXabarYuborish()
 *  2) To'lov qabul qilindi   -> tulovXabarYuborish()
 *  3) Ertaga to'lov kuni     -> tulovEslatmaYuborish()  (SmsEslatmaScheduler chaqiradi)
 *
 * Xabar mijozning birinchi mavjud raqamiga (tel1 -> tel2 -> tel3) yuboriladi.
 */
@Slf4j
@Service
public class SmsXabarService {

    private static final String BOT_LINK = "https://t.me/gilam888_bot";

    private final EskizSmsService eskiz;

    public SmsXabarService(EskizSmsService eskiz) {
        this.eskiz = eskiz;
    }

    /**
     * 1-shablon — yangi shartnoma rasmiylashtirilganda.
     *
     * 888 Gilam Magazini.
     * Shartnoma #10000 rasmiylashtirildi
     * Umumiy summa: 50000000 so'm
     * Muddat: 12 oy
     * Batafsil malumot: https://t.me/gilam888_bot
     */
    public void yangiShartnomaXabarYuborish(Shartnoma shartnoma) {
        if (shartnoma == null || shartnoma.getMijoz() == null) return;
        String tel = birinchiRaqam(shartnoma.getMijoz());
        if (tel == null) return;

        String matn = "888 Gilam Magazini.\n"
                + "Shartnoma #" + shartnoma.getId() + " rasmiylashtirildi\n"
                + "Umumiy summa: " + shartnoma.getSumma() + " so'm\n"
                + "Muddat: " + shartnoma.getMuddat() + " oy\n"
                + "Batafsil malumot: " + BOT_LINK;

        eskiz.sendSms(tel, matn);
    }

    /**
     * 2-shablon — to'lov qabul qilinganda.
     *
     * 888 Gilam Magazini.
     * Shartnoma #15741 bo'yicha 10000000 so'm to'lovingiz qabul qilindi.
     * Qolgan qarz: 50000000 so'm.
     * Batafsil malumot: https://t.me/gilam888_bot
     *
     * Imzo o'zgarmadi — AdminService'dagi mavjud chaqiruvlarni tahrirlash shart emas.
     * (shartnomaMahsulot endi matnda ishlatilmaydi.)
     */
    public void tulovXabarYuborish(Mijoz mijoz, long summa, String shartnomaMahsulot,
                                   Long shartnomaId, long umumiyQolgan) {
        if (mijoz == null) return;
        String tel = birinchiRaqam(mijoz);
        if (tel == null) return;

        String matn = "888 Gilam Magazini.\n"
                + "Shartnoma #" + shartnomaId + " bo'yicha " + summa + " so'm to'lovingiz qabul qilindi.\n"
                + "Qolgan qarz: " + umumiyQolgan + " so'm.\n"
                + "Batafsil malumot: " + BOT_LINK;

        eskiz.sendSms(tel, matn);
    }

    /**
     * 3-shablon — to'lov kunidan 1 kun oldin eslatma.
     *
     * 888 Gilam Magazini.
     * Hurmatli mijoz! Ertaga sizning #10000 shartnomangiz to'lov kuni.
     * To'lov summasi: 10000000 so'm
     * Batafsil malumot: https://t.me/gilam888_bot
     *
     * @param tulovSumma o'sha oy uchun qolgan (to'lanishi kerak) summa
     * @return SMS Eskiz'ga topshirildimi
     */
    public boolean tulovEslatmaYuborish(Mijoz mijoz, Long shartnomaId, long tulovSumma) {
        if (mijoz == null || shartnomaId == null) return false;
        String tel = birinchiRaqam(mijoz);
        if (tel == null) {
            log.warn("Eslatma yuborilmadi — mijozda telefon raqam yo'q. shartnomaId={}", shartnomaId);
            return false;
        }

        String matn = "888 Gilam Magazini.\n"
                + "Hurmatli mijoz! Ertaga sizning #" + shartnomaId + " shartnomangiz to'lov kuni.\n"
                + "To'lov summasi: " + tulovSumma + " so'm\n"
                + "Batafsil malumot: " + BOT_LINK;

        return eskiz.sendSms(tel, matn);
    }

    /** Mijozning birinchi bo'sh bo'lmagan raqamini qaytaradi (tel1 -> tel2 -> tel3). */
    private String birinchiRaqam(Mijoz mijoz) {
        for (String tel : new String[]{mijoz.getTel1(), mijoz.getTel2(), mijoz.getTel3()}) {
            if (tel != null && !tel.isBlank()) return tel;
        }
        return null;
    }
}