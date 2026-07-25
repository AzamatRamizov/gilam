package com.example.gilam888.Service;

import com.example.gilam888.Configurations.ApiResponse;
import com.example.gilam888.Configurations.TokenGenerator;
import com.example.gilam888.Dto.*;
import com.example.gilam888.Entity.*;
import com.example.gilam888.Repository.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final MijozRepository mijozRepository;
    private final FaylBaytRepository faylBaytRepository;
    private final ShartnomaRepository shartnomaRepository;
    private final MagazinRepository magazinRepository;
    private final TokenGenerator tokenGenerator;
    private final JadvalRepository jadvalRepository;
    private final PaymentRepository paymentRepository;
    private final TelegramXabarService telegramXabarService;
    private final SmsXabarService smsXabarService;
    private final AmalService amalService;

    public AdminService(UsersRepository usersRepository, PasswordEncoder passwordEncoder, MijozRepository mijozRepository, FaylBaytRepository faylBaytRepository, ShartnomaRepository shartnomaRepository, MagazinRepository magazinRepository, TokenGenerator tokenGenerator, JadvalRepository jadvalRepository, PaymentRepository paymentRepository, TelegramXabarService telegramXabarService, SmsXabarService smsXabarService, AmalService amalService) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.mijozRepository = mijozRepository;
        this.faylBaytRepository = faylBaytRepository;
        this.shartnomaRepository = shartnomaRepository;
        this.magazinRepository = magazinRepository;
        this.tokenGenerator = tokenGenerator;
        this.jadvalRepository = jadvalRepository;
        this.paymentRepository = paymentRepository;
        this.telegramXabarService = telegramXabarService;
        this.smsXabarService = smsXabarService;
        this.amalService = amalService;
    }

    public ApiResponse addHodim(Users user) {
        if(usersRepository.findByUsername(user.getUsername()).isPresent()){
            return new ApiResponse("Bu username allaqachon ishlatilgan",false);
        }
        Users users = new Users();
        users.setFish(user.getFish());
        users.setUsername(user.getUsername());
        users.setPassword(passwordEncoder.encode(user.getPassword()));
        users.setRole("hodim");
        users.setAddress(user.getAddress());
        users.setTel(user.getTel());
        users.setIzoh(user.getIzoh());
        usersRepository.save(users);
        amalService.log("HODIM", "Yangi hodim qo'shildi: " + users.getFish() + " (" + users.getUsername() + ")");
        return new ApiResponse("Hodim qo'shildi",true);
    }

    public ApiResponse deleteHodim(Long id) {
        String fish = usersRepository.findById(id).map(Users::getFish).orElse("#" + id);
        usersRepository.deleteById(id);
        amalService.log("HODIM", "Hodim o'chirildi: " + fish);
        return new ApiResponse("Hodim o'chirildi",true);
    }

    public ApiResponse editHodim(Users user) {
        if(usersRepository.findByUsername(user.getUsername()).isPresent()){
            Users users = usersRepository.findById(user.getId()).get();
            assert user.getPassword() != null;
            users.setUsername(user.getUsername());
            users.setAddress(user.getAddress());
            users.setTel(user.getTel());
            users.setIzoh(user.getIzoh());
            if(!user.getPassword().isEmpty()){
                users.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            usersRepository.save(users);
            amalService.log("HODIM", "Hodim tahrirlandi: " + users.getFish() + " (" + users.getUsername() + ")");
            return new ApiResponse("Hodim o'zgartirildi",true);
        }
        return new ApiResponse("Hodim topilmadi",false);
    }

    public ApiResponse addMijoz(MijozDataDto mijoz, MultipartFile passport, MultipartFile rasm2, MultipartFile kafolat, MultipartFile kafolat2) throws IOException {
        if(mijozRepository.findByPassport(mijoz.getPassport()).isPresent()){
            return new ApiResponse("Bu passport allaqachon ishlatilgan",false);
        }

//        -----------------------------------------------------------

        Mijoz mijoz1 = new Mijoz();
        mijoz1.setIsm(mijoz.getIsm());
        mijoz1.setFamiliya(mijoz.getFamiliya());
        mijoz1.setSharif(mijoz.getSharif());
        mijoz1.setTel1(mijoz.getTel1());
        mijoz1.setTel2(mijoz.getTel2());
        mijoz1.setTel3(mijoz.getTel3());
        mijoz1.setViloyat(mijoz.getViloyat());
        mijoz1.setTuman(mijoz.getTuman());
        mijoz1.setManzil(mijoz.getManzil());
        mijoz1.setMuljal(mijoz.getMuljal());
        mijoz1.setPassport(mijoz.getPassport());


        FaylBayt passportRasm = new FaylBayt();
        passportRasm.setOriginalNomi(passport.getOriginalFilename());
        passportRasm.setHajmiFayl(passport.getSize());
        passportRasm.setContentTypeFayl(passport.getContentType());
        passportRasm.setBayt(passport.getBytes());
        FaylBayt passportSave = faylBaytRepository.save(passportRasm);

        FaylBayt katm = new FaylBayt();
        katm.setOriginalNomi(rasm2.getOriginalFilename());
        katm.setHajmiFayl(rasm2.getSize());
        katm.setContentTypeFayl(rasm2.getContentType());
        katm.setBayt(rasm2.getBytes());
        FaylBayt katmSave = faylBaytRepository.save(katm);

        mijoz1.setPassportRasm(passportSave);
        mijoz1.setKatm(katmSave);
        Mijoz mijozSave = mijozRepository.save(mijoz1);

//        ___________________________________________________________

        Shartnoma shartnoma = new Shartnoma();
        shartnoma.setMijoz(mijozSave);
        shartnoma.setSumma(mijoz.getSumma());
        shartnoma.setStatus("ochiq");
        shartnoma.setMahsulot(mijoz.getAbout());
        shartnoma.setIzoh(mijoz.getIzoh());
        shartnoma.setMuddat(mijoz.getMuddat());
        // Shartnoma sanasi doim yyyy-MM-dd ko'rinishida saqlanadi.
        // (String.valueOf(null) "null" degan matn hosil qilardi — statistikada oy topilmasdi)
        shartnoma.setSotibOlinganSana(mijoz.getShartnomaSana() != null
                ? mijoz.getShartnomaSana().toLocalDate().toString()
                : null);
        shartnoma.setTannarx(mijoz.getTannarx());
        shartnoma.setJoylashuv(mijoz.getJoylashuv());
        // Sotilgan do'kon: formadan kelgan dokonId endi shartnomaning o'zida saqlanadi
        // (ilgari yo'qolib ketardi). 0 — tanlanmagan degani, NULL sifatida saqlaymiz.
        shartnoma.setDokonId(mijoz.getDokonId() > 0 ? Long.valueOf(mijoz.getDokonId()) : null);
        // DIQQAT: createdTime'ga @CreationTimestamp qo'yilgan — Hibernate uni yozuv
        // bazaga tushgan payt bilan avtomatik to'ldiradi. Bu yerda qo'lda qo'yilgan qiymat
        // e'tiborga olinmaydi, shuning uchun shartnoma sanasi faqat sotibOlinganSana'da turadi.
        long tulov=mijoz.getSumma()/mijoz.getMuddat();
        List<Jadval> jadvalList = new ArrayList<>();
        for (long i = 0; i < mijoz.getMuddat(); i++) {
            Jadval jadval = new Jadval();
            jadval.setSana(mijoz.getTulovSana().plusMonths(i));
            jadval.setSumma(tulov);
            jadval.setTulangan(0);
            jadval.setHolat("tulanmagan");
            jadval = jadvalRepository.save(jadval);
            jadvalList.add(jadval);
        }
        shartnoma.setJadvalList(jadvalList);
        FaylBayt kafolatRasm = new FaylBayt();
        kafolatRasm.setOriginalNomi(kafolat.getOriginalFilename());
        kafolatRasm.setHajmiFayl(kafolat.getSize());
        kafolatRasm.setContentTypeFayl(kafolat.getContentType());
        kafolatRasm.setBayt(kafolat.getBytes());
        FaylBayt kafolatSave = faylBaytRepository.save(kafolatRasm);

        FaylBayt kafolat2Rasm = new FaylBayt();
        kafolat2Rasm.setOriginalNomi(kafolat2.getOriginalFilename());
        kafolat2Rasm.setHajmiFayl(kafolat2.getSize());
        kafolat2Rasm.setContentTypeFayl(kafolat2.getContentType());
        kafolat2Rasm.setBayt(kafolat2.getBytes());
        FaylBayt kafolat2Save = faylBaytRepository.save(kafolat2Rasm);

        shartnoma.setKafolat(kafolatSave);
        shartnoma.setKafolat2(kafolat2Save);

        LocalDateTime localDateTime = LocalDateTime.now();
        shartnoma.setCreatedTime(localDateTime);

        shartnomaRepository.save(shartnoma);
        telegramXabarService.yangiShartnomaXabarYuborish(shartnoma);
        smsXabarService.yangiShartnomaXabarYuborish(shartnoma);

        amalService.log("MIJOZ_QOSHISH",
                "Yangi mijoz va shartnoma qo'shildi: " + mijozIsmi(mijozSave)
                        + (shartnoma.getMahsulot() != null ? ", mahsulot: " + shartnoma.getMahsulot() : ""),
                mijozSave.getId(), shartnoma.getId(), shartnoma.getSumma());

//        ------------------------------------

        return new ApiResponse("Mijoz qo'shildi",true);
    }

    public Object mijozlar() {

        List<MijozRoyxat> mijozRoyxats=new ArrayList<>();

        for (Mijoz mijoz : mijozRepository.findAll()) {
            MijozRoyxat mijozRoyxat = new MijozRoyxat();
            mijozRoyxat.setIsm(mijoz.getIsm());
            mijozRoyxat.setFamiliya(mijoz.getFamiliya());
            mijozRoyxat.setSharif(mijoz.getSharif());
            mijozRoyxat.setId(mijoz.getId());
            mijozRoyxat.setTel(mijoz.getTel1());
            mijozRoyxat.setTuman(mijoz.getTuman());
            mijozRoyxat.setManzil(mijoz.getManzil());
            mijozRoyxat.setPassport(mijoz.getPassport());
            mijozRoyxats.add(mijozRoyxat);
        }
        return mijozRoyxats;
    }

    public Object getShartnomaAll() {
        List<ShartnomaRoyxat> shartnomaRoyxats=new ArrayList<>();

        for (Shartnoma shartnoma : shartnomaRepository.findAll()) {
            ShartnomaRoyxat shartnomaRoyxat = new ShartnomaRoyxat();
            String fish= shartnoma.getMijoz().getIsm()+" " + shartnoma.getMijoz().getFamiliya()+" " + shartnoma.getMijoz().getSharif();
            shartnomaRoyxat.setFish(fish);
            shartnomaRoyxat.setId(shartnoma.getId());
            shartnomaRoyxat.setSumma(shartnoma.getSumma());
            shartnomaRoyxat.setMuddat(shartnoma.getMuddat());
            shartnomaRoyxat.setStatus(shartnoma.getStatus());
            shartnomaRoyxat.setTel(shartnoma.getMijoz().getTel1());
            shartnomaRoyxats.add(shartnomaRoyxat);
        }
        return shartnomaRoyxats;
    }

    public ShartnomaDetailDto shartnomaDetail(long id) {
        Shartnoma s = shartnomaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shartnoma topilmadi: " + id));

        List<Long> jadvalIds = s.getJadvalList().stream().map(Jadval::getId).toList();

        // 1) Eski to'lovlar — jadval bo'yicha bog'langan
        List<PaymentHistory> jadvalOrqali = jadvalIds.isEmpty()
                ? List.of()
                : paymentRepository.findByJadvalIdInOrderBySanaDesc(jadvalIds);

        // 2) Yangi to'lovlar — shartnoma bo'yicha to'g'ridan-to'g'ri bog'langan
        List<PaymentHistory> shartnomaOrqali = paymentRepository.findByShartnomaIdOrderBySanaAsc(s.getId());

        // Ikkalasini birlashtirib, id bo'yicha dublikatlarni olib tashlaymiz
        Map<Long, PaymentHistory> birlashgan = new LinkedHashMap<>();
        for (PaymentHistory p : jadvalOrqali) birlashgan.put(p.getId(), p);
        for (PaymentHistory p : shartnomaOrqali) birlashgan.put(p.getId(), p);

        List<PaymentDto> tulovTarixi = birlashgan.values().stream()
                .sorted(Comparator.comparing(PaymentHistory::getSana,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(p -> {
                    PaymentDto dto = new PaymentDto();
                    dto.setSana(String.valueOf(p.getSana()));
                    dto.setSumma(p.getSumma());
                    dto.setTuri(p.getTuri());
                    dto.setDokon(resolveDokonNomi(p.getDokonId()));
                    return dto;
                })
                .toList();

        MahsulotDto mahsulot = new MahsulotDto(
                0,
                s.getMahsulot(),
                s.getSotibOlinganSana(),
                s.getTannarx(),
                s.getJoylashuv(),
                s.getDokonId(),
                resolveDokonNomi(s.getDokonId())
        );

        return new ShartnomaDetailDto(
                s.getId(), s.getSumma(), s.getMuddat(), s.getStatus(), s.getIzoh(),
                s.getMijoz(), s.getJadvalList(),
                s.getKafolat()  != null ? s.getKafolat().getId()  : null,
                s.getKafolat2() != null ? s.getKafolat2().getId() : null,
                mahsulot, tulovTarixi
        );
    }

    public ShartnomaDetailDto updateMahsulot(MahsulotDto dto) {
        Shartnoma s = shartnomaRepository.findById(dto.getShartnomaId())
                .orElseThrow(() -> new RuntimeException("Shartnoma topilmadi: " + dto.getShartnomaId()));

        s.setMahsulot(dto.getNomi());
        s.setTannarx(dto.getNarx());              // String → String, parse shart emas
        // Bo'sh yuborilsa eski sana saqlanib qoladi (statistikadagi oy yo'qolmasligi uchun)
        if (dto.getSana() != null && !dto.getSana().isBlank()) {
            s.setSotibOlinganSana(dto.getSana().trim());
        }
        s.setJoylashuv(dto.getLokatsiya());
        // Sotilgan do'kon tahrirlanadi; 0/undefined kelsa NULL bo'ladi (belgilanmagan)
        s.setDokonId(dto.getDokonId() != null && dto.getDokonId() > 0 ? dto.getDokonId() : null);
        shartnomaRepository.save(s);

        amalService.log("SHARTNOMA_TAHRIR",
                "Shartnoma #" + s.getId() + " mahsulot ma'lumotlari tahrirlandi: " + dto.getNomi(),
                s.getMijoz() != null ? s.getMijoz().getId() : null, s.getId(), null);

        return shartnomaDetail(s.getId());
    }

    /**
     * Shartnoma muddatini o'zgartirish.
     * Umumiy summa yangi muddatga qayta bo'linadi, to'lov grafigi qayta tuziladi,
     * oldin to'langan summa yangi grafikga eng eski oydan boshlab (FIFO) taqsimlanadi.
     */
    @Transactional
    public ApiResponse muddatOzgartirish(Long shartnomaId, long yangiMuddat) {
        if (yangiMuddat < 1 || yangiMuddat > 120) {
            return new ApiResponse("Muddat noto'g'ri kiritildi (1 dan 120 oygacha bo'lishi kerak)", false);
        }

        Shartnoma s = shartnomaRepository.findById(shartnomaId).orElse(null);
        if (s == null) return new ApiResponse("Shartnoma topilmadi: " + shartnomaId, false);

        long eskiMuddat = s.getMuddat();
        if (eskiMuddat == yangiMuddat) {
            return new ApiResponse("Shartnoma muddati allaqachon " + yangiMuddat + " oy", false);
        }

        // Eski jadvallar (eng eski sana birinchi)
        List<Jadval> eski = s.getJadvalList() == null ? new ArrayList<>() : new ArrayList<>(s.getJadvalList());
        eski.sort(Comparator.comparing(Jadval::getSana, Comparator.nullsLast(Comparator.naturalOrder())));

        long jamiTulangan = eski.stream().mapToLong(Jadval::getTulangan).sum();

        // Yangi grafik boshlanish sanasi — eski grafikning birinchi to'lov sanasi
        LocalDateTime birinchiSana;
        if (!eski.isEmpty() && eski.get(0).getSana() != null) {
            birinchiSana = eski.get(0).getSana();
        } else if (s.getCreatedTime() != null) {
            birinchiSana = s.getCreatedTime().plusMonths(1);
        } else {
            birinchiSana = LocalDateTime.now().plusMonths(1);
        }

        // Oylik to'lov: qoldiq farq oxirgi oyga qo'shiladi (jami aynan summa bo'lishi uchun)
        long oylik = s.getSumma() / yangiMuddat;
        long qoldiqFarq = s.getSumma() - oylik * yangiMuddat;

        // Yangi jadval ro'yxati — imkon qadar eski qatorlar qayta ishlatiladi
        List<Jadval> yangiList = new ArrayList<>();
        for (int i = 0; i < yangiMuddat; i++) {
            Jadval j = i < eski.size() ? eski.get(i) : new Jadval();
            j.setSana(birinchiSana.plusMonths(i));
            j.setSumma(oylik + (i == yangiMuddat - 1 ? qoldiqFarq : 0));
            j.setTulangan(0);
            j.setHolat("tulanmagan");
            j.setTulovSana(null);
            yangiList.add(j);
        }

        // Muddat qisqargan bo'lsa — ortiqcha jadvallarning to'lov tarixini shartnomaga bog'lab qo'yamiz
        List<Jadval> ortiqcha = eski.size() > yangiMuddat
                ? new ArrayList<>(eski.subList((int) yangiMuddat, eski.size()))
                : new ArrayList<>();
        if (!ortiqcha.isEmpty()) {
            List<Long> ortiqchaIds = ortiqcha.stream().map(Jadval::getId).toList();
            List<PaymentHistory> bogliqTulovlar = paymentRepository.findByJadvalIdIn(ortiqchaIds);
            for (PaymentHistory p : bogliqTulovlar) {
                p.setShartnomaId(s.getId());
                p.setJadvalId(0);
            }
            paymentRepository.saveAll(bogliqTulovlar);
        }

        // To'langan summani yangi grafikga FIFO tartibida taqsimlash
        long qoldiq = jamiTulangan;
        for (Jadval j : yangiList) {
            if (qoldiq <= 0) break;
            long q = Math.min(qoldiq, j.getSumma());
            j.setTulangan(q);
            if (q >= j.getSumma()) {
                j.setHolat("tulangan");
                j.setTulovSana(LocalDateTime.now());
            }
            qoldiq -= q;
        }
        // Agar to'langan summa umumiy summadan oshib ketgan bo'lsa — ortig'i oxirgi oyga yoziladi
        if (qoldiq > 0 && !yangiList.isEmpty()) {
            Jadval oxirgi = yangiList.get(yangiList.size() - 1);
            oxirgi.setTulangan(oxirgi.getTulangan() + qoldiq);
        }

        jadvalRepository.saveAll(yangiList);
        s.setMuddat(yangiMuddat);
        s.setJadvalList(yangiList);
        shartnomaRepository.save(s);
        if (!ortiqcha.isEmpty()) {
            jadvalRepository.deleteAll(ortiqcha);
        }

        amalService.log("SHARTNOMA_TAHRIR",
                "Shartnoma #" + s.getId() + " muddati o'zgartirildi: " + eskiMuddat + " oy → " + yangiMuddat
                        + " oy (oylik to'lov: " + oylik + " so'm)",
                s.getMijoz() != null ? s.getMijoz().getId() : null, s.getId(), null);

        return new ApiResponse("Muddat " + yangiMuddat + " oyga o'zgartirildi", true);
    }

    /**
     * Bitta jadval qatorining to'lov sanasini o'zgartiradi.
     * Faqat SANA o'zgaradi — summa, to'langan va holat tegilmaydi.
     * Kunning vaqti (soat/daqiqa) eski sanadan saqlanib qoladi.
     */
    @Transactional
    public ApiResponse jadvalSanaOzgartirish(Long jadvalId, LocalDate yangiSana) {
        if (jadvalId == null) return new ApiResponse("Jadval ID berilmadi", false);
        if (yangiSana == null) return new ApiResponse("Yangi sanani kiriting", false);

        Jadval j = jadvalRepository.findById(jadvalId).orElse(null);
        if (j == null) return new ApiResponse("Jadval topilmadi: " + jadvalId, false);

        // To'liq to'langan qatorning sanasini o'zgartirishga ruxsat yo'q
        // (frontendda ruchka yashirilgan, lekin so'rov to'g'ridan-to'g'ri ham kelishi mumkin)
        if (j.getTulangan() >= j.getSumma() || "tulangan".equalsIgnoreCase(j.getHolat())) {
            return new ApiResponse("Bu oy to'langan — sanasini o'zgartirib bo'lmaydi", false);
        }

        LocalDateTime eski = j.getSana();
        if (eski != null && eski.toLocalDate().equals(yangiSana)) {
            return new ApiResponse("Sana hozirgisi bilan bir xil", false);
        }

        // eski vaqtni saqlaymiz, bo'lmasa 09:00
        LocalDateTime yangi = (eski != null)
                ? yangiSana.atTime(eski.toLocalTime())
                : yangiSana.atTime(9, 0);

        j.setSana(yangi);
        jadvalRepository.save(j);

        Shartnoma s = shartnomaRepository.findByJadvalListContaining(j).orElse(null);
        amalService.log("SHARTNOMA_TAHRIR",
                "Shartnoma #" + (s != null ? s.getId() : "?") + " to'lov sanasi o'zgartirildi: "
                        + (eski != null ? eski.toLocalDate() : "—") + " → " + yangiSana,
                (s != null && s.getMijoz() != null) ? s.getMijoz().getId() : null,
                s != null ? s.getId() : null, null);

        return new ApiResponse("To'lov sanasi " + yangiSana + " ga o'zgartirildi", true);
    }

    /**
     * Oylar bo'yicha statistika: har oyda qo'shilgan shartnomalar soni va umumiy summasi.
     * Oy sifatida shartnoma tuzilgan sana (sotibOlinganSana) olinadi,
     * u bo'sh bo'lsa createdTime'ga qaraladi. Oraliqda bo'sh oylar 0 bilan to'ldiriladi.
     */
    public StatistikaResponseDto getStatistika() {
        List<Object[]> qatorlar = shartnomaRepository.findAllForStatistika();

        // TreeMap — oylar avtomatik o'sish tartibida saqlanadi ("2026-01" < "2026-02" ...)
        TreeMap<String, long[]> oylar = new TreeMap<>(); // key: "yyyy-MM", value: [soni, summa]
        long totalCount = 0, totalSumma = 0;
        int zaxiraSanaBilan = 0;   // shartnoma sanasi o'qilmagani uchun createdTime ishlatilganlar

        for (Object[] q : qatorlar) {
            String sotibOlinganSana = (String) q[0];
            LocalDateTime createdTime = (LocalDateTime) q[1];
            long summa = q[2] != null ? ((Number) q[2]).longValue() : 0;

            // Avval shartnoma sanasini o'qiymiz, faqat u yaroqsiz bo'lsa createdTime'ga qaytamiz
            String oy = oyKeyMatndan(sotibOlinganSana);
            if (oy == null && createdTime != null) {
                oy = String.format("%04d-%02d", createdTime.getYear(), createdTime.getMonthValue());
                zaxiraSanaBilan++;
            }
            if (oy == null) continue;

            long[] v = oylar.computeIfAbsent(oy, k -> new long[2]);
            v[0]++;
            v[1] += summa;
            totalCount++;
            totalSumma += summa;
        }

        // Bo'sh oylarni 0 bilan to'ldirish (grafik uzluksiz bo'lishi uchun)
        List<OylikStatDto> items = new ArrayList<>();
        if (!oylar.isEmpty()) {
            java.time.YearMonth boshi = java.time.YearMonth.parse(oylar.firstKey());
            java.time.YearMonth oxiri = java.time.YearMonth.parse(oylar.lastKey());
            for (java.time.YearMonth ym = boshi; !ym.isAfter(oxiri); ym = ym.plusMonths(1)) {
                String key = String.format("%04d-%02d", ym.getYear(), ym.getMonthValue());
                long[] v = oylar.getOrDefault(key, new long[2]);
                items.add(new OylikStatDto(key, v[0], v[1]));
            }
        }

        if (zaxiraSanaBilan > 0) {
            // Bu son katta bo'lsa — bazadagi sotibOlinganSana bo'sh yoki formati buzuq degani
            System.out.println("[STATISTIKA] " + zaxiraSanaBilan + " ta shartnomaning sanasi o'qilmadi; "
                    + "ular bazaga kiritilgan oy bo'yicha hisoblandi.");
        }

        return new StatistikaResponseDto(items, totalCount, totalSumma);
    }

    /**
     * Kalendar statistikasi — tanlangan oy uchun kunlar kesimida shartnomalar.
     * Yil/oy berilmasa joriy oy olinadi.
     *
     * Sana sifatida shartnoma sanasi (sotibOlinganSana) ishlatiladi;
     * u o'qilmasa createdTime'ga qaytiladi.
     */
    public KalendarStatDto getKalendarStatistika(Integer yilParam, Integer oyParam) {
        LocalDate bugun = LocalDate.now();
        int yil = (yilParam != null) ? yilParam : bugun.getYear();
        int oy  = (oyParam  != null) ? oyParam  : bugun.getMonthValue();
        if (oy < 1 || oy > 12) oy = bugun.getMonthValue();

        List<Object[]> qatorlar = shartnomaRepository.findAllForKalendar();

        TreeSet<Integer> yillar = new TreeSet<>();
        Map<Integer, long[]> kunlarMap = new TreeMap<>();   // kun -> [soni, summa]
        List<KunShartnomaDto> oyShartnomalari = new ArrayList<>();
        long oySoni = 0, oySumma = 0, yilSoni = 0, yilSumma = 0;

        for (Object[] q : qatorlar) {
            long id            = ((Number) q[0]).longValue();
            String sanaMatn    = (String) q[1];
            LocalDateTime crt  = (LocalDateTime) q[2];
            long summa         = q[3] != null ? ((Number) q[3]).longValue() : 0;
            long muddat        = q[4] != null ? ((Number) q[4]).longValue() : 0;
            String status      = (String) q[5];

            LocalDate sana = sanaMatndan(sanaMatn);
            if (sana == null && crt != null) sana = crt.toLocalDate();
            if (sana == null) continue;

            yillar.add(sana.getYear());

            if (sana.getYear() != yil) continue;
            yilSoni++;
            yilSumma += summa;

            if (sana.getMonthValue() != oy) continue;
            oySoni++;
            oySumma += summa;

            long[] v = kunlarMap.computeIfAbsent(sana.getDayOfMonth(), k -> new long[2]);
            v[0]++;
            v[1] += summa;

            String fish = fishYig(q[6], q[7], q[8]);
            oyShartnomalari.add(new KunShartnomaDto(
                    id, sana.toString(), fish, (String) q[9], summa, muddat, status));
        }

        // Jadval kun bo'yicha, keyin id bo'yicha tartiblanadi
        oyShartnomalari.sort(Comparator
                .comparing(KunShartnomaDto::getSana)
                .thenComparing(KunShartnomaDto::getId));

        List<KunlikStatDto> kunlar = new ArrayList<>();
        for (Map.Entry<Integer, long[]> e : kunlarMap.entrySet()) {
            kunlar.add(new KunlikStatDto(e.getKey(), e.getValue()[0], e.getValue()[1]));
        }

        yillar.add(bugun.getYear());   // joriy yil doim ro'yxatda bo'lsin
        yillar.add(yil);

        return new KalendarStatDto(new ArrayList<>(yillar), yil, oy,
                oySoni, oySumma, yilSoni, yilSumma, kunlar, oyShartnomalari);
    }

    /**
     * Do'konlar bo'yicha statistika: har bir sotgan do'kon kesimida shartnomalar
     * soni, umumiy summasi, tannarxi, foydasi va umumiy summadagi ulushi.
     *
     * yil = null bo'lsa barcha davr olinadi. Sana sifatida sotibOlinganSana,
     * u o'qilmasa createdTime ishlatiladi (boshqa statistikalar bilan bir xil qoida).
     * dokonId NULL bo'lgan shartnomalar "Do'kon belgilanmagan" qatoriga yig'iladi.
     */
    public DokonStatResponseDto getDokonStatistika(Integer yil) {
        List<Object[]> qatorlar = shartnomaRepository.findAllForDokonStat();

        // Do'kon nomlari bir marta o'qiladi (har qator uchun findById qilinmaydi)
        Map<Long, String> dokonNomlari = new HashMap<>();
        for (Magazin m : magazinRepository.findAll()) {
            dokonNomlari.put(m.getId(), m.getNomi());
        }

        TreeSet<Integer> yillar = new TreeSet<>();
        // key: dokonId (null bo'lsa -1L bilan belgilaymiz), value: [soni, summa, tannarx]
        Map<Long, long[]> yigma = new HashMap<>();
        long jamiSoni = 0, jamiSumma = 0, jamiTannarx = 0;

        for (Object[] q : qatorlar) {
            Long dokonId       = q[0] != null ? ((Number) q[0]).longValue() : null;
            String sanaMatn    = (String) q[1];
            LocalDateTime crt  = (LocalDateTime) q[2];
            long summa         = q[3] != null ? ((Number) q[3]).longValue() : 0;
            long tannarx       = tannarxSon((String) q[4]);

            LocalDate sana = sanaMatndan(sanaMatn);
            if (sana == null && crt != null) sana = crt.toLocalDate();
            if (sana != null) yillar.add(sana.getYear());

            // Yil filtri: sanasi umuman o'qilmaganlar faqat "Hammasi" rejimida ko'rinadi
            if (yil != null && (sana == null || sana.getYear() != yil)) continue;

            long key = dokonId != null ? dokonId : -1L;
            long[] v = yigma.computeIfAbsent(key, k -> new long[3]);
            v[0]++;
            v[1] += summa;
            v[2] += tannarx;
            jamiSoni++;
            jamiSumma += summa;
            jamiTannarx += tannarx;
        }

        List<DokonStatDto> dokonlar = new ArrayList<>();
        for (Map.Entry<Long, long[]> e : yigma.entrySet()) {
            Long dokonId = e.getKey() == -1L ? null : e.getKey();
            String nomi = dokonId == null
                    ? "Do'kon belgilanmagan"
                    : dokonNomlari.getOrDefault(dokonId, "Do'kon #" + dokonId);
            long[] v = e.getValue();
            double ulush = jamiSumma > 0 ? (v[1] * 100.0 / jamiSumma) : 0;
            dokonlar.add(new DokonStatDto(dokonId, nomi, v[0], v[1], v[2], v[1] - v[2], ulush));
        }
        // Summasi kattasi tepada; "belgilanmagan" qatori doim oxirida
        dokonlar.sort(Comparator
                .comparing((DokonStatDto d) -> d.getDokonId() == null ? 1 : 0)
                .thenComparing(DokonStatDto::getSumma, Comparator.reverseOrder()));

        yillar.add(LocalDate.now().getYear());
        return new DokonStatResponseDto(new ArrayList<>(yillar), yil,
                jamiSoni, jamiSumma, jamiSumma - jamiTannarx, dokonlar);
    }

    /** Tannarx String saqlanadi — faqat raqamlarini olib songa o'giramiz ("2 500 000 so'm" ham o'qiladi). */
    private long tannarxSon(String matn) {
        if (matn == null) return 0;
        String raqamlar = matn.replaceAll("[^0-9]", "");
        if (raqamlar.isEmpty()) return 0;
        try {
            return Long.parseLong(raqamlar);
        } catch (NumberFormatException e) {
            return 0; // haddan tashqari uzun/buzuq qiymat
        }
    }

    private String fishYig(Object ism, Object familiya, Object sharif) {
        StringBuilder sb = new StringBuilder();
        for (Object p : new Object[]{ism, familiya, sharif}) {
            if (p != null && !String.valueOf(p).isBlank()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(String.valueOf(p).trim());
            }
        }
        return sb.length() == 0 ? "—" : sb.toString();
    }

    private String oyKey(String sotibOlinganSana, LocalDateTime createdTime) {
        String oy = oyKeyMatndan(sotibOlinganSana);
        if (oy != null) return oy;
        if (createdTime != null) {
            return String.format("%04d-%02d", createdTime.getYear(), createdTime.getMonthValue());
        }
        return null;
    }

    // 2026-07-23 | 2026/07/23 | 2026-07-23T10:30:00 | 2026-07
    private static final java.util.regex.Pattern OY_ISO =
            java.util.regex.Pattern.compile("^(\\d{4})[-/.](\\d{1,2})");
    // 23.07.2026 | 23/07/2026 | 23-07-2026
    private static final java.util.regex.Pattern OY_KUN_OY_YIL =
            java.util.regex.Pattern.compile("^(\\d{1,2})[-/.](\\d{1,2})[-/.](\\d{4})");

    /**
     * Matn ko'rinishidagi sanadan "yyyy-MM" kalitini ajratadi.
     * sotibOlinganSana bazada oddiy String bo'lgani uchun formatlar aralash bo'lishi mumkin.
     * O'qib bo'lmasa null qaytaradi — chaqiruvchi createdTime'ga qaytadi.
     */
    private String oyKeyMatndan(String matn) {
        LocalDate d = sanaMatndan(matn);
        return d == null ? null : String.format("%04d-%02d", d.getYear(), d.getMonthValue());
    }

    // To'liq sana: 2026-07-23 | 2026/07/23 | 2026-07-23T10:30:00
    private static final java.util.regex.Pattern SANA_ISO =
            java.util.regex.Pattern.compile("^(\\d{4})[-/.](\\d{1,2})[-/.](\\d{1,2})");

    /**
     * Matn ko'rinishidagi sanani LocalDate'ga o'giradi.
     * sotibOlinganSana bazada oddiy String bo'lgani uchun formatlar aralash bo'lishi mumkin.
     * Faqat oy berilgan bo'lsa (masalan "2026-07") — oyning 1-kuni deb olinadi.
     * O'qib bo'lmasa null qaytaradi.
     */
    private LocalDate sanaMatndan(String matn) {
        if (matn == null) return null;
        String t = matn.trim();
        if (t.isEmpty() || "null".equalsIgnoreCase(t)) return null;

        java.util.regex.Matcher m = SANA_ISO.matcher(t);
        if (m.find()) return sanaYasa(m.group(1), m.group(2), m.group(3));

        m = OY_KUN_OY_YIL.matcher(t);
        if (m.find()) return sanaYasa(m.group(3), m.group(2), m.group(1));

        m = OY_ISO.matcher(t);
        if (m.find()) return sanaYasa(m.group(1), m.group(2), "1");

        return null;
    }

    private LocalDate sanaYasa(String yil, String oy, String kun) {
        try {
            int y = Integer.parseInt(yil);
            int o = Integer.parseInt(oy);
            int k = Integer.parseInt(kun);
            if (y < 1900 || y > 2200) return null;
            return LocalDate.of(y, o, k);   // noto'g'ri oy/kun bo'lsa istisno tashlaydi
        } catch (Exception e) {
            return null;
        }
    }


    private String resolveDokonNomi(Long dokonId) {
        if (dokonId == null) return null;
        // sizdagi do'kon entitisi/metodiga qarab moslang
        return magazinRepository.findById(dokonId)
                .map(Magazin::getNomi)
                .orElse("Do'kon #" + dokonId);
    }

    public ApiResponse addMagazine(Magazin magazin) {
        if(magazinRepository.findByNomi(magazin.getNomi()).isPresent()){
            return new ApiResponse("Bu nomi allaqachon ishlatilgan",false);
        }
        Magazin magazin1 = new Magazin();
        magazin1.setManzil(magazin.getManzil());
        magazin1.setNomi(magazin.getNomi());
        magazinRepository.save(magazin1);
        amalService.log("DOKON", "Yangi do'kon qo'shildi: " + magazin1.getNomi());
        return new ApiResponse("Magazin qo'shildi",true);
    }

    public Object getMagazinAll() {
        return magazinRepository.findAll();
    }


    public Users getMydata(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String username = null;
        // Agar header yo‘q bo‘lsa, cookie’dan o‘qish
        if (header == null || !header.startsWith("Bearer ")) {
            if (request.getCookies() != null) {
                for (Cookie c : request.getCookies()) {
                    if ("Auth".equals(c.getName())) {
                        header = "Bearer " + c.getValue();
                    }
                }
            }
        }
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                username = tokenGenerator.extractUsername(token);
            } catch (io.jsonwebtoken.ExpiredJwtException ex) {
                // Token muddati tugagan, SecurityContext bo'lmaydi
                System.out.println("JWT expired: " + ex.getMessage());
            } catch (Exception e) {
                System.out.println("JWT error: " + e.getMessage());
            }
        }
        if (username != null) {
            Optional<Users> user = usersRepository.findByUsername(username);
            return user.get();
        }
        return null;
    }

    public ApiResponse updateMyData(Users user) {
        Optional<Users> byUsername = usersRepository.findByUsername(user.getUsername());
        Users users = byUsername.get();
        users.setFish(user.getFish());
        users.setAddress(user.getAddress());
        users.setTel(user.getTel());
        users.setIzoh(user.getIzoh());
        usersRepository.save(users);
        return new ApiResponse("Muvaffaqiyatli o'zgartirildi",true);
    }

    public ApiResponse changePassword(Users users) {
        Users users1 = usersRepository.findByUsername(users.getUsername()).get();
        users1.setPassword(passwordEncoder.encode(users.getPassword()));
        usersRepository.save(users1);
        return new ApiResponse("Parol o'zgartirildi",true);
    }

    public Object getMagazinlar() {
        List<Nomlar> nomlars = new ArrayList<>();
        for (Magazin magazin : magazinRepository.findAll()) {
            Nomlar nomlar = new Nomlar();
            nomlar.setId(magazin.getId());
            nomlar.setNomi(magazin.getNomi());
            nomlars.add(nomlar);
        }
        return nomlars;
    }

    public ApiResponse  tulov(Long id, long summa, String turi, LocalDateTime sana, Long dokonId) {
        Optional<Jadval> byId = jadvalRepository.findById(id);
        if(byId.isEmpty()){
            return new ApiResponse("Jadval topilmadi",false);
        }
        Jadval jadval = byId.get();

        jadval.setTulangan(jadval.getTulangan()+summa);
        jadval.setTulovSana(sana);
        jadval.setTuri(turi);
        jadval.setDokonId(dokonId.toString());
        if(jadval.getTulangan()>=jadval.getSumma()){
            jadval.setHolat("tulangan");
        }
        jadvalRepository.save(jadval);

        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setJadvalId(jadval.getId());
        paymentHistory.setSumma(summa);
        paymentHistory.setSana(sana);
        paymentHistory.setTuri(turi);
        paymentHistory.setDokonId(dokonId);
        paymentHistory.setCreatedTime(LocalDateTime.now());


        paymentRepository.save(paymentHistory);

        return new ApiResponse("Muvaffaqiyatli tulov qilindi",true);
    }
    @Transactional
    public ApiResponse tulovShartnoma(Long shartnomaId, long summa, String turi, Long dokonId) {
        LocalDateTime now = LocalDateTime.now();
        if (summa == 0) {
            return new ApiResponse("To'lov summasi noto'g'ri", false);
        }

        Optional<Shartnoma> byId = shartnomaRepository.findById(shartnomaId);
        if (byId.isEmpty()) {
            return new ApiResponse("Shartnoma topilmadi", false);
        }
        Shartnoma shartnoma = byId.get();

        List<Jadval> jadvalList = shartnoma.getJadvalList();
        if (jadvalList == null || jadvalList.isEmpty()) {
            return new ApiResponse("Bu shartnomada to'lov jadvali mavjud emas", false);
        }

        if (summa > 0) {
            ApiResponse natija = tulovQoshish(shartnoma, jadvalList, summa, turi, dokonId, now);
            if (natija.isHolat()) {
                amalService.log("TOLOV",
                        "To'lov qabul qilindi: " + mijozIsmi(shartnoma.getMijoz()) + ", shartnoma #" + shartnoma.getId() + " (" + turi + ")",
                        shartnoma.getMijoz() != null ? shartnoma.getMijoz().getId() : null, shartnoma.getId(), summa);
            }
            return natija;
        } else {
            ApiResponse natija = tulovAyirish(shartnoma, jadvalList, -summa, turi, dokonId, now);
            if (natija.isHolat()) {
                amalService.log("TOLOV_AYIRISH",
                        "To'lov qaytarildi/ayirildi: " + mijozIsmi(shartnoma.getMijoz()) + ", shartnoma #" + shartnoma.getId() + " (" + turi + ")",
                        shartnoma.getMijoz() != null ? shartnoma.getMijoz().getId() : null, shartnoma.getId(), summa);
            }
            return natija;
        }
    }

    // Musbat to'lov — eng eski sanadan boshlab FIFO tartibida to'lanmagan jadvallarga taqsimlanadi
    private ApiResponse tulovQoshish(Shartnoma shartnoma, List<Jadval> jadvalList, long summa, String turi, Long dokonId, LocalDateTime now) {
        // Eng eski sanadan boshlab tartiblaymiz — FIFO
        List<Jadval> tartiblangan = jadvalList.stream()
                .sorted(Comparator.comparing(Jadval::getSana))
                .toList();

        long qoldiq = summa;
        Long birinchiTegilganJadvalId = null;

        for (Jadval jadval : tartiblangan) {
            if (qoldiq <= 0) break;
            if (!"tulanmagan".equals(jadval.getHolat())) continue;

            long jadvalQolgan = jadval.getSumma() - jadval.getTulangan();
            if (jadvalQolgan <= 0) continue;

            long qoshiladigan = Math.min(qoldiq, jadvalQolgan);

            jadval.setTulangan(jadval.getTulangan() + qoshiladigan);
            jadval.setTulovSana(now);
            jadval.setTuri(turi);
            jadval.setDokonId(dokonId.toString());
            if (jadval.getTulangan() >= jadval.getSumma()) {
                jadval.setHolat("tulangan");
            }
            jadvalRepository.save(jadval);

            if (birinchiTegilganJadvalId == null) {
                birinchiTegilganJadvalId = jadval.getId();
            }

            qoldiq -= qoshiladigan;
        }

        long qabulQilingan = summa - qoldiq;
        if (qabulQilingan <= 0) {
            return new ApiResponse("Barcha to'lov jadvallari allaqachon to'langan", false);
        }

        // ── Shartnoma yopilganmi tekshiramiz (barcha jadvallar tulangan) ──
        boolean hammasiTulangan = tartiblangan.stream().allMatch(j -> "tulangan".equals(j.getHolat()));
        if (hammasiTulangan) {
            shartnoma.setStatus("yopilgan");
            shartnomaRepository.save(shartnoma);
        }

        // ── Bitta umumiy to'lov tarixi yozuvi ──
        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setShartnomaId(shartnoma.getId());
        paymentHistory.setJadvalId(birinchiTegilganJadvalId);
        paymentHistory.setSumma(qabulQilingan);
        paymentHistory.setSana(now);
        paymentHistory.setTuri(turi);
        paymentHistory.setDokonId(dokonId);
        paymentHistory.setCreatedTime(now);
        paymentRepository.save(paymentHistory);

        long umumiyQolgan = tartiblangan.stream()
                .mapToLong(j -> Math.max(0, j.getSumma() - j.getTulangan()))
                .sum();
        telegramXabarService.tulovXabarYuborish(shartnoma.getMijoz(), qabulQilingan, shartnoma.getMahsulot(), shartnoma.getId(), umumiyQolgan);
        smsXabarService.tulovXabarYuborish(shartnoma.getMijoz(), qabulQilingan, shartnoma.getMahsulot(), shartnoma.getId(), umumiyQolgan);

        if (qoldiq > 0) {
            return new ApiResponse(
                    "To'lov saqlandi. Diqqat: " + qoldiq + " so'm ortiqcha (barcha jadvallar to'liq to'landi).",
                    true
            );
        }
        return new ApiResponse("Muvaffaqiyatli to'lov qilindi", true);
    }

    // Manfiy to'lov (ayirish/bekor qilish) — eng so'nggi sanadan boshlab orqaga qarab,
    // to'langan jadvallardan xuddi to'lov algoritmi kabi (bitta oy yetmasa keyingisidan olib) ayiriladi
    private ApiResponse tulovAyirish(Shartnoma shartnoma, List<Jadval> jadvalList, long ayiriladiganSumma, String turi, Long dokonId, LocalDateTime now) {
        // Eng so'nggi sanadan boshlab tartiblaymiz — orqaga qarab (LIFO)
        List<Jadval> tartiblangan = jadvalList.stream()
                .sorted(Comparator.comparing(Jadval::getSana).reversed())
                .toList();

        long qoldiq = ayiriladiganSumma;
        Long birinchiTegilganJadvalId = null;

        for (Jadval jadval : tartiblangan) {
            if (qoldiq <= 0) break;

            long jadvalTulangan = jadval.getTulangan();
            if (jadvalTulangan <= 0) continue;

            long ayriladigan = Math.min(qoldiq, jadvalTulangan);

            jadval.setTulangan(jadvalTulangan - ayriladigan);
            jadval.setTulovSana(now);
            jadval.setTuri(turi);
            jadval.setDokonId(dokonId.toString());
            if (jadval.getTulangan() < jadval.getSumma()) {
                jadval.setHolat("tulanmagan");
            }
            jadvalRepository.save(jadval);

            if (birinchiTegilganJadvalId == null) {
                birinchiTegilganJadvalId = jadval.getId();
            }

            qoldiq -= ayriladigan;
        }

        long ayirilganSumma = ayiriladiganSumma - qoldiq;
        if (ayirilganSumma <= 0) {
            return new ApiResponse("Ayirish uchun to'langan mablag' topilmadi", false);
        }

        // ── Shartnoma "yopilgan" bo'lsa va endi to'liq to'lanmagan bo'lsa — qayta ochamiz ──
        boolean hammasiTulangan = tartiblangan.stream().allMatch(j -> "tulangan".equals(j.getHolat()));
        if (!hammasiTulangan && "yopilgan".equals(shartnoma.getStatus())) {
            shartnoma.setStatus("ochiq");
            shartnomaRepository.save(shartnoma);
        }

        // ── Bitta umumiy to'lov tarixi yozuvi (manfiy summa bilan) ──
        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setShartnomaId(shartnoma.getId());
        paymentHistory.setJadvalId(birinchiTegilganJadvalId);
        paymentHistory.setSumma(-ayirilganSumma);
        paymentHistory.setSana(now);
        paymentHistory.setTuri(turi);
        paymentHistory.setDokonId(dokonId);
        paymentHistory.setCreatedTime(now);
        paymentRepository.save(paymentHistory);

        if (qoldiq > 0) {
            return new ApiResponse(
                    "Ayirish saqlandi. Diqqat: " + qoldiq + " so'm ayirib bo'lmadi (to'langan mablag' yetarli emas).",
                    true
            );
        }
        return new ApiResponse("To'lov muvaffaqiyatli ayirildi", true);
    }

    public Object getTodayPayment() {
        LocalDateTime boshi = LocalDate.now().atStartOfDay();           // 2026-06-24T00:00:00
        LocalDateTime oxiri = boshi.plusDays(1);                        // 2026-06-25T00:00:00
        List<Statistikaga> statistikagas=new ArrayList<>();
        for (PaymentHistory todayPayment : paymentRepository.findTodayPayments(boshi, oxiri)) {
            Statistikaga statistikaga = new Statistikaga();
            Optional<Magazin> dokon = magazinRepository.findById(Long.valueOf(todayPayment.getDokonId()));
            statistikaga.setDokon(dokon.get().getNomi());
            statistikaga.setTuri(todayPayment.getTuri());
            statistikaga.setSana(todayPayment.getSana());
            Optional<Jadval> jadval = jadvalRepository.findById(todayPayment.getJadvalId());
            Shartnoma shartnoma = shartnomaRepository.findByJadvalListContaining(jadval.get()).get();
            statistikaga.setShartnomaId(shartnoma.getId());
            statistikaga.setMijoz(shartnoma.getMijoz().getIsm()+" "+shartnoma.getMijoz().getFamiliya()+" "+shartnoma.getMijoz().getSharif());
            statistikaga.setSumma(todayPayment.getSumma());
            statistikagas.add(statistikaga);
        }
        return statistikagas;
    }

    public ApiResponse addShartnoma(MijozDataDto mijoz, MultipartFile rasm1, MultipartFile rasm2) throws IOException {
        Optional<Mijoz> byId = mijozRepository.findById(mijoz.getMijozId());
        if(byId.isEmpty()){
            return new ApiResponse("Mijoz topilmadi",false);
        }
        Mijoz mijoz1 = byId.get();
        Shartnoma shartnoma = new Shartnoma();
        shartnoma.setMijoz(mijoz1);
        shartnoma.setSumma(mijoz.getSumma());
        shartnoma.setStatus("ochiq");
        shartnoma.setIzoh(mijoz.getIzoh());
        shartnoma.setMahsulot(mijoz.getAbout());
        shartnoma.setTannarx(mijoz.getTannarx());
        shartnoma.setJoylashuv(mijoz.getJoylashuv());
        // Sotilgan do'kon (ilgari bu yo'lda ham saqlanmasdi)
        shartnoma.setDokonId(mijoz.getDokonId() > 0 ? Long.valueOf(mijoz.getDokonId()) : null);

        shartnoma.setMuddat(mijoz.getMuddat());
        // ASOSIY TUZATISH: bu yo'lda shartnoma sanasi umuman saqlanmasdi.
        // createdTime @CreationTimestamp bilan avtomatik yozilgani uchun bu yerdagi
        // setCreatedTime(...) hech qanday ta'sir qilmasdi va sana yo'qolib ketardi —
        // natijada statistika shartnomani BAZAGA KIRITILGAN oyga qo'shardi.
        shartnoma.setSotibOlinganSana(mijoz.getShartnomaSana() != null
                ? mijoz.getShartnomaSana().toLocalDate().toString()
                : null);
        long tulov=mijoz.getSumma()/mijoz.getMuddat();
        List<Jadval> jadvalList = new ArrayList<>();
        for (long i = 0; i < mijoz.getMuddat(); i++) {
            Jadval jadval = new Jadval();
            jadval.setSana(mijoz.getTulovSana().plusMonths(i));
            jadval.setSumma(tulov);
            jadval.setTulangan(0);
            jadval.setHolat("tulanmagan");
            jadval = jadvalRepository.save(jadval);
            jadvalList.add(jadval);
        }
        shartnoma.setJadvalList(jadvalList);
        FaylBayt kafolatRasm = new FaylBayt();
        kafolatRasm.setOriginalNomi(rasm1.getOriginalFilename());
        kafolatRasm.setHajmiFayl(rasm1.getSize());
        kafolatRasm.setContentTypeFayl(rasm1.getContentType());
        kafolatRasm.setBayt(rasm1.getBytes());
        FaylBayt kafolatSave = faylBaytRepository.save(kafolatRasm);

        FaylBayt kafolat2Rasm = new FaylBayt();
        kafolat2Rasm.setOriginalNomi(rasm2.getOriginalFilename());
        kafolat2Rasm.setHajmiFayl(rasm2.getSize());
        kafolat2Rasm.setContentTypeFayl(rasm2.getContentType());
        kafolat2Rasm.setBayt(rasm2.getBytes());
        FaylBayt kafolat2Save = faylBaytRepository.save(kafolat2Rasm);

        shartnoma.setKafolat(kafolatSave);
        shartnoma.setKafolat2(kafolat2Save);

        LocalDateTime localDateTime = LocalDateTime.now();
        shartnoma.setCreatedTime(localDateTime);

        shartnomaRepository.save(shartnoma);
        telegramXabarService.yangiShartnomaXabarYuborish(shartnoma);
        smsXabarService.yangiShartnomaXabarYuborish(shartnoma);
        amalService.log("SHARTNOMA_QOSHISH",
                "Yangi shartnoma yaratildi: " + mijozIsmi(shartnoma.getMijoz())
                        + (shartnoma.getMahsulot() != null ? ", mahsulot: " + shartnoma.getMahsulot() : ""),
                shartnoma.getMijoz() != null ? shartnoma.getMijoz().getId() : null, shartnoma.getId(), shartnoma.getSumma());
        return new ApiResponse("Shartnoma yaratildi",true);
    }
    public List<OverdueDebtDto> getOverdueDebts() {
        List<Jadval> overdueList = jadvalRepository.findOverdueUnpaid(LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now();

        List<OverdueDebtDto> result = new ArrayList<>();
        for (Jadval jadval : overdueList) {
            OverdueDebtDto dto = new OverdueDebtDto();

            long qolgan = jadval.getSumma() - jadval.getTulangan();
            dto.setQolgan(qolgan);

            long kun = Duration.between(jadval.getSana(), now).toDays();
            dto.setKun(kun);

            shartnomaRepository.findByJadvalListContaining(jadval).ifPresent(s -> {
                dto.setShartnomaId(s.getId());
                Mijoz m = s.getMijoz();
                if (m != null) {
                    dto.setMijozId(m.getId());
                    dto.setMijoz(m.getFamiliya() + " " + m.getIsm());
                    dto.setTel(m.getTel1());
                }
            });

            dto.setOxirgiTolov(jadval.getTulovSana());

            result.add(dto);
        }
        return result;
    }

    public ApiResponse checkPassport(String passport) {
        Optional<Mijoz> byPassport = mijozRepository.findByPassport(passport);
        if(byPassport.isPresent()){
            return new ApiResponse("Bu passport egasi bazada mavjud!",false);
        }
        return new ApiResponse("Hammasi yaxshi!",true);
    }

    public Object getDashboardData() {
        long mijoz = mijozRepository.count();

        DashboardData dashboardData = new DashboardData();
        dashboardData.setMijozSoni(mijoz);

        return dashboardData;
    }

    public FoydaResponseDTO getFoyda(String davr) {
        return getFoyda(davr, null, null);
    }
    public FoydaResponseDTO getFoyda(String davr, String fromParam, String toParam) {
        String from;
        String to;
        LocalDate now = LocalDate.now();

        switch (davr) {
            case "kunlik" -> {
                from = now.toString();
                to   = now.toString();
            }
            case "haftalik" -> {
                LocalDate dushanba = now.with(java.time.DayOfWeek.MONDAY);
                LocalDate yakshanba = now.with(java.time.DayOfWeek.SUNDAY);
                from = dushanba.toString();
                to   = yakshanba.toString();
            }
            case "oylik" -> {
                from = now.withDayOfMonth(1).toString();
                to   = now.withDayOfMonth(now.lengthOfMonth()).toString();
            }
            case "yillik" -> {
                from = now.withDayOfYear(1).toString();
                to   = now.withDayOfYear(now.lengthOfYear()).toString();
            }
            case "tanlangan" -> {
                if (fromParam == null || fromParam.isBlank() || toParam == null || toParam.isBlank()) {
                    throw new IllegalArgumentException("Tanlangan muddat uchun 'from' va 'to' parametrlari majburiy");
                }
                from = fromParam;
                to   = toParam;
            }
            default -> throw new IllegalArgumentException("Noto'g'ri davr: " + davr);
        }

        List<Shartnoma> shartnomalar = shartnomaRepository.findBySotibOlinganSanaBetween(from, to);

        List<FoydaItemDto> items = new ArrayList<>();
        long totalSumma = 0, totalTannarx = 0, totalFoyda = 0;

        for (Shartnoma s : shartnomalar) {
            long tannarx = parseLongSafe(s.getTannarx());
            long summa   = s.getSumma();
            long foyda   = summa - tannarx;

            Mijoz m = s.getMijoz();
            String fish = m != null
                    ? (nonNull(m.getFamiliya()) + " " + nonNull(m.getIsm())).trim()
                    : "—";
            String tel = m != null ? nonNull(m.getTel1()) : "—";

            items.add(new FoydaItemDto(s.getId(), fish.isBlank() ? "—" : fish, tel.isBlank() ? "—" : tel, summa, tannarx, foyda));

            totalSumma   += summa;
            totalTannarx += tannarx;
            totalFoyda   += foyda;
        }

        return new FoydaResponseDTO(items, items.size(), totalSumma, totalTannarx, totalFoyda);
    }

    private long parseLongSafe(String s) {
        if (s == null || s.isBlank()) return 0L;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return 0L; // eski/noto'g'ri formatdagi tannarx qiymatlari xatoga sabab bo'lmasin
        }
    }

    private String nonNull(String s) { return s == null ? "" : s; }

    public ApiResponse editMijoz(MijozDataDto mijoz, MultipartFile passport, MultipartFile passport2) throws IOException {
        Optional<Mijoz> byId = mijozRepository.findById(mijoz.getMijozId());
        if (byId.isEmpty()) {
            return new ApiResponse("Mijoz topilmadi", false);
        }
        Mijoz mijoz1 = byId.get();
        mijoz1.setIsm(mijoz.getIsm());
        mijoz1.setFamiliya(mijoz.getFamiliya());
        mijoz1.setSharif(mijoz.getSharif());
        mijoz1.setTel1(mijoz.getTel1());
        mijoz1.setTel2(mijoz.getTel2());
        mijoz1.setTel3(mijoz.getTel3());
        mijoz1.setViloyat(mijoz.getViloyat());
        mijoz1.setTuman(mijoz.getTuman());
        mijoz1.setManzil(mijoz.getManzil());
        mijoz1.setMuljal(mijoz.getMuljal());
        mijoz1.setPassport(mijoz.getPassport());

        if (passport != null && !passport.isEmpty()) {
            mijoz1.setPassportRasm(saqlashYokiYangilash(mijoz1.getPassportRasm(), passport));
        }
        if (passport2 != null && !passport2.isEmpty()) {
            mijoz1.setKatm(saqlashYokiYangilash(mijoz1.getKatm(), passport2));
        }

        mijozRepository.save(mijoz1);
        amalService.log("MIJOZ_TAHRIR", "Mijoz ma'lumotlari tahrirlandi: " + mijozIsmi(mijoz1), mijoz1.getId(), null, null);
        return new ApiResponse("Mijoz o'zgartirildi", true);
    }

    public ApiResponse editShartnomaKafolat(Long shartnomaId, MultipartFile kafolat1, MultipartFile kafolat2) throws IOException {
        Optional<Shartnoma> byId = shartnomaRepository.findById(shartnomaId);
        if (byId.isEmpty()) {
            return new ApiResponse("Shartnoma topilmadi", false);
        }
        Shartnoma shartnoma = byId.get();

        if (kafolat1 != null && !kafolat1.isEmpty()) {
            shartnoma.setKafolat(saqlashYokiYangilash(shartnoma.getKafolat(), kafolat1));
        }
        if (kafolat2 != null && !kafolat2.isEmpty()) {
            shartnoma.setKafolat2(saqlashYokiYangilash(shartnoma.getKafolat2(), kafolat2));
        }

        shartnomaRepository.save(shartnoma);
        amalService.log("SHARTNOMA_TAHRIR", "Shartnoma #" + shartnomaId + " kafolat rasmlari yangilandi", null, shartnomaId, null);
        return new ApiResponse("Kafolat xati yangilandi", true);
    }

    // Mavjud FaylBayt bo'lsa ID bo'yicha topib o'sha yozuvni yangilaydi,
// bo'lmasa yangi FaylBayt yaratadi.
    private FaylBayt saqlashYokiYangilash(FaylBayt eski, MultipartFile file) throws IOException {
        FaylBayt fayl;
        if (eski != null && eski.getId() != 0) {
            fayl = faylBaytRepository.findById(eski.getId())
                    .orElseGet(FaylBayt::new);
        } else {
            fayl = new FaylBayt();
        }
        fayl.setOriginalNomi(file.getOriginalFilename());
        fayl.setHajmiFayl(file.getSize());
        fayl.setContentTypeFayl(file.getContentType());
        fayl.setBayt(file.getBytes());
        return faylBaytRepository.save(fayl);
    }

    @Transactional
    public ApiResponse deleteShartnoma(Long id) {
        Optional<Shartnoma> optional = shartnomaRepository.findById(id);
        if (optional.isEmpty()) {
            return new ApiResponse("Shartnoma topilmadi", false);
        }

        Shartnoma shartnoma = optional.get();

        // 1) Shartnomaga bog'liq to'lov tarixini o'chirish
        List<Jadval> jadvalList = shartnoma.getJadvalList();
        if (jadvalList != null && !jadvalList.isEmpty()) {
            List<Long> jadvalIds = jadvalList.stream()
                    .map(Jadval::getId)
                    .collect(Collectors.toList());

            List<PaymentHistory> tulovlar = paymentRepository.findByJadvalIdIn(jadvalIds);
            paymentRepository.deleteAll(tulovlar);

            // 2) Jadval (to'lov grafigi) yozuvlarini o'chirish
            shartnoma.setJadvalList(null);
            shartnomaRepository.save(shartnoma); // ManyToMany bog'lanishini uzish
            jadvalRepository.deleteAll(jadvalList);
        }

        // 3) Shartnomaning o'zini o'chirish
        Long mijozIdSaqla = shartnoma.getMijoz() != null ? shartnoma.getMijoz().getId() : null;
        String mijozNomi = mijozIsmi(shartnoma.getMijoz());
        shartnomaRepository.delete(shartnoma);

        amalService.log("SHARTNOMA_OCHIRISH",
                "Shartnoma #" + id + " o'chirildi (mijoz: " + mijozNomi + ")", mijozIdSaqla, id, null);

        return new ApiResponse("Shartnoma muvaffaqiyatli o'chirildi", true);
    }

    public List<QarzdorlarDto> getQarzdorlar(String filter) {
        if ("undiruv".equals(filter)) {
            return shartnomaHolatiBoyichaRoyxat("undiruv");
        }
        if ("tugatilgan".equals(filter)) {
            return shartnomaHolatiBoyichaRoyxat("yopilgan");
        }

        LocalDateTime hozir = LocalDateTime.now();
        List<Jadval> jadvalList;

        if ("bugun".equals(filter)) {
            LocalDate bugun = LocalDate.now();
            jadvalList = jadvalRepository.findTodayPayments(
                    bugun.atStartOfDay(), bugun.plusDays(1).atStartOfDay());
        } else if ("otgan".equals(filter)) {
            jadvalList = jadvalRepository.findOverdueUnpaid(hozir);
        } else {
            LocalDate boshi = LocalDate.now().withDayOfMonth(1);
            LocalDate oxiri = boshi.plusMonths(1);
            jadvalList = jadvalRepository.findCurrentMonthUnpaid(
                    boshi.atStartOfDay(), oxiri.atStartOfDay());
        }

        Map<Long, QarzdorlarDto> result = new LinkedHashMap<>();
        for (Jadval jadval : jadvalList) {
            shartnomaRepository.findByJadvalListContaining(jadval).ifPresent(shartnoma ->
                    result.putIfAbsent(shartnoma.getId(), toQarzdorDto(shartnoma, jadval))
            );
        }
        return new ArrayList<>(result.values());
    }

    // "undiruv" va "tugatilgan" uchun umumiy — status bo'yicha to'g'ridan-to'g'ri qidirish
    private List<QarzdorlarDto> shartnomaHolatiBoyichaRoyxat(String status) {
        List<Shartnoma> shartnomalar = shartnomaRepository.findByStatus(status);
        List<QarzdorlarDto> result = new ArrayList<>();
        for (Shartnoma shartnoma : shartnomalar) {
            Jadval engErtaTulanmagan = shartnoma.getJadvalList().stream()
                    .filter(j -> "tulanmagan".equals(j.getHolat()))
                    .min(Comparator.comparing(Jadval::getSana))
                    .orElse(null);
            result.add(toQarzdorDto(shartnoma, engErtaTulanmagan));
        }
        return result;
    }

    private QarzdorlarDto toQarzdorDto(Shartnoma shartnoma, Jadval activeJadval) {
        Mijoz mijoz = shartnoma.getMijoz();
        List<Jadval> jadvalList = shartnoma.getJadvalList();

        long qolganQarz = jadvalList.stream()
                .filter(j -> "tulanmagan".equals(j.getHolat()))
                .mapToLong(j -> Math.max(0, j.getSumma() - j.getTulangan()))
                .sum();

        LocalDateTime hozir = LocalDateTime.now();
        long muddatiOtganKun = jadvalList.stream()
                .filter(j -> "tulanmagan".equals(j.getHolat())
                        && j.getSana() != null && j.getSana().isBefore(hozir))
                .map(Jadval::getSana)
                .min(LocalDateTime::compareTo)
                .map(eng -> Duration.between(eng, hozir).toDays())
                .orElse(0L);

        // Faqat sanasi o'tib ketgan (muddati o'tgan) jadvallar bo'yicha qolgan qarz
        long muddatiOtganQarz = jadvalList.stream()
                .filter(j -> "tulanmagan".equals(j.getHolat())
                        && j.getSana() != null && j.getSana().isBefore(hozir))
                .mapToLong(j -> Math.max(0, j.getSumma() - j.getTulangan()))
                .sum();

        return new QarzdorlarDto(
                mijoz.getId(),
                mijoz.getIsm(),
                mijoz.getFamiliya(),
                mijoz.getTel1(),
                mijoz.getTel2(),
                mijoz.getTel3(),
                shartnoma.getId(),
                qolganQarz,
                activeJadval != null && activeJadval.getSana() != null ? activeJadval.getSana().toString() : null,
                muddatiOtganKun,
                muddatiOtganQarz,
                shartnoma.getStatus(),   // ← qo'shildi
                mijoz.getTuman(),
                shartnoma.getUndiruvSababi()
        );
    }

    public Object getAllSumma() {
        long jamiShartnomaSoni = shartnomaRepository.count();
        long umumiySumma = shartnomaRepository.sumUmumiySumma();
        long qaytganSumma = jadvalRepository.sumTulangan();
        long qolganSumma = umumiySumma - qaytganSumma;

        return new AllSummaStatDto(jamiShartnomaSoni, umumiySumma, qaytganSumma, qolganSumma);
    }

    @Transactional
    public void checkShartnomaStatus(Long shartnomaId) {
        Shartnoma shartnoma = shartnomaRepository.findById(shartnomaId)
                .orElseThrow(() -> new RuntimeException("Shartnoma topilmadi"));

        boolean hammasiTulangan = shartnoma.getJadvalList()
                .stream()
                .allMatch(j -> "tulangan".equalsIgnoreCase(j.getHolat()));

        if (hammasiTulangan) {
            shartnoma.setStatus("yopilgan");
            shartnomaRepository.save(shartnoma);
        }
    }

    public Object checkAllShartnoma(){
        for (Shartnoma shartnoma : shartnomaRepository.findAll()) checkShartnomaStatus(shartnoma.getId());
        return "tekshirildi";
    }
    // Shu sanadan oldingi shartnomalarda sana'dan boshqa kamchiliklar ko'rsatilmaydi
    private static final LocalDate KAMCHILIK_TEKSHIRUV_BOSHI = LocalDate.of(2026, 7, 1);

    public List<ShartnomaRoyxat> getShartnomaWithMissingInfo() {
        List<Shartnoma> list = shartnomaRepository.findShartnomaWithMissingInfo();

        List<ShartnomaRoyxat> natija = new ArrayList<>();
        for (Shartnoma s : list) {
            boolean mahsulotYoq  = s.getMahsulot()  == null || s.getMahsulot().trim().isEmpty();
            boolean tannarxYoq   = s.getTannarx()   == null || s.getTannarx().trim().isEmpty();
            boolean joylashuvYoq = s.getJoylashuv() == null || s.getJoylashuv().trim().isEmpty();

            String sanaMatn = s.getSotibOlinganSana();
            boolean sanaBosh = sanaMatn == null || sanaMatn.trim().isEmpty();
            LocalDate sana = sanaMatndan(sanaMatn);   // bo'sh yoki formati buzuq bo'lsa null

            // Sana ma'lum va 1-iyuldan oldin bo'lsa — bu shartnoma kamchiliklar ro'yxatiga kirmaydi
            if (sana != null && sana.isBefore(KAMCHILIK_TEKSHIRUV_BOSHI)) {
                continue;
            }

            List<String> yetishmagan = new ArrayList<>();
            if (sanaBosh) {
                yetishmagan.add("shartnoma sanasi");
            } else if (sana == null) {
                // Sana yozilgan, lekin o'qib bo'lmaydigan formatda
                yetishmagan.add("shartnoma sanasi (formati noto'g'ri: " + sanaMatn.trim() + ")");
            }
            if (mahsulotYoq)  yetishmagan.add("mahsulot nomi");
            if (tannarxYoq)   yetishmagan.add("tannarx");
            if (joylashuvYoq) yetishmagan.add("joylashuv");
            if (s.getDokonId() == null) yetishmagan.add("sotilgan do'kon");

            if (yetishmagan.isEmpty()) continue;

            ShartnomaRoyxat dto = new ShartnomaRoyxat();
            dto.setId(s.getId());
            dto.setSumma(s.getSumma());
            dto.setMuddat(s.getMuddat());
            dto.setStatus(s.getStatus());

            String fish = ((s.getMijoz() != null && s.getMijoz().getFamiliya() != null) ? s.getMijoz().getFamiliya() : "") +
                    " " +
                    ((s.getMijoz() != null && s.getMijoz().getIsm() != null) ? s.getMijoz().getIsm() : "");
            dto.setFish(fish.trim());
            dto.setTel(s.getMijoz() != null ? s.getMijoz().getTel1() : null);

            dto.setSabab(" " + String.join(", ", yetishmagan));
            natija.add(dto);
        }
        return natija;
    }

    public ApiResponse shartnomaniUndiruvgaOtkazish(Long shartnomaId, String sabab) {
        if (sabab == null || sabab.isBlank()) {
            return new ApiResponse("Sabab kiritilishi shart", false);
        }
        Shartnoma shartnoma = shartnomaRepository.findById(shartnomaId).orElse(null);
        if (shartnoma == null) {
            return new ApiResponse("Shartnoma topilmadi", false);
        }
        shartnoma.setStatus("undiruv");
        shartnoma.setUndiruvSababi(sabab);
        shartnoma.setUndiruvVaqti(LocalDateTime.now());
        shartnomaRepository.save(shartnoma);
        amalService.log("UNDIRUV",
                "Shartnoma #" + shartnomaId + " undiruvga topshirildi (mijoz: " + mijozIsmi(shartnoma.getMijoz()) + "). Sabab: " + sabab,
                shartnoma.getMijoz() != null ? shartnoma.getMijoz().getId() : null, shartnomaId, null);
        return new ApiResponse("Shartnoma undiruvga topshirildi", true);
    }

    // Amallar tarixidagi tavsiflar uchun mijoz ismini xavfsiz shakllantiradi
    private String mijozIsmi(Mijoz mijoz) {
        if (mijoz == null) return "noma'lum mijoz";
        StringBuilder sb = new StringBuilder();
        if (mijoz.getFamiliya() != null) sb.append(mijoz.getFamiliya()).append(" ");
        if (mijoz.getIsm() != null) sb.append(mijoz.getIsm());
        String natija = sb.toString().trim();
        return natija.isEmpty() ? ("mijoz #" + mijoz.getId()) : natija;
    }

    @Transactional
    public ApiResponse undiruvdanChiqarish(Long shartnomaId) {
        Optional<Shartnoma> optional = shartnomaRepository.findById(shartnomaId);
        if (optional.isEmpty()) {
            return new ApiResponse("Shartnoma topilmadi", false);
        }
        Shartnoma shartnoma = optional.get();

        if (!"undiruv".equals(shartnoma.getStatus())) {
            return new ApiResponse("Bu shartnoma undiruvda emas", false);
        }

        // Agar hamma jadval to'langan bo'lsa "yopilgan", aks holda "ochiq" ga qaytaramiz
        boolean hammasiTulangan = shartnoma.getJadvalList() != null
                && !shartnoma.getJadvalList().isEmpty()
                && shartnoma.getJadvalList().stream()
                .allMatch(j -> "tulangan".equals(j.getHolat()));

        shartnoma.setStatus(hammasiTulangan ? "yopilgan" : "ochiq");
        shartnoma.setUndiruvSababi(null);
        shartnoma.setUndiruvVaqti(null);
        shartnomaRepository.save(shartnoma);

        amalService.log(
                "UNDIRUV",
                "№" + shartnomaId + " shartnoma undiruvdan chiqarildi",
                shartnoma.getMijoz() != null ? shartnoma.getMijoz().getId() : null,
                shartnomaId,
                null
        );

        return new ApiResponse("Shartnoma undiruvdan chiqarildi", true);
    }
}