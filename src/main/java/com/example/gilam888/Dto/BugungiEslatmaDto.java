package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BugungiEslatmaDto {
    private Long mijozId;
    private String ism;
    private String familiya;
    private String tel1;
    private String tel2;
    private String tel3;
    private Long shartnomaId;
    private long qolganQarz;
    private String status;
    private LocalDate vadaSana;
    private String izoh; // oxirgi qo'ng'iroqdagi izoh (va'da matni)
}
