package com.example.gilam888.Repository;

import com.example.gilam888.Entity.Jadval;
import com.example.gilam888.Entity.Shartnoma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

}
