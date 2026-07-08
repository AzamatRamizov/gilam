package com.example.gilam888.Repository;

import com.example.gilam888.Entity.Jadval;
import com.example.gilam888.Entity.Shartnoma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShartnomaRepository extends JpaRepository<Shartnoma, Long> {
    @Query("""
        SELECT s FROM Shartnoma s
        JOIN s.jadvalList j
        WHERE j = :jadval
    """)
    Optional<Shartnoma> findByJadvalListContaining(@Param("jadval") Jadval jadval);

    @Query("""
        SELECT s FROM Shartnoma s
        WHERE s.sotibOlinganSana >= :from
          AND s.sotibOlinganSana <= :to
        ORDER BY s.sotibOlinganSana DESC
    """)
    List<Shartnoma> findBySotibOlinganSanaBetween(@Param("from") String from, @Param("to") String to);
    @Query("""
        SELECT j FROM Jadval j
        WHERE j.holat = 'tulanmagan'
          AND j.sana < :hozir
        ORDER BY j.sana ASC
    """)
    List<Jadval> findOverdueUnpaid(@Param("hozir") LocalDateTime hozir);
    @Query("SELECT COALESCE(SUM(s.summa), 0) FROM Shartnoma s")
    long sumUmumiySumma();

    @Query("SELECT COUNT(s) FROM Shartnoma s WHERE s.status = :status")
    long countByStatus(@Param("status") String status);

    // Har bir shartnomaning ENG OXIRGI (max sana) jadvali olinadi,
    // agar o'sha jadval hali tulanmagan va sanasi o'tib ketgan bo'lsa — shartnoma "muddati o'tgan" hisoblanadi
    @Query("""
        SELECT COUNT(DISTINCT s) FROM Shartnoma s
        JOIN s.jadvalList j
        WHERE j.sana = (
            SELECT MAX(j2.sana) FROM Shartnoma s2
            JOIN s2.jadvalList j2
            WHERE s2 = s
        )
        AND j.holat = 'tulanmagan'
        AND j.sana < :hozir
    """)
    long countMuddatiOtganShartnoma(@Param("hozir") LocalDateTime hozir);

    @Query("""
    SELECT s FROM Shartnoma s
    WHERE (s.mahsulot   IS NULL OR TRIM(s.mahsulot)   = '')
       OR (s.tannarx    IS NULL OR TRIM(s.tannarx)    = '')
       OR (s.joylashuv  IS NULL OR TRIM(s.joylashuv)  = '')
    ORDER BY s.id DESC
""")
    List<Shartnoma> findShartnomaWithMissingInfo();

}
