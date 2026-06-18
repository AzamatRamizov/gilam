package com.example.gilam888.Entity;

import com.example.gilam888.Configurations.AbstractLongEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class FaylBayt extends AbstractLongEntity {
    private String originalNomi;
    private double hajmiFayl;
    private String contentTypeFayl;
    private byte[] bayt;
}
