package com.example.gilam888.Dto;

import lombok.Data;

import java.util.List;

@Data
public class MijozSahifaDto {
    private long id;
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

    // Rasm fayllari (/admin/fayl/{id} orqali ochiladi), bo'lmasa null
    private Long passportRasmId;
    private Long katmId;

    // Status
    private String status;      // ko'rsatiladigan status: yaxshi | qisman | yomon
    private boolean qolda;      // true = operator qo'lda belgilagan
    private String avtoStatus;  // avto hisoblangan status (qo'lda rejimda ham ko'rsatish uchun)
    private int muddatiOtganOylar;

    // Umumiy statistika
    private int shartnomaSoni;
    private int ochiqShartnomaSoni;
    private long jamiSumma;
    private long tulanganSumma;
    private long qoldiqSumma;

    private List<ShartnomaQisqaDto> shartnomalar;
}