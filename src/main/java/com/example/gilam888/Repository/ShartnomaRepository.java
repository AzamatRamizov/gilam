package com.example.gilam888.Repository;

import com.example.gilam888.Entity.Jadval;
import com.example.gilam888.Entity.Shartnoma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShartnomaRepository extends JpaRepository<Shartnoma, Long> {
    @Query("""
        SELECT s FROM Shartnoma s
        JOIN s.jadvalList j
        WHERE j = :jadval
    """)
    Optional<Shartnoma> findByJadvalListContaining(@Param("jadval") Jadval jadval);
}
