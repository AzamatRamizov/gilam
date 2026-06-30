package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OverdueDebtDto {
    private String mijoz;
    private String tel;
    private long qolgan;
    private long kun;
    private LocalDateTime oxirgiTolov;
    private Long shartnomaId;
}
