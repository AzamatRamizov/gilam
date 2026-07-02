package com.example.gilam888.Entity;

import com.example.gilam888.Configurations.AbstractLongEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @ManyToOne
    private Mijoz mijoz;

    private String status;

    @ManyToMany
    List<Jadval> jadvalList;

    @ManyToOne
    private FaylBayt kafolat;
    @ManyToOne
    private FaylBayt kafolat2;
}
