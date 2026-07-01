package com.example.gilam888.Repository;

import com.example.gilam888.Entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<PaymentHistory, Long> {
    @Query("SELECT p FROM PaymentHistory p WHERE p.sana >= :boshi AND p.sana < :oxiri")
    List<PaymentHistory> findTodayPayments(
            @Param("boshi") LocalDateTime boshi,
            @Param("oxiri") LocalDateTime oxiri
    );

    @Query("SELECT p FROM PaymentHistory p WHERE p.jadvalId = :jadvalId AND p.sana >= :boshi AND p.sana < :oxiri")
    List<PaymentHistory> findTodayPaymentsByJadval(
            @Param("jadvalId") long jadvalId,
            @Param("boshi") LocalDateTime boshi,
            @Param("oxiri") LocalDateTime oxiri
    );

    @Query("SELECT p FROM PaymentHistory p WHERE p.dokonId = :dokonId AND p.sana >= :boshi AND p.sana < :oxiri")
    List<PaymentHistory> findTodayPaymentsByDokon(
            @Param("dokonId") Long dokonId,
            @Param("boshi") LocalDateTime boshi,
            @Param("oxiri") LocalDateTime oxiri
    );
}
