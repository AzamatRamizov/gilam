package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shartnomalar bo'yicha umumiy statistika — status kesimida soni va summasi.
 * Bitta GROUP BY so'rovi bilan olinadi (entity yuklanmaydi).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShartnomaStatDto {
    private long hammasi;
    private long faol;
    private long yopilgan;
    private long undiruv;

    private long hammasiSumma;
    private long faolSumma;
    private long yopilganSumma;
    private long undiruvSumma;
}
