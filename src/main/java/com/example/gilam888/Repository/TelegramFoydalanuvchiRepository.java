package com.example.gilam888.Repository;

import com.example.gilam888.Entity.TelegramFoydalanuvchi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TelegramFoydalanuvchiRepository extends JpaRepository<TelegramFoydalanuvchi, Long> {
    Optional<TelegramFoydalanuvchi> findByTelefonRaqami(String telefonRaqami);
    Optional<TelegramFoydalanuvchi> findByChatId(Long chatId);
}
