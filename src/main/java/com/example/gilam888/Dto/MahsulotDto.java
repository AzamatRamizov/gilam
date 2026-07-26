package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MahsulotDto {
    private long shartnomaId;
    private String nomi;
    private String sana;   // "yyyy-MM-dd"
    private String narx;
    private String lokatsiya; // Telegramdan olingan lokatsiya (link yoki matn), string sifatida saqlanadi
    private Long dokonId;     // sotilgan do'kon (Magazin.id), NULL = belgilanmagan
    private String dokonNomi; // faqat ko'rsatish uchun (frontend select'ni to'ldirishda ishlatiladi)
    private String izoh;      // mahsulotga izoh (Shartnoma.izoh) — mahsulot tabida tahrirlanadi
    private Long oldindanTulov; // boshlang'ich naqd to'lov, NULL = yo'q (0 deb qaraladi)
}
