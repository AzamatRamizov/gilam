package com.example.gilam888.Controller;

import com.example.gilam888.Configurations.ApiResponse;
import com.example.gilam888.Dto.MijozDataDto;
import com.example.gilam888.Dto.YaqinJadvalDto;
import com.example.gilam888.Entity.*;
import com.example.gilam888.Repository.*;
import com.example.gilam888.Service.AdminService;
import com.example.gilam888.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final UsersRepository usersRepository;
    private final MijozRepository mijozRepository;
    private final UserService userService;
    private final FaylBaytRepository faylBaytRepository;
    private final JadvalRepository jadvalRepository;
    private final ShartnomaRepository shartnomaRepository;

    public AdminController(AdminService adminService, UsersRepository usersRepository, MijozRepository mijozRepository, UserService userService, FaylBaytRepository faylBaytRepository, JadvalRepository jadvalRepository, ShartnomaRepository shartnomaRepository) {
        this.adminService = adminService;
        this.usersRepository = usersRepository;
        this.mijozRepository = mijozRepository;
        this.userService = userService;
        this.faylBaytRepository = faylBaytRepository;
        this.jadvalRepository = jadvalRepository;
        this.shartnomaRepository = shartnomaRepository;
    }

//    @PreAuthorize("hasRole('owner')")
    @GetMapping("/dashboard")
    public String admin(){
        return "dashboard";
    }

    @GetMapping("/user-add")
    public String userAdd(){
        return "add-page";
    }

    @PreAuthorize("hasRole('owner')")
    @GetMapping("/hodimlar")
    public String hodimlar(){
        return "Admin/hodimlar";
    }

    @PreAuthorize("hasRole('owner')")
    @PostMapping("/add-hodim")
    public ResponseEntity<?> addHodim(@RequestBody Users user){
        ApiResponse apiResponse = adminService.addHodim(user);
        return ResponseEntity.status(apiResponse.isHolat()?200:208).body(apiResponse.getMessage());
    }
    @PreAuthorize("hasRole('owner')")
    @GetMapping("/hodim-delete/{id}")
    public ResponseEntity<?> hodimDelete(@PathVariable Long id){
        ApiResponse apiResponse = adminService.deleteHodim(id);
        return ResponseEntity.status(apiResponse.isHolat()?200:208).body(apiResponse.getMessage());
    }
    @PreAuthorize("hasRole('owner')")
    @PutMapping("/edit-hodim")
    public ResponseEntity<?> editHodim(@RequestBody Users user){
        ApiResponse apiResponse=adminService.editHodim(user);
        return ResponseEntity.status(apiResponse.isHolat()?200:208).body(apiResponse.getMessage());
    }

    @GetMapping("/all-hodim")
    public ResponseEntity<?> allHodim(){
        return ResponseEntity.ok(usersRepository.findAll());
    }

    @GetMapping("/mijozlar")
    public String mijozlar(){
        return "mijozlar";
    }

    @GetMapping("/mijozlar-all")
    public ResponseEntity<?> mijozlarAll(){

        return ResponseEntity.ok(adminService.mijozlar());
    }

    @GetMapping("/mijozlar-delete/{id}")
    public ResponseEntity<?> mijozlarDelete(@PathVariable Long id){
        mijozRepository.deleteById(id);
        return ResponseEntity.ok("Mijoz o'chirildi");
    }

    @PostMapping("/add-mijoz")
    public ResponseEntity<?> addMijoz(@RequestPart("mijoz") MijozDataDto mijoz, @RequestParam(value = "rasm",required = false) MultipartFile passport, @RequestParam(value = "rasm2",required = false) MultipartFile rasm2,  @RequestParam(value = "kafolat1",required = false) MultipartFile kafolat,  @RequestParam(value = "kafolat2",required = false) MultipartFile kafolat2) throws IOException {
        ApiResponse apiResponse=adminService.addMijoz(mijoz, passport, rasm2, kafolat, kafolat2);
        return ResponseEntity.status(apiResponse.isHolat()?200:208).body(apiResponse.getMessage());
    }

    @GetMapping("/my-profile")
    public String myProfile(){
        return "my-profile-page";
    }

    @GetMapping("/shartnomalar")
    public String shartnomalar(){
        return "shartnomalar";
    }

    @GetMapping("/shartnoma-all")
    public ResponseEntity<?> shartnomaAll(){
        return ResponseEntity.ok(adminService.getShartnomaAll());
    }

    @GetMapping("/shartnoma-detail/{id}")
    public ResponseEntity<?> shartnomaDetail(@PathVariable long id){
        return ResponseEntity.ok(adminService.shartnomaDetail(id));
    }
    @GetMapping("/shartnoma-detail")
    public String shartnomaDetail(){
        return "shartnomaDetail";
    }

    @GetMapping("/fayl/{id}")
    public ResponseEntity<byte[]> getFayl(@PathVariable Long id) {
        Optional<FaylBayt> optional = faylBaytRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        FaylBayt fayl = optional.get();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fayl.getContentTypeFayl()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fayl.getOriginalNomi() + "\"")
                .body(fayl.getBayt());
    }

    @PreAuthorize("hasRole('owner')")
    @GetMapping("/magazin")
    public String magazin(){
        return "magazin";
    }

    @PreAuthorize("hasRole('owner')")
    @PostMapping("/add-magazin")
    public ResponseEntity<?> addMagazin(@RequestBody Magazin magazin){
        ApiResponse apiResponse=adminService.addMagazine(magazin);
        return ResponseEntity.status(apiResponse.isHolat()?200:208).body(apiResponse.getMessage());
    }

    @PreAuthorize("hasRole('owner')")
    @GetMapping("/magazin-all")
    public ResponseEntity<?> magazinAll(){
        return ResponseEntity.ok(adminService.getMagazinAll());
    }

    @GetMapping("/my-data")
    public ResponseEntity<?> myData(HttpServletRequest request){
        return ResponseEntity.ok(adminService.getMydata(request));
    }
    @PostMapping("/update-my-data")
    public ResponseEntity<?> updateMyData(@RequestBody Users user){
        ApiResponse apiResponse=adminService.updateMyData(user);
        return ResponseEntity.status(apiResponse.isHolat()?200:208).body(apiResponse.getMessage());
    }
    @PutMapping("/change-my-password")
    public ResponseEntity<?> changePassword(@RequestBody Users users){
        ApiResponse apiResponse=adminService.changePassword(users);
        return ResponseEntity.status(apiResponse.isHolat()?200:208).body(apiResponse.getMessage());
    }
    @GetMapping("/get-magazinlar")
    public ResponseEntity<?> magazinlar(){
        return ResponseEntity.ok(adminService.getMagazinlar());
    }
    @PutMapping("/tulov/{id}")
    public ResponseEntity<?> tulov(@PathVariable Long id, @RequestParam("summa") long summa, @RequestParam("turi") String turi, @RequestParam("sana") LocalDateTime sana, @RequestParam("dokon") Long dokonId){
        ApiResponse apiResponse=adminService.tulov(id,summa,turi,sana);
        return ResponseEntity.status(apiResponse.isHolat()?200:208).body(apiResponse.getMessage());
    }
    @GetMapping("/get-last-muddat")
    public ResponseEntity<?> getLastMuddat(){
        LocalDate bugun   = LocalDate.now();
        LocalDateTime oyBoshi  = bugun.withDayOfMonth(1).atStartOfDay();
        LocalDateTime oyOxiri  = oyBoshi.plusMonths(1);

        List<Jadval> jadvallar = jadvalRepository.findCurrentMonthUnpaid(oyBoshi, oyOxiri);

        List<YaqinJadvalDto> result = new ArrayList<>();

        for (Jadval jadval : jadvallar) {
            // Shu jadval qaysi shartnomaga tegishli?
            Shartnoma shartnoma = shartnomaRepository
                    .findByJadvalListContaining(jadval)
                    .orElse(null);

            if (shartnoma == null) continue;

            var mijoz = shartnoma.getMijoz();
            if (mijoz == null) continue;

            String fish = String.join(" ",
                    nvl(mijoz.getFamiliya()),
                    nvl(mijoz.getIsm()),
                    nvl(mijoz.getSharif())
            ).trim();

            YaqinJadvalDto dto = new YaqinJadvalDto();
            dto.setJadvalId(jadval.getId());
            dto.setSumma(jadval.getSumma());
            dto.setTulangan(jadval.getTulangan());
            dto.setHolat(jadval.getHolat());
            dto.setSana(jadval.getSana());
            dto.setMijozFish(fish);
            dto.setMijozTel(nvl(mijoz.getTel1()));
            dto.setShartnomaId(shartnoma.getId());
            dto.setShartnomaSumma(shartnoma.getSumma());
            dto.setShartnomaMuddat(shartnoma.getMuddat() + " oy");

            result.add(dto);
        }

        return ResponseEntity.ok(result);
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }
}
