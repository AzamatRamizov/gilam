package com.example.gilam888.Dto;

import java.time.LocalDateTime;

/** Baza (ombor) dasturidan keladigan KATM so'rovi */
public record KatmSorovKirimDto(Long sotuvId,
                                String mijozIsmi,
                                String mijozTel,
                                String mahsulotNomi,
                                String mahsulotKod,
                                Double miqdor,
                                String birlik,
                                Double boyi,
                                Double eni,
                                Double kv,
                                Long tannarx,
                                Long sotuvNarxi,
                                Long birlikNarxi,
                                Long oldindanTulov,
                                Integer muddat,
                                String dokonNomi,
                                String izoh,
                                String sotuvchi,
                                LocalDateTime vaqt) {
}
