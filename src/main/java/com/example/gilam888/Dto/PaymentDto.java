package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private Long id;                 // PaymentHistory id — "Qabul qildim" tugmasi uchun
    private String sana;
    private long summa;
    private String turi;
    private String dokon;
    private String mijoz;
    private Long shartnomaId;

    // Qabul holati
    private boolean qabulQilindi;
    private String qabulVaqti;
    private String qabulQilganFish;
}