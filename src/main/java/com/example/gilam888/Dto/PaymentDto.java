package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private String sana;   // ISO string, LocalDateTime.toString() bilan
    private long summa;    // bu Jadval/PaymentHistory'da allaqachon long, o'zgartirish shart emas
    private String turi;
    private String dokon;
}
