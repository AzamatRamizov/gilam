package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatistikaResponseDto {
    private List<OylikStatDto> items;
    private long totalCount;
    private long totalSumma;
}
