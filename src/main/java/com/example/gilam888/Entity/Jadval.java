package com.example.gilam888.Entity;

import com.example.gilam888.Configurations.AbstractLongEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Jadval extends AbstractLongEntity {
    private long summa;
    private LocalDateTime sana;
    private String holat;
    private long tulangan;
    private String turi;
    private LocalDateTime tulovSana;
    private long dokonId;
}
