package com.example.gilam888.Dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MijozDataDto {
    private long mijozId;
    private String ism;
    private String familiya;
    private String sharif;
    private String tel1;
    private String tel2;
    private String tel3;
    private String tuman;
    private String viloyat;
    private String manzil;
    private String muljal;
    private String passport;
    private String about;
    private long dokonId;
    private long summa;
    private long foiz;
    private long muddat;
    private String izoh;
    private LocalDateTime shartnomaSana;
    private LocalDateTime tulovSana;
}
