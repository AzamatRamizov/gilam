package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Do'konlar bo'yicha statistika javobi (butun davr yoki tanlangan yil). */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DokonStatResponseDto {
    private List<Integer> yillar;      // filtr uchun mavjud yillar
    private Integer yil;               // null = barcha yillar
    private long jamiSoni;
    private long jamiSumma;
    private long jamiFoyda;
    private List<DokonStatDto> dokonlar;
}
