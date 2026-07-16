package com.example.gilam888.Service;

import com.example.gilam888.Entity.Mijoz;
import com.example.gilam888.Entity.Shartnoma;
import com.example.gilam888.Entity.TelegramFoydalanuvchi;
import com.example.gilam888.Repository.MijozRepository;
import com.example.gilam888.Repository.ShartnomaRepository;
import com.example.gilam888.Repository.TelegramFoydalanuvchiRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Bot mijozdan /start va telefon raqamini qabul qilib, TelegramFoydalanuvchi jadvaliga yozadi.
// Shu raqam orqali keyinchalik AdminService to'lov xabarini yuboradi (TelegramXabarService).
//
// Guruhga qo'shilish IXTIYORIY: menyudagi "Bizning guruhlar" tugmasi orqali mijozga
// application.properties'dagi telegram.bot.guruhlar ro'yxati havola tugmalari ko'rinishida
// taklif qilinadi, lekin botdan foydalanish uchun a'zolik talab qilinmaydi.
@Slf4j
@Component
public class Gilam888TelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final String GURUHLAR_TUGMA = "\uD83D\uDCE2 Bizning guruhlar";
    private static final String JADVAL_TUGMA = "\uD83D\uDCCB To'lov jadvalim";

    private final TelegramClient telegramClient;
    private final TelegramFoydalanuvchiRepository telegramFoydalanuvchiRepository;
    private final ShartnomaRepository shartnomaRepository;
    private final MijozRepository mijozRepository;
    private final TelegramXabarService telegramXabarService;

    @Value("${telegram.bot.token}")
    private String botToken;

    // "Nomi|havola ; Nomi|havola" ko'rinishidagi ro'yxat (application.properties'dan)
    @Value("${telegram.bot.guruhlar:}")
    private String guruhlarConfig;

    public Gilam888TelegramBot(TelegramClient telegramClient,
                                TelegramFoydalanuvchiRepository telegramFoydalanuvchiRepository,
                                ShartnomaRepository shartnomaRepository,
                                MijozRepository mijozRepository,
                                TelegramXabarService telegramXabarService) {
        this.telegramClient = telegramClient;
        this.telegramFoydalanuvchiRepository = telegramFoydalanuvchiRepository;
        this.shartnomaRepository = shartnomaRepository;
        this.mijozRepository = mijozRepository;
        this.telegramXabarService = telegramXabarService;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingSingleThreadUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (update.hasCallbackQuery()) {
            callbackQueryniQabulQilish(update.getCallbackQuery());
            return;
        }
        if (!update.hasMessage()) return;
        Message message = update.getMessage();

        // Bot faqat shaxsiy chatda ishlaydi — guruh ichidagi xabarlarga javob bermaymiz,
        // aks holda guruhga qo'shilgan har bir mijozning xabariga bot javob yozib yuborardi.
        if (!message.getChat().isUserChat()) return;

        Long chatId = message.getChatId();

        try {
            if (message.hasText() && "/start".equals(message.getText())) {
                kontaktSorash(chatId);
            } else if (message.hasContact()) {
                String telefon = normalizeTelefon(message.getContact().getPhoneNumber());
                saqlashYokiYangilash(telefon, chatId, message.getFrom() != null ? message.getFrom().getUserName() : null,
                        message.getContact().getFirstName());
                telegramClient.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text("✅ Raqamingiz muvaffaqiyatli bog'landi. Endi to'lovlaringiz haqida shu yerda xabar olib turasiz.")
                        .replyMarkup(asosiyMenyu())
                        .build());
            } else if (message.hasText() && GURUHLAR_TUGMA.equals(message.getText())) {
                guruhlarniKorsatish(chatId);
            } else if (message.hasText() && JADVAL_TUGMA.equals(message.getText())) {
                jadvalniKorsatish(chatId);
            } else {
                telegramClient.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text("Iltimos, /start tugmasini bosing va telefon raqamingizni yuboring.")
                        .build());
            }
        } catch (TelegramApiException e) {
            log.error("Telegram xabar yuborishda xato", e);
        }
    }

    // ─────────────────────────── BIZNING GURUHLAR ───────────────────────────

    // Doimiy pastki menyu: hozircha bitta "Bizning guruhlar" tugmasi.
    // Keyinchalik yangi tugmalar shu yerga qo'shiladi.
    private ReplyKeyboardMarkup asosiyMenyu() {
        return ReplyKeyboardMarkup.builder()
                .keyboardRow(new KeyboardRow(List.of(KeyboardButton.builder().text(JADVAL_TUGMA).build())))
                .keyboardRow(new KeyboardRow(List.of(KeyboardButton.builder().text(GURUHLAR_TUGMA).build())))
                .resizeKeyboard(true)
                .build();
    }

    // application.properties'dagi telegram.bot.guruhlar qiymatini o'qib,
    // har bir guruh uchun alohida havola-tugma (inline) shakllantiradi.
    private void guruhlarniKorsatish(Long chatId) throws TelegramApiException {
        List<InlineKeyboardRow> qatorlar = new ArrayList<>();

        if (guruhlarConfig != null && !guruhlarConfig.isBlank()) {
            for (String bolak : guruhlarConfig.split(";")) {
                String[] qism = bolak.split("\\|", 2);
                if (qism.length != 2) continue;
                String nomi = qism[0].trim();
                String link = qism[1].trim();
                if (nomi.isEmpty() || link.isEmpty()) continue;

                qatorlar.add(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(nomi)
                                .url(link)
                                .build()));
            }
        }

        if (qatorlar.isEmpty()) {
            xabarYuborish(chatId, "Hozircha guruhlar ro'yxati kiritilmagan.");
            return;
        }

        telegramClient.execute(SendMessage.builder()
                .chatId(chatId)
                .text("\uD83D\uDCE2 Bizning rasmiy guruhlarimiz.\n\n"
                        + "Yangi gilamlar, aksiya va chegirmalardan xabardor bo'lib turish uchun qo'shilib oling:")
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(qatorlar).build())
                .build());
    }

    // ─────────────────────────── TO'LOV JADVALIM ───────────────────────────

    // Menyudagi "To'lov jadvalim" tugmasi bosilganda ishga tushadi.
    // ChatId orqali bog'langan telefon raqami topiladi, shu raqamga tegishli mijoz(lar)ning
    // shartnomalari qidiriladi va to'lov jadvali ko'rsatiladi.
    private void jadvalniKorsatish(Long chatId) throws TelegramApiException {
        Optional<TelegramFoydalanuvchi> foydalanuvchi = telegramFoydalanuvchiRepository.findByChatId(chatId);
        if (foydalanuvchi.isEmpty() || foydalanuvchi.get().getTelefonRaqami() == null) {
            xabarYuborish(chatId, "Avval telefon raqamingizni bog'lashingiz kerak. /start tugmasini bosing.");
            return;
        }

        // +998901234567 -> 901234567 (mijoz jadvalidagi raqamlar bilan solishtirish uchun)
        String digits = foydalanuvchi.get().getTelefonRaqami().replaceAll("[^0-9]", "");
        String oxirgi9 = digits.length() > 9 ? digits.substring(digits.length() - 9) : digits;

        List<Shartnoma> shartnomalar = new ArrayList<>();
        for (Mijoz mijoz : mijozRepository.findByTelefonOxiri(oxirgi9)) {
            shartnomalar.addAll(shartnomaRepository.findByMijoz_Id(mijoz.getId()));
        }

        if (shartnomalar.isEmpty()) {
            xabarYuborish(chatId, "Sizning raqamingizga tegishli shartnoma topilmadi.\n\n"
                    + "Agar shartnomangiz bo'lsa, magazinda ro'yxatdan o'tgan raqamingiz botga ulangan raqam bilan bir xil ekanini tekshiring.");
            return;
        }

        // Bitta shartnoma bo'lsa — jadvalni darhol ko'rsatamiz
        if (shartnomalar.size() == 1) {
            xabarYuborish(chatId, telegramXabarService.jadvalMatniniShakllantirish(shartnomalar.get(0)));
            return;
        }

        // Bir nechta shartnoma bo'lsa — har biri uchun tugma chiqaramiz,
        // tanlangani mavjud "jadval_<id>" callback orqali ko'rsatiladi
        List<InlineKeyboardRow> qatorlar = new ArrayList<>();
        for (Shartnoma shartnoma : shartnomalar) {
            String yorliq = "Shartnoma #" + shartnoma.getId();
            if (shartnoma.getMahsulot() != null && !shartnoma.getMahsulot().isBlank()) {
                yorliq += " — " + shartnoma.getMahsulot();
            }
            if ("yopilgan".equals(shartnoma.getStatus())) {
                yorliq += " ✅";
            }
            qatorlar.add(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text(yorliq)
                            .callbackData("jadval_" + shartnoma.getId())
                            .build()));
        }

        telegramClient.execute(SendMessage.builder()
                .chatId(chatId)
                .text("Sizda " + shartnomalar.size() + " ta shartnoma bor. Jadvalini ko'rmoqchi bo'lganingizni tanlang:")
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(qatorlar).build())
                .build());
    }

    // ─────────────────────────── CALLBACK QUERY ───────────────────────────

    // "To'lov jadvali" tugmasi bosilganda ishga tushadi (callback_data = "jadval_<shartnomaId>")
    private void callbackQueryniQabulQilish(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        // Bot faqat shaxsiy chatlarda ishlaydi — bunday holatda chatId har doim foydalanuvchi Id'siga teng,
        // shu sababli callbackQuery.getMessage() (MaybeInaccessibleMessage) o'rniga foydalanuvchidan olamiz.
        Long chatId = callbackQuery.getFrom().getId();

        try {
            telegramClient.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .build());
        } catch (TelegramApiException e) {
            log.error("Callback query javobini yuborishda xato", e);
        }

        if (data == null || !data.startsWith("jadval_")) return;

        Long shartnomaId;
        try {
            shartnomaId = Long.parseLong(data.substring("jadval_".length()));
        } catch (NumberFormatException e) {
            return;
        }

        Optional<Shartnoma> byId = shartnomaRepository.findById(shartnomaId);
        if (byId.isEmpty()) {
            xabarYuborish(chatId, "Shartnoma topilmadi.");
            return;
        }

        xabarYuborish(chatId, telegramXabarService.jadvalMatniniShakllantirish(byId.get()));
    }

    // ─────────────────────────── ASOSIY OQIM ───────────────────────────

    private void kontaktSorash(Long chatId) throws TelegramApiException {
        KeyboardButton kontaktTugma = KeyboardButton.builder()
                .text("\uD83D\uDCF1 Telefon raqamni yuborish")
                .requestContact(true)
                .build();

        ReplyKeyboardMarkup keyboard = ReplyKeyboardMarkup.builder()
                .keyboardRow(new KeyboardRow(List.of(kontaktTugma)))
                .keyboardRow(new KeyboardRow(List.of(KeyboardButton.builder().text(JADVAL_TUGMA).build())))
                .keyboardRow(new KeyboardRow(List.of(KeyboardButton.builder().text(GURUHLAR_TUGMA).build())))
                .resizeKeyboard(true)
                .build();

        telegramClient.execute(SendMessage.builder()
                .chatId(chatId)
                .text("Assalomu alaykum!\n\n888 Gilam Magazini botiga xush kelibsiz. To'lovlaringiz haqida shu yerda xabar olish uchun quyidagi tugma orqali telefon raqamingizni yuboring.")
                .replyMarkup(keyboard)
                .build());
    }

    private void saqlashYokiYangilash(String telefon, Long chatId, String username, String ismi) {
        TelegramFoydalanuvchi foydalanuvchi = telegramFoydalanuvchiRepository.findByTelefonRaqami(telefon)
                .orElseGet(TelegramFoydalanuvchi::new);
        foydalanuvchi.setTelefonRaqami(telefon);
        foydalanuvchi.setChatId(chatId);
        foydalanuvchi.setTelegramUsername(username);
        foydalanuvchi.setIsmi(ismi);
        telegramFoydalanuvchiRepository.save(foydalanuvchi);
    }

    private void xabarYuborish(Long chatId, String matn) {
        try {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(matn)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Xabar yuborishda xato (chatId={})", chatId, e);
        }
    }

    // Telegramdan keladigan raqam turlicha formatda bo'lishi mumkin (+998901234567, 998901234567, 901234567)
    // Mijoz.tel1/tel2/tel3 bilan solishtirish uchun bitta formatga keltiramiz.
    public static String normalizeTelefon(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() == 9) {
            digits = "998" + digits;
        }
        return "+" + digits;
    }
}
