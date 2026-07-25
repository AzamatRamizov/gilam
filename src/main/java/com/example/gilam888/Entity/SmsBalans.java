package com.example.gilam888.Entity;

import com.example.gilam888.Configurations.AbstractLongEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Eskiz hisobidagi pul balansi (bitta qator saqlanadi).
// Hisob to'ldirilganda operator "Balansni to'ldirish" orqali summa qo'shadi,
// har SMS yuborilganda SmsHisobService narxni avtomatik ayirib boradi.
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SmsBalans extends AbstractLongEntity {

    // Hozirgi qolgan balans (so'mda). Manfiy ham bo'lishi mumkin —
    // bu balans o'z vaqtida yangilanmaganini bildiradi.
    private Long balans;

    // Statistika uchun: hisobga jami qancha pul tashlangan
    private Long jamiToldirilgan;
}
