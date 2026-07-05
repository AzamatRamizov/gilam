package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartPointDto {
    private String label;
    private long val;
    private String state; // "past" | "today" | "dim"
}
