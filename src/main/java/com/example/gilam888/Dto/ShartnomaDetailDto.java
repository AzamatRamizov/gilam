package com.example.gilam888.Dto;

import com.example.gilam888.Entity.Jadval;
import com.example.gilam888.Entity.Mijoz;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ShartnomaDetailDto {
    private long id;
    private long summa;
    private long muddat;
    private String status;
    private String izoh;

    private Mijoz mijoz;
    private List<Jadval> jadvalList;

    private Long kafolat;      // FaylBayt id (photoUrl frontendda /admin/fayl/{id} qiladi)
    private Long kafolat2;

    private MahsulotDto mahsulot;
    private List<PaymentDto> tulovTarixi;
}
