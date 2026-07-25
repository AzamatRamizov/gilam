package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Statistika: bitta do'kon bo'yicha yig'ma ko'rsatkichlar. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DokonStatDto {
    private Long dokonId;       // null = do'koni belgilanmagan
    private String dokonNomi;
    private long soni;          // shartnomalar soni
    private long summa;         // umumiy shartnoma summasi
    private long tannarx;       // umumiy tan narx (o'qilganlari)
    private long foyda;         // summa - tannarx
    private double ulush;       // umumiy summadagi ulushi (%)
}
