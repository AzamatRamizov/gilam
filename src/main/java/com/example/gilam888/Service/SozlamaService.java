package com.example.gilam888.Service;

import com.example.gilam888.Entity.Sozlama;
import com.example.gilam888.Repository.SozlamaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tizim sozlamalarini o'qish/yozish.
 *
 * YANGI SOZLAMA QO'SHISH TARTIBI (3 qadam):
 *   1. Quyidagi Kalit klassiga konstanta + DEFAULT qiymat qo'shing
 *   2. hammasi() metodidagi map'ga qo'shing (sahifaga shu orqali chiqadi)
 *   3. sozlama.html ga bitta maydon qo'shing (id = kalitning nuqtasiz shakli)
 * Bazaga migratsiya kerak emas — jadval kalit/qiymat ko'rinishida.
 */
@Slf4j
@Service
public class SozlamaService {

    /** Sozlama kalitlari va ularning standart qiymatlari */
    public static final class Kalit {
        /** Necha kun kechikkanda shartnoma avtomatik undiruvga o'tadi */
        public static final String UNDIRUV_KUN = "undiruv.kun";
        public static final String UNDIRUV_KUN_DEFAULT = "30";

        /** Avtomatik undiruv umuman yoqilganmi (yoqilmasa scheduler ishlamaydi) */
        public static final String UNDIRUV_AVTO = "undiruv.avto";
        public static final String UNDIRUV_AVTO_DEFAULT = "true";

        // ── Keyingi sozlamalar shu yerga qo'shiladi ──
        // public static final String X = "x.y";
        // public static final String X_DEFAULT = "...";

        private Kalit() {}
    }

    private final SozlamaRepository sozlamaRepository;
    private final AmalService amalService;

    public SozlamaService(SozlamaRepository sozlamaRepository, AmalService amalService) {
        this.sozlamaRepository = sozlamaRepository;
        this.amalService = amalService;
    }

    // ─────────────────────── O'QISH ───────────────────────

    /** Matn qiymat; bazada yo'q bo'lsa standart qiymat qaytadi */
    public String get(String kalit, String standart) {
        return sozlamaRepository.findByKalit(kalit)
                .map(Sozlama::getQiymat)
                .filter(v -> v != null && !v.isBlank())
                .orElse(standart);
    }

    /** Butun son; o'qib bo'lmasa standart qiymat */
    public int getInt(String kalit, int standart) {
        try {
            return Integer.parseInt(get(kalit, String.valueOf(standart)).trim());
        } catch (NumberFormatException e) {
            log.warn("Sozlama '{}' soni noto'g'ri, standart qiymat ishlatildi: {}", kalit, standart);
            return standart;
        }
    }

    public boolean getBool(String kalit, boolean standart) {
        return "true".equalsIgnoreCase(get(kalit, String.valueOf(standart)).trim());
    }

    /** Undiruvga chiqish kuni — schedulerdagi asosiy chaqiruv */
    public int undiruvKun() {
        return getInt(Kalit.UNDIRUV_KUN, Integer.parseInt(Kalit.UNDIRUV_KUN_DEFAULT));
    }

    public boolean undiruvAvtoYoqilgan() {
        return getBool(Kalit.UNDIRUV_AVTO, Boolean.parseBoolean(Kalit.UNDIRUV_AVTO_DEFAULT));
    }

    /** Sahifa uchun barcha sozlamalar (bazada yo'qlari standart qiymat bilan) */
    public Map<String, Object> hammasi() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(Kalit.UNDIRUV_KUN,  undiruvKun());
        m.put(Kalit.UNDIRUV_AVTO, undiruvAvtoYoqilgan());
        // ── Keyingi sozlamalar shu yerga ──
        return m;
    }

    // ─────────────────────── YOZISH ───────────────────────

    @Transactional
    public void set(String kalit, String qiymat, String izoh) {
        Sozlama s = sozlamaRepository.findByKalit(kalit).orElseGet(() -> {
            Sozlama yangi = new Sozlama();
            yangi.setKalit(kalit);
            return yangi;
        });
        String eski = s.getQiymat();
        s.setQiymat(qiymat);
        if (izoh != null) s.setIzoh(izoh);
        sozlamaRepository.save(s);

        if (eski == null || !eski.equals(qiymat)) {
            amalService.log("SOZLAMA",
                    "Sozlama o'zgartirildi: " + kalit + " = " + qiymat
                            + (eski != null ? " (avval: " + eski + ")" : ""));
        }
    }

    /**
     * Sahifadan kelgan sozlamalarni tekshirib saqlaydi.
     * Faqat yuborilgan kalitlar yangilanadi — qolganlari tegilmaydi.
     *
     * @return xato bo'lsa xabar matni, hammasi joyida bo'lsa null
     */
    @Transactional
    public String saqla(Map<String, String> kelgan) {
        if (kelgan == null || kelgan.isEmpty()) return "Saqlash uchun ma'lumot yo'q";

        // ── undiruv.kun ──
        if (kelgan.containsKey(Kalit.UNDIRUV_KUN)) {
            int kun;
            try {
                kun = Integer.parseInt(kelgan.get(Kalit.UNDIRUV_KUN).trim());
            } catch (Exception e) {
                return "Undiruvga chiqish kuni butun son bo'lishi kerak";
            }
            if (kun < 1 || kun > 365) {
                return "Undiruvga chiqish kuni 1 va 365 orasida bo'lishi kerak";
            }
            set(Kalit.UNDIRUV_KUN, String.valueOf(kun),
                    "To'lov shu kundan ko'p kechiksa shartnoma avtomatik undiruvga o'tadi");
        }

        // ── undiruv.avto ──
        if (kelgan.containsKey(Kalit.UNDIRUV_AVTO)) {
            boolean yoqilgan = "true".equalsIgnoreCase(kelgan.get(Kalit.UNDIRUV_AVTO).trim());
            set(Kalit.UNDIRUV_AVTO, String.valueOf(yoqilgan),
                    "Avtomatik undiruv yoqilgan/o'chirilgan");
        }

        // ── Keyingi sozlamalar shu yerga (tekshiruvi bilan) ──

        return null;
    }
}
