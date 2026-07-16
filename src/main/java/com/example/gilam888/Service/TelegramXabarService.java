package com.example.gilam888.Service;

import com.example.gilam888.Entity.Jadval;
import com.example.gilam888.Entity.Mijoz;
import com.example.gilam888.Entity.Shartnoma;
import com.example.gilam888.Entity.TelegramFoydalanuvchi;
import com.example.gilam888.Repository.TelegramFoydalanuvchiRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

// Mijozning tel1/tel2/tel3 raqamlaridan biri Telegram botga ulangan bo'lsa,
// AdminService to'lov qabul qilingandan so'ng shu orqali xabar yuboradi.
@Slf4j
@Service
public class TelegramXabarService {

    private static final DateTimeFormatter SANA_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TelegramClient telegramClient;
    private final TelegramFoydalanuvchiRepository telegramFoydalanuvchiRepository;

    public TelegramXabarService(TelegramClient telegramClient,
                                 TelegramFoydalanuvchiRepository telegramFoydalanuvchiRepository) {
        this.telegramClient = telegramClient;
        this.telegramFoydalanuvchiRepository = telegramFoydalanuvchiRepository;
    }

    public void tulovXabarYuborish(Mijoz mijoz, long summa, String shartnomaMahsulot, Long shartnomaId, long umumiyQolgan) {
        if (mijoz == null) return;

        TelegramFoydalanuvchi foydalanuvchi = topishTelefonlarBoyicha(mijoz);
        if (foydalanuvchi == null) return; // mijoz botga ulanmagan — xabar yuborilmaydi

        String matn = String.format(
                "\uD83D\uDCB0 To'lov qabul qilindi!\n\nShartnoma: #%d\nMahsulot: %s\nTo'langan summa: %s so'm\nUmumiy qolgan summa: %s so'm",
                shartnomaId, shartnomaMahsulot == null ? "-" : shartnomaMahsulot, formatSumma(summa), formatSumma(umumiyQolgan)
        );

        InlineKeyboardButton jadvalTugma = InlineKeyboardButton.builder()
                .text("\uD83D\uDCCB To'lov jadvali")
                .callbackData("jadval_" + shartnomaId)
                .build();

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(List.of(jadvalTugma)))
                .build();

        try {
            telegramClient.execute(SendMessage.builder()
                    .chatId(foydalanuvchi.getChatId())
                    .text(matn)
                    .replyMarkup(keyboard)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Mijozga to'lov xabarini yuborishda xato (mijozId={})", mijoz.getId(), e);
        }
    }

    // Yangi shartnoma tuzilganda (addMijoz / addShartnoma) chaqiriladi —
    // mijoz botga ulangan bo'lsa, shartnoma ma'lumotlari va to'liq to'lov jadvali yuboriladi.
    public void yangiShartnomaXabarYuborish(Shartnoma shartnoma) {
        if (shartnoma == null || shartnoma.getMijoz() == null) return;

        TelegramFoydalanuvchi foydalanuvchi = topishTelefonlarBoyicha(shartnoma.getMijoz());
        if (foydalanuvchi == null) return; // mijoz botga ulanmagan — xabar yuborilmaydi

        StringBuilder sb = new StringBuilder();
        sb.append("\uD83D\uDCC4 Yangi shartnoma tuzildi!\n\n");
        sb.append(String.format("Shartnoma: #%d\n", shartnoma.getId()));
        if (shartnoma.getMahsulot() != null && !shartnoma.getMahsulot().isBlank()) {
            sb.append("Mahsulot: ").append(shartnoma.getMahsulot()).append("\n");
        }
        sb.append(String.format("Umumiy summa: %s so'm\n", formatSumma(shartnoma.getSumma())));
        sb.append(String.format("Muddat: %d oy\n", shartnoma.getMuddat()));
        sb.append("\n").append(jadvalMatniniShakllantirish(shartnoma));

        try {
            telegramClient.execute(SendMessage.builder()
                    .chatId(foydalanuvchi.getChatId())
                    .text(sb.toString())
                    .build());
        } catch (TelegramApiException e) {
            log.error("Mijozga yangi shartnoma xabarini yuborishda xato (shartnomaId={})", shartnoma.getId(), e);
        }
    }

    // Shartnomaning to'liq to'lov jadvalini matn ko'rinishida shakllantiradi.
    // Ham yangi shartnoma xabarida, ham "To'lov jadvali" tugmasi bosilganda (Gilam888TelegramBot) ishlatiladi.
    public String jadvalMatniniShakllantirish(Shartnoma shartnoma) {
        List<Jadval> jadvalList = shartnoma.getJadvalList();
        if (jadvalList == null || jadvalList.isEmpty()) {
            return String.format("\uD83D\uDCCB Shartnoma #%d uchun to'lov jadvali mavjud emas.", shartnoma.getId());
        }

        List<Jadval> tartiblangan = jadvalList.stream()
                .sorted(Comparator.comparing(Jadval::getSana))
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("\uD83D\uDCCB To'lov jadvali:\n\n");

        int i = 1;
        for (Jadval jadval : tartiblangan) {
            boolean tulangan = "tulangan".equals(jadval.getHolat());
            String belgi = tulangan ? "✅" : "❌";
            String sana = jadval.getSana() != null ? jadval.getSana().format(SANA_FORMAT) : "-";

            sb.append(String.format("%d. %s — %s so'm — %s", i, sana, formatSumma(jadval.getSumma()), belgi));
            if (tulangan) {
                sb.append(" To'langan");
            } else {
                long qolgan = Math.max(0, jadval.getSumma() - jadval.getTulangan());
                sb.append(String.format(" To'lanmagan (qoldiq: %s so'm)", formatSumma(qolgan)));
            }
            sb.append("\n");
            i++;
        }

        long umumiyQolgan = tartiblangan.stream()
                .mapToLong(j -> Math.max(0, j.getSumma() - j.getTulangan()))
                .sum();
        sb.append("\n").append("Umumiy qolgan summa: ").append(formatSumma(umumiyQolgan)).append(" so'm");

        return sb.toString();
    }

    private String formatSumma(long summa) {
        return String.format("%,d", summa).replace(',', ' ');
    }

    private TelegramFoydalanuvchi topishTelefonlarBoyicha(Mijoz mijoz) {
        for (String tel : new String[]{mijoz.getTel1(), mijoz.getTel2(), mijoz.getTel3()}) {
            if (tel == null || tel.isBlank()) continue;
            Optional<TelegramFoydalanuvchi> topilgan =
                    telegramFoydalanuvchiRepository.findByTelefonRaqami(Gilam888TelegramBot.normalizeTelefon(tel));
            if (topilgan.isPresent()) return topilgan.get();
        }
        return null;
    }
}
