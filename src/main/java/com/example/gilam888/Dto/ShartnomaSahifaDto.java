package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Shartnomalar ro'yxatining BITTA sahifasi.
 * Butun ro'yxat emas, faqat so'ralgan sahifa qaytariladi — shu tufayli
 * necha ming shartnoma bo'lsa ham heap to'lib qolmaydi.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShartnomaSahifaDto {
    /** Shu sahifadagi qatorlar */
    private List<ShartnomaRoyxat> items;
    /** Filtrga mos jami qatorlar soni (barcha sahifalar bo'yicha) */
    private long jami;
    /** Filtrga mos jami summa */
    private long jamiSumma;
    /** Hozirgi sahifa (1 dan boshlanadi) */
    private int page;
    /** Sahifadagi qatorlar soni */
    private int size;
    /** Umumiy sahifalar soni */
    private int pages;
    /** Tab (Barchasi / Faol / Yopilgan / Undiruv) uchun umumiy statistika */
    private ShartnomaStatDto stat;
}
