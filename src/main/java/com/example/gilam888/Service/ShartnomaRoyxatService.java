package com.example.gilam888.Service;

import com.example.gilam888.Dto.ShartnomaRoyxat;
import com.example.gilam888.Dto.ShartnomaSahifaDto;
import com.example.gilam888.Dto.ShartnomaStatDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Shartnomalar ro'yxati uchun alohida servis.
 *
 * NEGA ALOHIDA VA NEGA ENTITY YUKLANMAYDI:
 * Ilgari ro'yxat `shartnomaRepository.findAll()` orqali olinardi. U holda:
 *   Shartnoma (barchasi) -> Mijoz (EAGER) -> passportRasm + katm (EAGER, byte[] rasm!)
 * ya'ni har bir shartnoma bilan birga 2 ta rasmning to'liq baytlari xotiraga tushardi.
 * Bir necha ming shartnomada bu OutOfMemoryError: Java heap space beradi.
 *
 * Endi:
 *   1) @ManyToOne larning hammasi LAZY qilindi (rasm baytlari umuman o'qilmaydi);
 *   2) entity emas, faqat kerakli ustunlar (projection) o'qiladi;
 *   3) natija sahifalanadi (LIMIT/OFFSET) — bir vaqtda faqat 1 sahifa xotirada bo'ladi;
 *   4) qidiruv/filtr/tartiblash bazada bajariladi (brauzerga butun baza yuborilmaydi).
 */
@Service
public class ShartnomaRoyxatService {

    /** Bitta sahifada ruxsat etilgan maksimal qatorlar soni (himoya chegarasi) */
    private static final int MAX_SIZE = 200;

    @PersistenceContext
    private EntityManager em;

    /* ───────────────────────── Yordamchilar ───────────────────────── */

    /** URL dagi holat nomini bazadagi status qiymatiga aylantiradi. null = filtrsiz (barchasi) */
    private static String statusKodi(String holat) {
        if (holat == null || holat.isBlank()) return null;
        return switch (holat.trim().toLowerCase()) {
            case "faol", "ochiq", "active" -> "ochiq";
            case "yopilgan", "yopiq", "closed" -> "yopilgan";
            case "undiruv", "undiruvda" -> "undiruv";
            default -> null;                       // "barchasi" va noma'lum qiymatlar
        };
    }

    /** Frontend ustun nomini xavfsiz JPQL ifodasiga aylantiradi (SQL injection'dan himoya) */
    private static String sortIfoda(String sort) {
        if (sort == null) return "s.id";
        return switch (sort) {
            case "fish" -> "m.familiya";
            case "tel" -> "m.tel1";
            case "muddat" -> "s.muddat";
            case "summa" -> "s.summa";
            case "status" -> "s.status";
            case "sana" -> "s.sotibOlinganSana";
            default -> "s.id";
        };
    }

    private static long l(Object o) {
        return o instanceof Number n ? n.longValue() : 0L;
    }

    private static String s(Object o) {
        return o == null ? null : o.toString();
    }

    /* ───────────────────────── Asosiy metod ───────────────────────── */

    @Transactional(readOnly = true)
    public ShartnomaSahifaDto royxat(String holat, String q, int page, int size, String sort, String dir) {

        String status = statusKodi(holat);
        String qidiruv = (q == null) ? "" : q.trim().toLowerCase();

        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > MAX_SIZE) size = MAX_SIZE;

        // ── WHERE qismi ──
        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        if (status != null) {
            where.append(" AND s.status = :status ");
        }
        if (!qidiruv.isEmpty()) {
            where.append("""
                     AND ( LOWER(CONCAT(COALESCE(m.familiya,''), ' ', COALESCE(m.ism,''), ' ', COALESCE(m.sharif,''))) LIKE :q
                        OR COALESCE(m.tel1,'') LIKE :q
                        OR COALESCE(m.tel2,'') LIKE :q
                        OR COALESCE(m.tel3,'') LIKE :q
                        OR CAST(s.id AS string) LIKE :q )
                    """);
        }

