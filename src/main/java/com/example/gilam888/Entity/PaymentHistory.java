package com.example.gilam888.Entity;

import com.example.gilam888.Configurations.AbstractLongEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class PaymentHistory extends AbstractLongEntity {
    private long summa;
    private long jadvalId;
    private LocalDateTime sana;
    private String turi;
    private Long dokonId;
}
