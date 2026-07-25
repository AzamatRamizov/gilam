package com.example.gilam888.Repository;

import com.example.gilam888.Entity.SmsBalans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SmsBalansRepository extends JpaRepository<SmsBalans, Long> {

    // Balansdan atomik ayirish — bir vaqtda bir nechta SMS ketsa ham
    // (scheduler + qo'lda to'lov) balans noto'g'ri hisoblanmasligi uchun
    // read-modify-save o'rniga bitta UPDATE ishlatiladi.
    @Modifying
    @Transactional
    @Query("UPDATE SmsBalans b SET b.balans = b.balans - :narx WHERE b.id = :id")
    int ayirish(@Param("id") long id, @Param("narx") long narx);

    // Balansga atomik qo'shish (hisob to'ldirilganda)
    @Modifying
    @Transactional
    @Query("""
            UPDATE SmsBalans b
            SET b.balans = b.balans + :summa,
                b.jamiToldirilgan = COALESCE(b.jamiToldirilgan, 0) + :summa
            WHERE b.id = :id
            """)
    int toldirish(@Param("id") long id, @Param("summa") long summa);
}
