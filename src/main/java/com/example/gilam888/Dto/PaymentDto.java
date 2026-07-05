package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private String sana;
    private long summa;
    private String turi;
    private String dokon;
    private String mijoz;
    private Long shartnomaId;
}
