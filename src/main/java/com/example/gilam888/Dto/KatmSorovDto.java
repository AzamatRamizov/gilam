package com.example.gilam888.Dto;

import java.time.LocalDateTime;

/**
 * KATM so'rovi — ro'yxat va shartnoma formasini to'ldirish uchun.
 *
 * @param dokonId  nomi bo'yicha topilgan magazin id (topilmasa 0)
 * @param about    "Mahsulot haqida" maydoniga tayyor matn
 */
public record KatmSorovDto(Long id,
                           Long sotuvId,
                           String mijozIsmi,
                           String mijozTel,
                           String mijozJshshir,
                           String mahsulotNomi,
                           String mahsulotKod,
                           Double miqdor,
                           String birlik,
                           Double boyi,
                           Double eni,
                           Double kv,
                           String about,
                           Long tannarx,
                           Long sotuvNarxi,
                           Long oldindanTulov,
                           Integer muddat,
                           String dokonNomi,
                           Long dokonId,
                           String izoh,
                           String sotuvchi,
                           LocalDateTime sotuvVaqti,
                           LocalDateTime kelganVaqt,
                           String holat,
                           Long shartnomaId,
                           String javobIzohi,
                           String javobBergan,
                           LocalDateTime javobVaqti) {
}
