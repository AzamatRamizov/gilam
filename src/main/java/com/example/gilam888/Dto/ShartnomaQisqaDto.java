package com.example.gilam888.Dto;

import lombok.Data;

@Data
public class ShartnomaQisqaDto {
    private long id;
    private String mahsulot;
    private String sotibOlinganSana;
    private long muddat;
    private String status;          // "ochiq" | "yopilgan"
    private long jamiSumma;         // jadvallar summasi
    private long tulangan;          // jadvallardagi to'langan
    private long qoldiq;
    private int kechikkanOylar;     // muddati o'tgan to'lanmagan jadvallar soni
}