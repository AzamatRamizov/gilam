package com.example.gilam888.Entity;

import com.example.gilam888.Configurations.AbstractLongEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Mijoz extends AbstractLongEntity {
    private String ism;
    private String familiya;
    private String sharif;
    private String viloyat;
    private String tuman;
    private String manzil;
    private String muljal;
    private String tel1;
    private String tel2;
    private String tel3;
    private String passport;

    // Mijoz statusi: null = avtomatik hisoblanadi (to'lov intizomidan),
    // qiymat bo'lsa operator qo'lda belgilagan: "yaxshi" | "qisman" | "yomon"
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    private FaylBayt passportRasm;
    @ManyToOne(fetch = FetchType.LAZY)
    private FaylBayt katm;
}