package com.example.gilam888.Controller;

import com.example.gilam888.Configurations.ApiResponse;
import com.example.gilam888.Configurations.TokenGenerator;
import com.example.gilam888.Dto.*;
import com.example.gilam888.Entity.*;
import com.example.gilam888.Repository.*;
import com.example.gilam888.Service.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

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
    private final KassaService kassaService;
    private final QongiroqService qongiroqService;
    private final AmalService amalService;
    private final TokenGenerator tokenGenerator;
    private final MijozSahifaService mijozSahifaService;
    private final SmsHisobService smsHisobService;

    public AdminController(AdminService adminService, UsersRepository usersRepository, MijozRepository mijozRepository, UserService userService, FaylBaytRepository faylBaytRepository, JadvalRepository jadvalRepository, ShartnomaRepository shartnomaRepository, KassaService kassaService, QongiroqService qongiroqService, TokenGenerator tokenGenerator, AmalService amalService, MijozSahifaService mijozSahifaService, SmsHisobService smsHisobService) {
        this.adminService = adminService;
        this.usersRepository = usersRepository;
        this.mijozRepository = mijozRepository;
        this.userService = userService;
        this.faylBaytRepository = faylBaytRepository;
        this.jadvalRepository = jadvalRepository;
        this.shartnomaRepository = shartnomaRepository;
        this.kassaService = kassaService;
        this.qongiroqService = qongiroqService;
        this.tokenGenerator = tokenGenerator;
        this.amalService = amalService;
        this.mijozSahifaService = mijozSahifaService;
        this.smsHisobService = smsHisobService;
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

    @PreAuthorize("hasRole('owner')")
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

    @PostMapping(value = "/update-mijoz", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editMijoz(
            @RequestPart("mijoz") MijozDataDto mijoz,
            @RequestParam(value = "rasm", required = false) MultipartFile passport,
            @RequestParam(value = "rasm2", required = false) MultipartFile passport2) throws IOException {

        ApiResponse apiResponse = adminService.editMijoz(mijoz, passport, passport2);
        return ResponseEntity.status(apiResponse.isHolat() ? 200 : 208)
                .body(Map.of("message", apiResponse.getMessage()));
    }

    @PostMapping(value = "/update-shartnoma-kafolat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editShartnomaKafolat(
            @RequestParam("shartnomaId") Long shartnomaId,
            @RequestParam(value = "kafolat1", required = false) MultipartFile kafolat1,
            @RequestParam(value = "kafolat2", required = false) MultipartFile kafolat2) throws IOException {

        ApiResponse apiResponse = adminService.editShartnomaKafolat(shartnomaId, kafolat1, kafolat2);
        return ResponseEntity.status(apiResponse.isHolat() ? 200 : 208)
                .body(Map.of("message", apiResponse.getMessage()));
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

    @PutMapping("/update-muddat")
    public ResponseEntity<?> updateMuddat(@RequestParam Long shartnomaId, @RequestParam long muddat) {
        return ResponseEntity.ok(adminService.muddatOzgartirish(shartnomaId, muddat));
    }

    /** Bitta jadval qatorining to'lov sanasini o'zgartirish. sana formati: yyyy-MM-dd */
    @PutMapping("/update-jadval-sana")
    public ResponseEntity<?> updateJadvalSana(
            @RequestParam Long jadvalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sana) {
        return ResponseEntity.ok(adminService.jadvalSanaOzgartirish(jadvalId, sana));
    }

    @PostMapping("/update-mahsulot")
    public ResponseEntity<?> updateMahsulot(@RequestBody MahsulotDto dto){
        return ResponseEntity.ok(adminService.updateMahsulot(dto));
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
    //    @PutMapping("/tulov/{id}")
//    public ResponseEntity<?> tulov(@PathVariable Long id, @RequestParam("summa") long summa, @RequestParam("turi") String turi, @RequestParam("sana") LocalDateTime sana, @RequestParam("dokon") Long dokonId){
//        ApiResponse apiResponse=adminService.tulov(id,summa,turi,sana,dokonId);
//        return ResponseEntity.status(apiResponse.isHolat()?200:208).body(apiResponse.getMessage());
//    }
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
            dto.setMijozId(mijoz.getId());
            dto.setJadvalId(jadval.getId());
            dto.setSumma(jadval.getSumma());
            dto.setTulangan(jadval.getTulangan());
            dto.setHolat(jadval.getHolat());
            dto.setSana(jadval.getSana());
            dto.setMijozFish(fish);
            dto.setMijozTel(nvl(mijoz.getTel1()));
            dto.setMijozTel2(nvl(mijoz.getTel2()));
            dto.setMijozTel3(nvl(mijoz.getTel3()));
            dto.setShartnomaId(shartnoma.getId());
            dto.setShartnomaSumma(shartnoma.getSumma());
            dto.setShartnomaMuddat(shartnoma.getMuddat() + " oy");
            // Bu oy to'lovi grafikda nechanchi to'lov ekani (call markazida ko'rsatiladi)
            dto.setOyRaqami(AdminService.oyRaqami(shartnoma.getJadvalList(), jadval));
            dto.setJamiOy(shartnoma.getMuddat());

            result.add(dto);
        }

        return ResponseEntity.ok(result);
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }

    //    @PreAuthorize("hasRole('owner')")
    @GetMapping("/kassa")
    public String kassa(org.springframework.ui.Model model){
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isOwner = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_owner".equals(a.getAuthority()));
        model.addAttribute("isOwner", isOwner);
        return "Admin/kassa";
    }

    //    @PreAuthorize("hasRole('owner')")
    @GetMapping("/get-today-payment")
    public ResponseEntity<?> getTodayPay(){
        return ResponseEntity.ok(adminService.getTodayPayment());
    }

    @PostMapping("/add-shartnoma")
    public ResponseEntity<?> addShartnoma(@RequestPart("mijoz") MijozDataDto mijoz, @RequestParam(value = "rasm",required = false) MultipartFile rasm1, @RequestParam(value = "rasm2",required = false) MultipartFile rasm2) throws IOException {
        ApiResponse apiResponse=adminService.addShartnoma(mijoz, rasm1,rasm2);
        return ResponseEntity.status(apiResponse.isHolat()?200:208).body(apiResponse.getMessage());
    }
    @GetMapping("/get-overdue-debts")
    public ResponseEntity<?> getOverdueDebts(){
        return ResponseEntity.ok(adminService.getOverdueDebts());
    }
    @GetMapping("/checkPassport")
    public ResponseEntity<?> checkPassport(@RequestParam("passport") String passport){
        ApiResponse apiResponse=adminService.checkPassport(passport);
        return ResponseEntity.status(apiResponse.isHolat()?200:208).body(apiResponse.getMessage());
    }
    @GetMapping("/dashboard-data")
    public ResponseEntity<?> dashboardData(){
        return ResponseEntity.ok(adminService.getDashboardData());
    }
    @PreAuthorize("hasRole('owner')")
    @GetMapping("/foyda")
    public String foydaPage(){
        return "Admin/foyda";
    }
    @GetMapping("/get-foyda/{davr}")
    public ResponseEntity<?> getFoyda(
            @PathVariable("davr") String davr,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to
    ){
        return ResponseEntity.ok(adminService.getFoyda(davr, from, to));
    }

    @GetMapping("/statistika")
    public String statistikaPage(){
        return "Admin/statistika";
    }

    @GetMapping("/get-statistika")
    public ResponseEntity<?> getStatistika(){
        return ResponseEntity.ok(adminService.getStatistika());
    }

    /** Kalendar statistikasi: tanlangan oyning kunlari bo'yicha shartnomalar. */
    @GetMapping("/get-statistika-kalendar")
    public ResponseEntity<?> getStatistikaKalendar(
            @RequestParam(required = false) Integer yil,
            @RequestParam(required = false) Integer oy) {
        return ResponseEntity.ok(adminService.getKalendarStatistika(yil, oy));
    }

    /** Do'konlar bo'yicha statistika: sotgan do'kon kesimida soni/summa/tannarx/foyda. yil=null — barcha davr. */
    @GetMapping("/get-statistika-dokon")
    public ResponseEntity<?> getStatistikaDokon(
            @RequestParam(required = false) Integer yil) {
        return ResponseEntity.ok(adminService.getDokonStatistika(yil));
    }
    @PreAuthorize("hasRole('owner')")
    @DeleteMapping("/delete-shartnoma/{id}")
    public ResponseEntity<?> deleteShartnoma(@PathVariable Long id) {
        ApiResponse apiResponse = adminService.deleteShartnoma(id);
        return ResponseEntity.status(apiResponse.isHolat() ? 200 : 208).body(apiResponse.getMessage());
    }
    @GetMapping("/call-markaz")
    public String callMarkaz(){
        return "call-markaz";
    }

    // ─────────── SO'NGI AMALLAR (audit log) ───────────

    @GetMapping("/amallar")
    public String amallarPage() {
        return "Admin/amallar";
    }

    @GetMapping("/get-amallar")
    public ResponseEntity<?> getAmallar(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String amalTuri,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sana,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate boshi,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate oxiri,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "vaqt") String sort,
            @RequestParam(defaultValue = "desc") String dir) {
        return ResponseEntity.ok(amalService.getAmallar(userId, amalTuri, sana, boshi, oxiri, page, size, sort, dir));
    }

    @GetMapping("/get-amal-userlar")
    public ResponseEntity<?> getAmalUserlar() {
        return ResponseEntity.ok(amalService.getAmalUserlar());
    }

    // ─────────── SMS NAZORATI (Eskiz balans va tarix) ───────────

    @GetMapping("/sms-nazorat")
    public String smsNazoratPage() {
        return "Admin/sms-nazorat";
    }

    // Stat kartalar: balans, bugun/oy/jami sarf va SMS soni
    @GetMapping("/get-sms-stat")
    public ResponseEntity<?> getSmsStat() {
        return ResponseEntity.ok(smsHisobService.getSmsStat());
    }

    // Yuborilgan SMSlar ro'yxati (filtr + sort + pagination)
    @GetMapping("/get-sms-tarix")
    public ResponseEntity<?> getSmsTarix(
            @RequestParam(required = false) String turi,
            @RequestParam(required = false) String holat,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sana,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate boshi,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate oxiri,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "vaqt") String sort,
            @RequestParam(defaultValue = "desc") String dir) {
        return ResponseEntity.ok(smsHisobService.getSmsTarix(turi, holat, sana, boshi, oxiri, page, size, sort, dir));
    }

    // Balansni yangilash: rejim="qoshish" (hisob to'ldirildi) yoki "belgilash" (aniq qiymat)
    @PostMapping("/sms-balans")
    public ResponseEntity<?> smsBalansYangilash(@RequestParam long summa,
                                                @RequestParam(defaultValue = "qoshish") String rejim) {
        if (summa < 0) {
            return ResponseEntity.badRequest().body("Summa manfiy bo'lishi mumkin emas");
        }
        return ResponseEntity.ok(smsHisobService.balansYangilash(summa, rejim));
    }

    @GetMapping("/undirish")
    public String undirish(){
        return "undirish";
    }
    @GetMapping("/get-qarzdorlar")
    public ResponseEntity<?> getQarzdorlar(@RequestParam(defaultValue = "barchasi") String filter) {
        return ResponseEntity.ok(adminService.getQarzdorlar(filter));
    }

    @GetMapping("/get-all-summa-stat")
    public ResponseEntity<?> getAllSumma(){
        return ResponseEntity.ok(adminService.getAllSumma());
    }

    //    @PreAuthorize("hasRole('owner')")
    @GetMapping("/get-kassa-stat")
    public ResponseEntity<?> getKassaStat(@RequestParam(defaultValue = "bugun") String period, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime boshi,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime oxiri) {
        return ResponseEntity.ok(kassaService.getKassaStat(period, boshi, oxiri));
    }

    // Owner kassadagi to'lov(lar)ni qabul qilganini belgilaydi ("Qabul qildim" tugmasi)
    @PreAuthorize("hasRole('owner')")
    @PutMapping("/tulov-qabul")
    public ResponseEntity<?> tulovQabul(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(kassaService.tulovQabulQilish(ids));
    }

    // Barcha qabul qilinmagan to'lovlar (davrdan qat'i nazar) — kassa sahifasidagi panel uchun
    @GetMapping("/get-qabul-qilinmagan")
    public ResponseEntity<?> getQabulQilinmagan() {
        return ResponseEntity.ok(kassaService.getQabulQilinmaganlar());
    }
    @PreAuthorize("hasRole('owner')")
    @GetMapping("/check-status")
    public ResponseEntity<?> checkStatus(){
        return ResponseEntity.ok(adminService.checkAllShartnoma());
    }

    @PutMapping("/tulov-shartnoma")
    public ResponseEntity<?> tulovShartnoma(
            @RequestParam Long shartnomaId,
            @RequestParam long summa,
            @RequestParam String turi,
            @RequestParam Long dokonId) {
        return ResponseEntity.ok(adminService.tulovShartnoma(shartnomaId, summa, turi, dokonId));
    }

    @GetMapping("/get-missing-info-shartnoma")
    public ResponseEntity<?> getMissingInfoShartnoma() {
        return ResponseEntity.ok(adminService.getShartnomaWithMissingInfo());
    }
    @PostMapping(value = "/add-qongiroq", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse> addQongiroq(
            @RequestParam Long mijozId,
            @RequestParam String natija,
            @RequestParam(required = false) String izoh,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vadaSana,
            @RequestParam(required = false) MultipartFile audio,
            HttpServletRequest request) {

        QongiroqSaveDto dto = new QongiroqSaveDto();
        dto.setMijozId(mijozId);
        dto.setNatija(natija);
        dto.setIzoh(izoh);
        dto.setVadaSana(vadaSana);

        Users user = adminService.getMydata(request);
        String operator = (user != null) ? user.getUsername() : "Noma'lum";

        ApiResponse response = qongiroqService.addQongiroq(dto, audio, operator);
        return ResponseEntity.status(response.isHolat() ? 200 : 400).body(response);
    }

    @GetMapping("/qongiroq-tarixi/{mijozId}")
    public ResponseEntity<List<QongiroqTarixiDto>> getQongiroqTarixi(@PathVariable Long mijozId) {
        return ResponseEntity.ok(qongiroqService.getTarixi(mijozId));
    }

    @GetMapping("/bugungi-eslatmalar")
    public ResponseEntity<?> getBugungiEslatmalar() {
        return ResponseEntity.ok(qongiroqService.getBugungiEslatmalar());
    }

    @GetMapping("/qongiroq-audio/{tarixiId}")
    public ResponseEntity<byte[]> getQongiroqAudio(@PathVariable Long tarixiId) {
        QongiroqAudio audio = qongiroqService.getAudio(tarixiId);
        if (audio == null || audio.getBayt() == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .header("Content-Type", audio.getContentType())
                .body(audio.getBayt());
    }

    @PostMapping("/to-undiruv")
    public ResponseEntity<ApiResponse> toUndiruv(@RequestParam Long shartnomaId,
                                                 @RequestParam String sabab) {
        ApiResponse response = adminService.shartnomaniUndiruvgaOtkazish(shartnomaId, sabab);
        return ResponseEntity.status(response.isHolat() ? 200 : 400).body(response);
    }

    /** Mijoz sahifasi (Thymeleaf). Ochilishi: /admin/mijoz?id=123 */
    @GetMapping("/mijoz")
    public String mijozSahifa() {
        return "mijoz";
    }

    /** Sahifa uchun JSON ma'lumot */
    @GetMapping("/mijoz-sahifa/{id}")
    public ResponseEntity<?> mijozSahifaInfo(@PathVariable long id) {
        return ResponseEntity.ok(mijozSahifaService.getMijozSahifa(id));
    }

    /** Statusni qo'lda o'zgartirish. status: yaxshi | qisman | yomon | avto */
    @PostMapping("/mijoz-status")
    public ResponseEntity<ApiResponse> mijozStatus(@RequestParam long mijozId,
                                                   @RequestParam String status) {
        try {
            mijozSahifaService.setStatus(mijozId, status);
            return ResponseEntity.ok(new ApiResponse("Status saqlandi", true));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse("Xatolik: " + e.getMessage(), false));
        }
    }
    @PreAuthorize("hasRole('owner')")
    @PostMapping("/undiruvdan-chiqarish")
    @ResponseBody
    public ApiResponse undiruvdanChiqarish(@RequestParam Long shartnomaId) {
        return adminService.undiruvdanChiqarish(shartnomaId);
    }

}