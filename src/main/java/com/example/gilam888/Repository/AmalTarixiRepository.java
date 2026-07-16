package com.example.gilam888.Repository;

import com.example.gilam888.Entity.AmalTarixi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AmalTarixiRepository extends JpaRepository<AmalTarixi, Long> {

    // Universal filtrlash: user bo'yicha, amal turi bo'yicha va sana oralig'i bo'yicha.
    // userId/amalTuri null bo'lsa — o'sha filtr qo'llanmaydi.
    // boshi/oxiri har doim to'ldirilgan holda keladi (AmalService default qiymat beradi),
    // shunda PostgreSQL'da null-parametr tip muammosi bo'lmaydi.
    @Query("""
            SELECT a FROM AmalTarixi a
            WHERE (:userId IS NULL OR a.userId = :userId)
              AND (:amalTuri IS NULL OR a.amalTuri = :amalTuri)
              AND a.vaqt >= :boshi
              AND a.vaqt <= :oxiri
            """)
    Page<AmalTarixi> filter(@Param("userId") Long userId,
                            @Param("amalTuri") String amalTuri,
                            @Param("boshi") LocalDateTime boshi,
                            @Param("oxiri") LocalDateTime oxiri,
                            Pageable pageable);
}
