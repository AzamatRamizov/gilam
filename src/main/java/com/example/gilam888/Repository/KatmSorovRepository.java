package com.example.gilam888.Repository;

import com.example.gilam888.Entity.KatmSorov;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KatmSorovRepository extends JpaRepository<KatmSorov, Long> {

    List<KatmSorov> findAllByOrderByIdDesc();

    List<KatmSorov> findByHolatOrderByIdDesc(String holat);

    long countByHolat(String holat);

    Optional<KatmSorov> findBySotuvId(Long sotuvId);
}
