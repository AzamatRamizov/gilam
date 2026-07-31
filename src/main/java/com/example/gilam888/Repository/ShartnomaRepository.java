package com.example.gilam888.Repository;

import com.example.gilam888.Entity.Jadval;
import com.example.gilam888.Entity.Shartnoma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

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
       OR (s.sotibOlinganSana IS NULL OR TRIM(s.sotibOlinganSana) = '')
       OR (s.dokonId IS NULL)
    ORDER BY s.id DESC
""")
    List<Shartnoma> findShartnomaWithMissingInfo();

    List<Shartnoma> findByStatus(String status);

    List<Shartnoma> findByMijoz_Id(Long mijozId);

    // Statistika uchun yengil projection — butun entity yuklanmaydi (heap tejash)
    @Query("SELECT s.sotibOlinganSana, s.createdTime, s.summa FROM Shartnoma s")
    List<Object[]> findAllForStatistika();

    // Do'konlar bo'yicha statistika uchun yengil projection (entity yuklanmaydi)
    @Query("SELECT s.dokonId, s.sotibOlinganSana, s.createdTime, s.summa, s.tannarx, s.oldindanTulov FROM Shartnoma s")
    List<Object[]> findAllForDokonStat();

    // Kalendar statistikasi uchun — mijoz ma'lumoti bilan, lekin entity yuklamasdan
    @Query("SELECT s.id, s.sotibOlinganSana, s.createdTime, s.summa, s.muddat, s.status, "
            + "m.ism, m.familiya, m.sharif, m.tel1 "
            + "FROM Shartnoma s LEFT JOIN s.mijoz m")
    List<Object[]> findAllForKalendar();

    // Barcha shartnoma ID'lari — entity yuklamasdan (checkAllShartnoma uchun).
    // Ilgari findAll() ishlatilar edi: barcha Shartnoma + Mijoz + passport rasm baytlari
    // xotiraga tushib "Java heap space" xatosini berardi.
    @Query("SELECT s.id FROM Shartnoma s")
    List<Long> findAllIds();



    // Sanasi bo'sh shartnomalar + ularning ENG BIRINCHI jadval sanasi (entity yuklanmaydi).
    // LEFT JOIN — jadvali umuman yo'q shartnomalar ham ko'rinsin (MIN = null bo'ladi).
    @Query("""
        SELECT s.id, MIN(j.sana) FROM Shartnoma s
        LEFT JOIN s.jadvalList j
        WHERE s.sotibOlinganSana IS NULL
           OR TRIM(s.sotibOlinganSana) = ''
           OR LOWER(TRIM(s.sotibOlinganSana)) = 'null'
        GROUP BY s.id
    """)
    List<Object[]> findSanasizShartnomalar();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Shartnoma s SET s.sotibOlinganSana = :sana WHERE s.id = :id")
    void updateSotibOlinganSana(@Param("id") Long id, @Param("sana") String sana);
}