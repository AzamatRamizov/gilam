package com.example.gilam888.Entity;

import com.example.gilam888.Configurations.AbstractLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Eskiz orqali yuborilgan har bir SMS shu jadvalga yoziladi.
// "SMS nazorati" sahifasida (Hisobotlar menyusi) ko'rsatiladi va
// yuborilgan SMS narxi Eskiz balansidan (SmsBalans) ayrilib boriladi.
@Entity
@Table(indexes = {
        @Index(name = "idx_sms_vaqt", columnList = "vaqt"),
        @Index(name = "idx_sms_holat", columnList = "holat")
})
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SmsTarix extends AbstractLongEntity {

    // Qabul qiluvchi raqam (998XXXXXXXXX ko'rinishida)
    private String telefon;

    // Raqam prefiksidan aniqlangan operator nomi (Beeline, Ucell, Mobiuz, ...)
    private String operator;

    // Yuborilgan SMS matni
    @Column(columnDefinition = "TEXT")
    private String matn;

    // SMS necha qismdan iborat (uzun matn bir nechta SMS bo'lib ketadi,
    // Eskiz har bir qism uchun alohida pul oladi)
    private Integer qismlar;

    // Bir qism SMS narxi (operator bo'yicha, so'mda)
    private Long birQismNarx;

    // Jami narx = birQismNarx * qismlar. Balansdan shu summa ayriladi.
    private Long narx;

    // SMS turi: YANGI_SHARTNOMA, TOLOV, ESLATMA
    private String turi;

    // Holat: YUBORILDI (Eskiz qabul qildi) yoki XATO.
    // XATO bo'lsa balansdan pul ayrilmaydi.
    private String holat;

    // Bog'liq yozuvlar — sahifadan mijoz/shartnomaga o'tish uchun
    private Long mijozId;
    private Long shartnomaId;

    // Mijoz ismi (snapshot — mijoz o'chirilsa ham tarix o'qilishi uchun)
    private String mijozFish;

    // Yuborilgan vaqt
    private LocalDateTime vaqt;
}
