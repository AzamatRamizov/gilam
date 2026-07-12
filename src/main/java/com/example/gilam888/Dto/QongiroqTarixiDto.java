package com.example.gilam888.Dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class QongiroqTarixiDto {
    private Long id;
    private LocalDateTime sana;
    private String natija;
    private String izoh;
    private String operator;
    private LocalDate vadaSana;
    private String audioUrl;
}
