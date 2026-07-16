package com.example.gilam888.Service;

import com.example.gilam888.Entity.AmalTarixi;
import com.example.gilam888.Entity.Users;
import com.example.gilam888.Repository.AmalTarixiRepository;
import com.example.gilam888.Repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Tizimdagi barcha muhim amallarni AmalTarixi jadvaliga yozadi va
// "So'ngi amallar" sahifasi uchun filtrlangan/tartiblangan ro'yxatni qaytaradi.
@Slf4j
@Service
public class AmalService {

    // Frontenddan keladigan sort maydonlarining oq ro'yxati —
    // ixtiyoriy qiymat yuborib xato/inyeksiya qilishning oldini oladi
    private static final Set<String> RUXSAT_SORT = Set.of("vaqt", "userFish", "amalTuri", "summa", "id");

    private final AmalTarixiRepository amalTarixiRepository;
    private final UsersRepository usersRepository;

    public AmalService(AmalTarixiRepository amalTarixiRepository, UsersRepository usersRepository) {
        this.amalTarixiRepository = amalTarixiRepository;
        this.usersRepository = usersRepository;
    }

    // ─────────────────────────── YOZISH ───────────────────────────

    // Amalni tarixga yozadi. Kim bajarganini SecurityContext'dan (JWT orqali kirgan user) oladi.
    // MUHIM: log yozishdagi xato asosiy amalni (to'lov, mijoz qo'shish...) buzmasligi kerak,
    // shuning uchun barcha istisnolar yutiladi va faqat log'ga yoziladi.
    public void log(String amalTuri, String tavsif, Long mijozId, Long shartnomaId, Long summa) {
        try {
            AmalTarixi amal = new AmalTarixi();
            amal.setAmalTuri(amalTuri);
            amal.setTavsif(tavsif);
            amal.setMijozId(mijozId);
            amal.setShartnomaId(shartnomaId);
            amal.setSumma(summa);
            amal.setVaqt(LocalDateTime.now());

            Users user = hozirgiUser();
            if (user != null) {
                amal.setUserId(user.getId());
                amal.setUserFish(user.getFish() != null && !user.getFish().isBlank() ? user.getFish() : user.getUsername());
                amal.setUsername(user.getUsername());
            } else {
                amal.setUserFish("Tizim");
            }

            amalTarixiRepository.save(amal);
        } catch (Exception e) {
            log.error("Amalni tarixga yozishda xato (amalTuri={}): {}", amalTuri, e.getMessage());
        }
    }

    // Qisqartma — bog'liq yozuvlarsiz amallar uchun (hodim, do'kon va h.k.)
    public void log(String amalTuri, String tavsif) {
        log(amalTuri, tavsif, null, null, null);
    }

    // JWT orqali kirgan hozirgi foydalanuvchini topadi.
    // UserService.loadUserByUsername Spring'ning User obyektini qaytargani uchun
    // principal'dan faqat username olinadi va bazadan to'liq Users yuklanadi.
    private Users hozirgiUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null) return null;
            return usersRepository.findByUsername(auth.getName()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    // ─────────────────────────── O'QISH ───────────────────────────

    // "So'ngi amallar" sahifasi uchun filtrlangan ro'yxat.
    //  - userId    : faqat shu hodim bajargan amallar
    //  - amalTuri  : faqat shu turdagi amallar (TOLOV, QONGIROQ, ...)
    //  - sana      : faqat shu kun (boshi/oxiri'dan ustun)
    //  - boshi/oxiri: ixtiyoriy sana oralig'i
    //  - sort/dir  : universal tartiblash (vaqt, userFish, amalTuri, summa)
    public Map<String, Object> getAmallar(Long userId, String amalTuri, LocalDate sana,
                                          LocalDate boshi, LocalDate oxiri,
                                          int page, int size, String sort, String dir) {
        LocalDateTime dan;
        LocalDateTime gacha;
        if (sana != null) {
            dan = sana.atStartOfDay();
            gacha = sana.atTime(23, 59, 59);
        } else {
            dan = boshi != null ? boshi.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
            gacha = oxiri != null ? oxiri.atTime(23, 59, 59) : LocalDateTime.of(2100, 1, 1, 0, 0);
        }

        String sortMaydon = RUXSAT_SORT.contains(sort) ? sort : "vaqt";
        Sort.Direction yonalish = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        // Bir xil qiymatli qatorlar barqaror tartibda turishi uchun id qo'shimcha kalit
        Sort tartib = Sort.by(yonalish, sortMaydon).and(Sort.by(Sort.Direction.DESC, "id"));

        if (amalTuri != null && amalTuri.isBlank()) amalTuri = null;

        Page<AmalTarixi> natija = amalTarixiRepository.filter(
                userId, amalTuri, dan, gacha,
                PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 200), tartib));

        Map<String, Object> javob = new LinkedHashMap<>();
        javob.put("content", natija.getContent());
        javob.put("page", natija.getNumber());
        javob.put("totalPages", natija.getTotalPages());
        javob.put("totalElements", natija.getTotalElements());
        return javob;
    }

    // Filtr dropdown'i uchun hodimlar ro'yxati (faqat kerakli maydonlar —
    // parol kabi ma'lumotlar frontendga chiqmasligi uchun)
    public List<Map<String, Object>> getAmalUserlar() {
        return usersRepository.findAll().stream()
                .map(u -> {
                    Map<String, Object> m = new LinkedHashMap<String, Object>();
                    m.put("id", u.getId());
                    m.put("fish", u.getFish() != null && !u.getFish().isBlank() ? u.getFish() : u.getUsername());
                    m.put("username", u.getUsername());
                    return m;
                })
                .toList();
    }
}
