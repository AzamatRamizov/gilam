package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoydaResponseDTO {
    private List<FoydaItemDto> items;
    private int totalCount;
    private long totalSumma;
    private long totalTannarx;
    private long totalFoyda;
}
