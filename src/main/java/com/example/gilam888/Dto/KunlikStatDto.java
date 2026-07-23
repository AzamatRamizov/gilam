package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Kalendarning bitta kuni: shu kuni nechta shartnoma kiritilgan va umumiy summasi. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KunlikStatDto {
    private int kun;        // 1..31
    private long soni;
    private long summa;
}
