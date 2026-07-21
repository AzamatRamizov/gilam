package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OylikStatDto {
    private String oy;      // "2026-07" formatida
    private long soni;      // shu oyda qo'shilgan shartnomalar soni
    private long summa;     // shu oydagi shartnomalar umumiy summasi
}
