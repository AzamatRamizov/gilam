package com.example.gilam888.Repository;

import com.example.gilam888.Entity.Sozlama;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SozlamaRepository extends JpaRepository<Sozlama, Long> {
    Optional<Sozlama> findByKalit(String kalit);
}
