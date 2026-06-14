package com.example.gilam888.Entity;

import com.example.gilam888.Configurations.AbstractLongEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Mijoz extends AbstractLongEntity {
    private String ism;
    private String familiya;
    private String sharif;
    private String tuman;
    private String manzil;
    private String tel;
    private String passport;
}
