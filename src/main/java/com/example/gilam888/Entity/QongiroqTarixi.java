package com.example.gilam888.Entity;

import com.example.gilam888.Configurations.AbstractLongEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class QongiroqTarixi extends AbstractLongEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mijoz_id", nullable = false)
    private Mijoz mijoz;

    @Column(nullable = false)
    private String natija;

    @Column(columnDefinition = "TEXT")
    private String izoh;

    private LocalDate vadaSana;

    @Column(nullable = false)
    private LocalDateTime sana;

    private String operator;

    // Faqat audio bor-yo'qligini bilish uchun — o'zi hech qachon eager yuklanmaydi
    private Boolean audioBormi = false;
}
