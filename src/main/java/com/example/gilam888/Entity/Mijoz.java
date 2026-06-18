package com.example.gilam888.Entity;

import com.example.gilam888.Configurations.AbstractLongEntity;
import jakarta.persistence.Entity;
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

    @ManyToOne
    private FaylBayt passportRasm;
    @ManyToOne
    private FaylBayt katm;
}
