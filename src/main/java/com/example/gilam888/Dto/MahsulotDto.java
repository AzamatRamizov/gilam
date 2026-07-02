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
}
