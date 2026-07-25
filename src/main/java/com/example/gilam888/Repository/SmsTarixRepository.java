package com.example.gilam888.Repository;

import com.example.gilam888.Entity.SmsTarix;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SmsTarixRepository extends JpaRepository<SmsTarix, Long> {

    // Universal filtrlash: turi (YANGI_SHARTNOMA/TOLOV/ESLATMA), holat (YUBORILDI/XATO)
    // va sana oralig'i. turi/holat null bo'lsa — o'sha filtr qo'llanmaydi.
    // boshi/oxiri har doim to'ldirilgan holda keladi (SmsHisobService default beradi).
    @Query("""
            SELECT s FROM SmsTarix s
            WHERE (:turi IS NULL OR s.turi = :turi)
              AND (:holat IS NULL OR s.holat = :holat)
              AND s.vaqt >= :boshi
              AND s.vaqt <= :oxiri
            """)
    Page<SmsTarix> filter(@Param("turi") String turi,
                          @Param("holat") String holat,
                          @Param("boshi") LocalDateTime boshi,
                          @Param("oxiri") LocalDateTime oxiri,
                          Pageable pageable);

    // Berilgan oraliqda muvaffaqiyatli yuborilgan SMSlarga sarflangan jami summa
    @Query("""
            SELECT COALESCE(SUM(s.narx), 0) FROM SmsTarix s
            WHERE s.holat = 'YUBORILDI' AND s.vaqt >= :boshi AND s.vaqt <= :oxiri
            """)
    long sarflanganSumma(@Param("boshi") LocalDateTime boshi, @Param("oxiri") LocalDateTime oxiri);

    // Berilgan oraliqda muvaffaqiyatli yuborilgan SMSlar soni
    @Query("""
            SELECT COUNT(s) FROM SmsTarix s
            WHERE s.holat = 'YUBORILDI' AND s.vaqt >= :boshi AND s.vaqt <= :oxiri
            """)
    long yuborilganSoni(@Param("boshi") LocalDateTime boshi, @Param("oxiri") LocalDateTime oxiri);

    // Xato bilan tugagan (yuborilmagan) SMSlar soni — jami
    long countByHolat(String holat);
}
