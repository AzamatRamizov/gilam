package com.example.gilam888.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QarzdorlarDto {
    private Long mijozId;
    private String ism;
    private String familiya;
    private String tel1;
    private String tel2;
    private String tel3;
    private Long shartnomaId;
    private long qolganQarz;
    private String keyingiTolovSana; // ISO_LOCAL_DATE_TIME string
    private long muddatiOtganKun;
}
