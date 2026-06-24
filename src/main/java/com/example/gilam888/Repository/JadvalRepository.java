package com.example.gilam888.Repository;

import com.example.gilam888.Entity.Jadval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JadvalRepository extends JpaRepository<Jadval, Long> {
    @Query("""
        SELECT j FROM Jadval j
        WHERE j.holat = 'tulanmagan'
          AND j.sana >= :oyBoshi
          AND j.sana < :oyOxiri
        ORDER BY j.sana ASC
    """)
    List<Jadval> findCurrentMonthUnpaid(
            @Param("oyBoshi") LocalDateTime oyBoshi,
            @Param("oyOxiri") LocalDateTime oyOxiri
    );

    @Query("SELECT j FROM Jadval j WHERE j.tulovSana >= :boshi AND j.tulovSana < :oxiri")
    List<Jadval> findTodayPayments(
            @Param("boshi") LocalDateTime boshi,
            @Param("oxiri") LocalDateTime oxiri
    );
}
