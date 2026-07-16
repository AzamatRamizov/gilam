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
public class TelegramFoydalanuvchi extends AbstractLongEntity {
    // +998901234567 formatda saqlanadi
    private String telefonRaqami;
    private Long chatId;
    private String telegramUsername;
    private String ismi;
}
