package com.example.gilam888.Repository;

import com.example.gilam888.Entity.Magazin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MagazinRepository extends JpaRepository<Magazin, Long> {
    Optional<Magazin> findByNomi(String nomi);
}
