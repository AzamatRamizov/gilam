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

// Tizimda bajarilgan har bir muhim amal (mijoz qo'shish/tahrirlash, to'lov,
// qo'ng'iroq natijasi, shartnoma yaratish/o'chirish va h.k.) shu jadvalga yoziladi.
// "So'ngi amallar" sahifasida (Hisobotlar menyusi) ko'rsatiladi.
@Entity
@Table(indexes = {
        @Index(name = "idx_amal_vaqt", columnList = "vaqt"),
        @Index(name = "idx_amal_user", columnList = "userId")
})
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AmalTarixi extends AbstractLongEntity {

    // Amalni bajargan foydalanuvchi (snapshot sifatida saqlanadi —
    // hodim o'chirilsa ham tarix buzilmasligi uchun id bilan birga ism ham yoziladi)
    private Long userId;
    private String userFish;
    private String username;

    // Amal turi: QONGIROQ, MIJOZ_QOSHISH, MIJOZ_TAHRIR, SHARTNOMA_QOSHISH,
    // SHARTNOMA_TAHRIR, SHARTNOMA_OCHIRISH, TOLOV, TOLOV_AYIRISH, UNDIRUV, HODIM, DOKON
    private String amalTuri;

    // Amalning odam o'qiydigan tavsifi ("Mijoz qo'shildi: Aliyev Vali", ...)
    @Column(columnDefinition = "TEXT")
    private String tavsif;

    // Bog'liq yozuvlar (bo'lishi shart emas) — sahifadan mijoz/shartnomaga o'tish uchun
    private Long mijozId;
    private Long shartnomaId;

    // To'lov amallarida summa (boshqa amallarda null)
    private Long summa;

    // Amal bajarilgan vaqt
    private LocalDateTime vaqt;
}