        // ── 1) Jami soni va summasi (entity yuklanmaydi) ──
        Query cq = em.createQuery(
                "SELECT COUNT(s.id), COALESCE(SUM(s.summa), 0) FROM Shartnoma s LEFT JOIN s.mijoz m " + where);
        bogla(cq, status, qidiruv);
        Object[] hisob = (Object[]) cq.getSingleResult();
        long jami = l(hisob[0]);
        long jamiSumma = l(hisob[1]);

        int pages = (int) Math.max(1, Math.ceil(jami / (double) size));
        if (page > pages) page = pages;

        // ── 2) Faqat shu sahifadagi qatorlar ──
        boolean desc = !"asc".equalsIgnoreCase(dir);
        String jpql = "SELECT s.id, m.ism, m.familiya, m.sharif, m.tel1, "
                + "s.summa, s.muddat, s.status, s.undiruvSababi, s.sotibOlinganSana "
                + "FROM Shartnoma s LEFT JOIN s.mijoz m "
                + where
                + " ORDER BY " + sortIfoda(sort) + (desc ? " DESC" : " ASC") + ", s.id DESC";

        Query dq = em.createQuery(jpql);
        bogla(dq, status, qidiruv);
        dq.setFirstResult((page - 1) * size);
        dq.setMaxResults(size);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dq.getResultList();

        List<ShartnomaRoyxat> items = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            ShartnomaRoyxat dto = new ShartnomaRoyxat();
            dto.setId(l(r[0]));
            dto.setFish(fish(s(r[1]), s(r[2]), s(r[3])));
            dto.setTel(s(r[4]));
            dto.setSumma(l(r[5]));
            dto.setMuddat(l(r[6]));
            dto.setStatus(s(r[7]));
            dto.setSabab(s(r[8]));
            dto.setSana(s(r[9]));
            items.add(dto);
        }

        return new ShartnomaSahifaDto(items, jami, jamiSumma, page, size, pages, statistika());
    }

    private void bogla(Query query, String status, String qidiruv) {
        if (status != null) query.setParameter("status", status);
        if (!qidiruv.isEmpty()) query.setParameter("q", "%" + qidiruv + "%");
    }

    private static String fish(String familiya, String ism, String sharif) {
        String natija = ((familiya == null ? "" : familiya) + " "
                + (ism == null ? "" : ism) + " "
                + (sharif == null ? "" : sharif)).replaceAll("\\s+", " ").trim();
        return natija.isEmpty() ? "—" : natija;
    }

    /* ───────────────── Tab / karta statistikasi ───────────────── */

    /**
     * Status kesimida soni va summasi — bitta GROUP BY so'rovi.
     * Tanlangan tabga bog'liq emas: kartalar va tab badge'lari doim umumiy rasmni ko'rsatadi.
     */
    @Transactional(readOnly = true)
    public ShartnomaStatDto statistika() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createQuery(
                "SELECT s.status, COUNT(s.id), COALESCE(SUM(s.summa), 0) FROM Shartnoma s GROUP BY s.status")
                .getResultList();

        ShartnomaStatDto st = new ShartnomaStatDto();
        for (Object[] r : rows) {
            String status = s(r[0]);
            long soni = l(r[1]);
            long summa = l(r[2]);

            st.setHammasi(st.getHammasi() + soni);
            st.setHammasiSumma(st.getHammasiSumma() + summa);

            if ("ochiq".equals(status)) {
                st.setFaol(soni);
                st.setFaolSumma(summa);
            } else if ("yopilgan".equals(status)) {
                st.setYopilgan(soni);
                st.setYopilganSumma(summa);
            } else if ("undiruv".equals(status)) {
                st.setUndiruv(soni);
                st.setUndiruvSumma(summa);
            }
        }
        return st;
    }
}
