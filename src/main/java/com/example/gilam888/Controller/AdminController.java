package com.example.gilam888.Controller;

import com.example.gilam888.Configurations.ApiResponse;
import com.example.gilam888.Dto.MijozDataDto;
import com.example.gilam888.Entity.FaylBayt;
import com.example.gilam888.Entity.Users;
import com.example.gilam888.Repository.FaylBaytRepository;
import com.example.gilam888.Repository.MijozRepository;
import com.example.gilam888.Repository.UsersRepository;
import com.example.gilam888.Service.AdminService;
import com.example.gilam888.Service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final UsersRepository usersRepository;
    private final MijozRepository mijozRepository;
    private final UserService userService;
    private final FaylBaytRepository faylBaytRepository;

    public AdminController(AdminService adminService, UsersRepository usersRepository, MijozRepository mijozRepository, UserService userService, FaylBaytRepository faylBaytRepository) {
        this.adminService = adminService;
        this.usersRepository = usersRepository;
        this.mijozRepository = mijozRepository;
        this.userService = userService;
        this.faylBaytRepository = faylBaytRepository;
    }

    @PreAuthorize("hasRole('owner')")
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
        System.out.println(id);
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
//    public ResponseEntity<byte[]> getFile(@PathVariable long id) {
//
//        FaylBayt fayl = faylBaytRepository.findById(id).orElseThrow();
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.IMAGE_JPEG)
//                .body(fayl.getBayt());
//    }

}
