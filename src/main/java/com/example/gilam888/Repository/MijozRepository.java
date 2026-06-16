package com.example.gilam888.Repository;

import com.example.gilam888.Entity.Mijoz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MijozRepository extends JpaRepository<Mijoz, Long> {
    Optional<Mijoz> findByPassport(String passport);

}
