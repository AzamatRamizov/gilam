package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Kalendarda kun tanlanganda pastda chiqadigan jadval qatori. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KunShartnomaDto {
    private long id;
    private String sana;      // "yyyy-MM-dd"
    private String fish;
    private String tel;
    private long summa;
    private long muddat;
    private String status;
}
