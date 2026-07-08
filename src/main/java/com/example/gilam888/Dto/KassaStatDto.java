package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KassaStatDto {
    private long jami;
    private long son;
    private long ortacha;
    private String jamiDiff;          // "+12.4%" yoki null ("barchasi" uchun)

    private long kutilayotganSumma;
    private long kutilayotganSon;
    private long overdueSumma;
    private long overdueSon;

    private int naqdPct;
    private int terminalPct;
    private int otkazmaPct;
    private long naqdSumma;
    private long terminalSumma;
    private long otkazmaSumma;

    private String chartLabel;
    private List<ChartPointDto> chart;

    private List<PaymentDto> payments;

    private long oylikKerakSumma;
    private long oylikKerakSon;

    private long yopilganShartnomaSoni;
    private long activShartnomaSoni;
    private long muddatiOtganShartnomaSoni;
}
