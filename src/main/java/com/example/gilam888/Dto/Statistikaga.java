package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Statistikaga {
    private String dokon;
    private String mijoz;
    private long summa;
    private String turi;
    private long shartnomaId;
    private LocalDateTime sana;
}
