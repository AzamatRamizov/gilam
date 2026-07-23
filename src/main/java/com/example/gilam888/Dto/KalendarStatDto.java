package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Kalendar ko'rinishidagi statistika javobi.
 * Bitta so'rov = bitta oy: kunlar bo'yicha sanoq + o'sha oydagi barcha shartnomalar.
 * Kun tanlash frontendda, qayta so'rovsiz bajariladi.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KalendarStatDto {
    private List<Integer> yillar;               // dropdown uchun mavjud yillar
    private int yil;
    private int oy;                             // 1..12
    private long oySoni;                        // shu oydagi jami shartnomalar
    private long oySumma;
    private long yilSoni;                       // shu yildagi jami
    private long yilSumma;
    private List<KunlikStatDto> kunlar;         // faqat soni > 0 bo'lgan kunlar
    private List<KunShartnomaDto> shartnomalar; // shu oydagi barcha shartnomalar
}
