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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public AdminService(UsersRepository usersRepository, PasswordEncoder passwordEncoder, MijozRepository mijozRepository, FaylBaytRepository faylBaytRepository, ShartnomaRepository shartnomaRepository, MagazinRepository magazinRepository, TokenGenerator tokenGenerator, JadvalRepository jadvalRepository) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.mijozRepository = mijozRepository;
        this.faylBaytRepository = faylBaytRepository;
        this.shartnomaRepository = shartnomaRepository;
        this.magazinRepository = magazinRepository;
        this.tokenGenerator = tokenGenerator;
        this.jadvalRepository = jadvalRepository;
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
        return new ApiResponse("Hodim qo'shildi",true);
    }

    public ApiResponse deleteHodim(Long id) {
        usersRepository.deleteById(id);
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
            shartnomaRoyxats.add(shartnomaRoyxat);
        }
        return shartnomaRoyxats;
    }

    public Object shartnomaDetail(long id) {
        Optional<Shartnoma> byId = shartnomaRepository.findById(id);
        return byId.get();
    }

    public ApiResponse addMagazine(Magazin magazin) {
        if(magazinRepository.findByNomi(magazin.getNomi()).isPresent()){
            return new ApiResponse("Bu nomi allaqachon ishlatilgan",false);
        }
        Magazin magazin1 = new Magazin();
        magazin1.setManzil(magazin.getManzil());
        magazin1.setNomi(magazin.getNomi());
        magazinRepository.save(magazin1);
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
//        if(byUsername.isEmpty()){
//            return new ApiResponse("User topilmadi",false);
//        }
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
        return new ApiResponse("Muvaffaqiyatli tulov qilindi",true);
    }

    public Object getTodayPayment() {
        LocalDateTime boshi = LocalDate.now().atStartOfDay();           // 2026-06-24T00:00:00
        LocalDateTime oxiri = boshi.plusDays(1);                        // 2026-06-25T00:00:00

        List<Jadval> bugungiTolovlar = jadvalRepository.findTodayPayments(boshi, oxiri);
        List<Statistikaga> statistikagas=new ArrayList<>();
        for (Jadval jadval : bugungiTolovlar) {
            Statistikaga statistikaga = new Statistikaga();
            Optional<Magazin> dokon = magazinRepository.findById(Long.valueOf(jadval.getDokonId()));
            statistikaga.setDokon(dokon.get().getNomi());
            statistikaga.setTuri(jadval.getTuri());
            statistikaga.setSana(jadval.getTulovSana());
            Shartnoma shartnoma = shartnomaRepository.findByJadvalListContaining(jadval).get();
            statistikaga.setShartnomaId(shartnoma.getId());
            statistikaga.setMijoz(shartnoma.getMijoz().getIsm()+" "+shartnoma.getMijoz().getFamiliya()+" "+shartnoma.getMijoz().getSharif());
            statistikaga.setSumma(jadval.getSumma());
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
        return new ApiResponse("Shartnoma yaratildi",true);
    }
//
//    public Object getOverdueDebts() {
//
//    }
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
                    dto.setMijoz(m.getFamiliya() + " " + m.getIsm());
                    dto.setTel(m.getTel1());
                }
            });

            dto.setOxirgiTolov(jadval.getTulovSana());

            result.add(dto);
        }
        return result;
    }
}
