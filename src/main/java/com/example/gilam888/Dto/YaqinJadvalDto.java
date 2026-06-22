package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YaqinJadvalDto {
    private Long    jadvalId;
    private Long    summa;
    private Long    tulangan;
    private String  holat;
    private LocalDateTime sana;        // to'lov sanasi
    private String  mijozFish;         // Familiya Ism Sharif
    private String  mijozTel;
    private Long    shartnomaSumma;
    private String  shartnomaMuddat;
    private Long    shartnomaId;
}
