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
    private final AmalService amalService;

    public AdminService(UsersRepository usersRepository, PasswordEncoder passwordEncoder, MijozRepository mijozRepository, FaylBaytRepository faylBaytRepository, ShartnomaRepository shartnomaRepository, MagazinRepository magazinRepository, TokenGenerator tokenGenerator, JadvalRepository jadvalRepository, PaymentRepository paymentRepository, TelegramXabarService telegramXabarService, AmalService amalService) {
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
        shartnoma.setSotibOlinganSana(String.valueOf(mijoz.getShartnomaSana()));
        shartnoma.setCreatedTime(mijoz.getShartnomaSana());
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
                s.getJoylashuv()
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
        s.setSotibOlinganSana(dto.getSana());     // String → String, parse shart emas
        s.setJoylashuv(dto.getLokatsiya());
        shartnomaRepository.save(s);

        amalService.log("SHARTNOMA_TAHRIR",
                "Shartnoma #" + s.getId() + " mahsulot ma'lumotlari tahrirlandi: " + dto.getNomi(),
                s.getMijoz() != null ? s.getMijoz().getId() : null, s.getId(), null);

        return shartnomaDetail(s.getId());
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

    public ApiResponse tulov(Long id, long summa, String turi, LocalDateTime sana, Long dokonId) {
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

        shartnoma.setMuddat(mijoz.getMuddat());
        shartnoma.setCreatedTime(mijoz.getShartnomaSana());
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
    public List<ShartnomaRoyxat> getShartnomaWithMissingInfo() {
        List<Shartnoma> list = shartnomaRepository.findShartnomaWithMissingInfo();

        return list.stream().map(s -> {
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

            boolean mahsulotYoq  = s.getMahsulot()  == null || s.getMahsulot().trim().isEmpty();
            boolean tannarxYoq   = s.getTannarx()   == null || s.getTannarx().trim().isEmpty();
            boolean joylashuvYoq = s.getJoylashuv() == null || s.getJoylashuv().trim().isEmpty();

            List<String> yetishmagan = new ArrayList<>();
            if (mahsulotYoq)  yetishmagan.add("mahsulot nomi");
            if (tannarxYoq)   yetishmagan.add("tannarx");
            if (joylashuvYoq) yetishmagan.add("joylashuv");

            String sabab = yetishmagan.isEmpty()
                    ? "—"
                    : (yetishmagan.size() == 1 ? " " : " ") + String.join(", ", yetishmagan);
            dto.setSabab(sabab);

            return dto;
        }).toList();
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
}