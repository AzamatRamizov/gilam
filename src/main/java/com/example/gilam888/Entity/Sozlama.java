package com.example.gilam888.Entity;

import com.example.gilam888.Configurations.AbstractLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tizim sozlamalari — kalit/qiymat ko'rinishida.
 *
 * Yangi sozlama qo'shish uchun ENTITY o'zgartirilmaydi: shunchaki
 * SozlamaService.Kalit ga yangi konstanta qo'shiladi va sahifaga
 * bitta maydon qo'shiladi. Shuning uchun keyingi sozlamalar
 * ketma-ket oson qo'shilaveradi.
 *
 * Qiymat doim String — o'qiyotganda SozlamaService int/boolean'ga o'giradi.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sozlama extends AbstractLongEntity {

    /** Masalan: "undiruv.kun", "undiruv.avto" */
    @Column(unique = true, nullable = false, length = 100)
    private String kalit;

    /** Qiymat matn ko'rinishida saqlanadi */
    @Column(length = 500)
    private String qiymat;

    /** Sozlama nima uchun kerakligi (admin uchun izoh) */
    @Column(length = 500)
    private String izoh;
}
