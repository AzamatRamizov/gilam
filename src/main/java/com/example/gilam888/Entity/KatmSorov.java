package com.example.gilam888.Entity;

import com.example.gilam888.Configurations.AbstractLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Baza (ombor) dasturidan kelgan KATM so'rovi.
 *
 * Sotuvchi omborda mahsulotni "KATMga o'tkazish" tugmasi bilan chiqaradi,
 * so'rov shu jadvalga tushadi. Bu yerdagi operator so'rovni bosib
 * "Yangi shartnoma" formasini to'ldirilgan holda ochadi va shartnoma tuzadi.
 *
 * Holat matn sifatida saqlanadi (enum CHECK constraint muammosidan qochish uchun):
 *   YANGI | BAJARILDI | RAD_ETILDI
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class KatmSorov extends AbstractLongEntity {

    public static final String YANGI = "YANGI";
    public static final String BAJARILDI = "BAJARILDI";
    public static final String RAD_ETILDI = "RAD_ETILDI";

    /** Baza dasturidagi Sotuv id — javob qaytarishda ishlatiladi */
    private Long sotuvId;

    // ---- Mijoz ----
    private String mijozIsmi;
    private String mijozTel;

    // ---- Mahsulot ----
    private String mahsulotNomi;
    private String mahsulotKod;
    private Double miqdor;
    private String birlik;
    private Double boyi;
    private Double eni;
    private Double kv;

    // ---- Pul ----
    /** Tovar asl (tan) narxi */
    private Long tannarx;

    /** Sotilgan narx — shartnoma summasi bo'ladi */
    private Long sotuvNarxi;

    /** 1 birlik narxi (metrajda 1 kv.metr uchun) */
    private Long birlikNarxi;

    /** Oldindan (boshlang'ich naqd) to'lov */
    private Long oldindanTulov;

    /** Nasiya muddati (oy) */
    private Integer muddat;

    /** Sotilgan do'kon nomi (bu yerdagi magazin bilan nomi bo'yicha moslanadi) */
    private String dokonNomi;

    @Column(length = 500)
    private String izoh;

    /** Ombordagi sotuvchi F.I.SH */
    private String sotuvchi;

    /** Ombordan chiqarilgan vaqt */
    private LocalDateTime sotuvVaqti;

    /** Bu yerga kelgan vaqt */
    private LocalDateTime kelganVaqt;

    @Column(length = 20)
    private String holat = YANGI;

    /** Tuzilgan shartnoma id (bajarilganda) */
    private Long shartnomaId;

    @Column(length = 500)
    private String javobIzohi;

    /** Javobni bergan hodim */
    private String javobBergan;

    private LocalDateTime javobVaqti;

    /** Javob baza dasturiga yetkazildimi */
    private Boolean bazagaYuborildi = false;

    @Column(length = 300)
    private String bazaXato;
}
