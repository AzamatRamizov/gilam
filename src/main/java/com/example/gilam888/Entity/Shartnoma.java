package com.example.gilam888.Entity;

import com.example.gilam888.Configurations.AbstractLongEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Shartnoma extends AbstractLongEntity {
    private long summa;
    private long muddat;
    private String izoh;
    private String mahsulot;
    private String tannarx;
    private String sotibOlinganSana;
    private String joylashuv;

    // Shartnoma qaysi do'konda (Magazin.id) sotilgani.
    // Ilgari forma yuborsa ham bazaga saqlanmay qolardi (faqat PaymentHistory.dokonId ga yozilardi).
    // Eski yozuvlarda NULL bo'lishi mumkin — ular dashboard "Kamchiliklar" kartasida ko'rsatiladi.
    private Long dokonId;

    @ManyToOne
    private Mijoz mijoz;

    private String status;
    // Yangi: undiruvga topshirilganda sabab va vaqtni saqlash uchun
    private String undiruvSababi;
    private LocalDateTime undiruvVaqti;

    @ManyToMany(fetch = FetchType.LAZY)
    List<Jadval> jadvalList;

    @ManyToOne(fetch = FetchType.LAZY)
    private FaylBayt kafolat;

    @ManyToOne(fetch = FetchType.LAZY)
    private FaylBayt kafolat2;
}
