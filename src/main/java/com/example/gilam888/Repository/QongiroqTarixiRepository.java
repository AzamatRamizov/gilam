package com.example.gilam888.Repository;

import com.example.gilam888.Entity.QongiroqTarixi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface QongiroqTarixiRepository extends JpaRepository<QongiroqTarixi, Long> {
    List<QongiroqTarixi> findByMijoz_IdOrderBySanaDesc(Long mijozId);

    // Bugungi eslatmalar uchun nomzodlar: bugun va'da qilingan va natijasi "Va'da berdi" bo'lgan qo'ng'iroqlar
    List<QongiroqTarixi> findByVadaSanaAndNatija(LocalDate vadaSana, String natija);

    // Shu mijoz uchun berilgan vaqtdan keyin yana qo'ng'iroq bo'lganmi (ya'ni bu eng oxirgi qo'ng'iroqmi)
    boolean existsByMijoz_IdAndSanaAfter(Long mijozId, LocalDateTime sana);
}
