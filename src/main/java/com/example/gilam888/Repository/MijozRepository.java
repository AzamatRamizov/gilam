package com.example.gilam888.Repository;

import com.example.gilam888.Entity.Mijoz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MijozRepository extends JpaRepository<Mijoz, Long> {
    Optional<Mijoz> findByPassport(String passport);

    // Telegram botdagi "To'lov jadvalim" tugmasi uchun: mijozning tel1/tel2/tel3
    // raqamlaridan biri berilgan raqamning oxirgi 9 ta raqamiga mos kelsa topadi.
    // tel maydonlari turli formatda saqlanishi mumkin (+998 90 123-45-67, 901234567, ...),
    // shu sababli avval barcha raqam bo'lmagan belgilarni olib tashlab solishtiramiz.
    @Query(value = """
            SELECT * FROM mijoz m
            WHERE regexp_replace(coalesce(m.tel1, ''), '[^0-9]', '', 'g') LIKE CONCAT('%', :oxirgi9)
               OR regexp_replace(coalesce(m.tel2, ''), '[^0-9]', '', 'g') LIKE CONCAT('%', :oxirgi9)
               OR regexp_replace(coalesce(m.tel3, ''), '[^0-9]', '', 'g') LIKE CONCAT('%', :oxirgi9)
            """, nativeQuery = true)
    List<Mijoz> findByTelefonOxiri(@Param("oxirgi9") String oxirgi9);

    // Mijozlar ro'yxati uchun yengil projection — entity (va u bilan passport/KATM
    // rasm baytlari) yuklanmaydi. Heap tejash uchun.
    @Query("SELECT m.id, m.ism, m.familiya, m.sharif, m.tuman, m.manzil, m.tel1, m.passport "
            + "FROM Mijoz m ORDER BY m.id DESC")
    List<Object[]> findAllForRoyxat();
}
