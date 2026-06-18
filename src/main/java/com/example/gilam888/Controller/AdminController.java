package com.example.gilam888.Controller;

import com.example.gilam888.Configurations.ApiResponse;
import com.example.gilam888.Dto.MijozDataDto;
import com.example.gilam888.Entity.Users;
import com.example.gilam888.Repository.MijozRepository;
import com.example.gilam888.Repository.UsersRepository;
import com.example.gilam888.Service.AdminService;
import com.example.gilam888.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final UsersRepository usersRepository;
    private final MijozRepository mijozRepository;
    private final UserService userService;

    public AdminController(AdminService adminService, UsersRepository usersRepository, MijozRepository mijozRepository, UserService userService) {
        this.adminService = adminService;
        this.usersRepository = usersRepository;
        this.mijozRepository = mijozRepository;
        this.userService = userService;
    }

    //    @PreAuthorize("hasRole('user')")
    @GetMapping("/dashboard")
    public String admin(){
        return "dashboard";
    }
    @GetMapping("/user-add")
    public String userAdd(){
        return "add-page";
    }

    @GetMapping("/hodimlar")
    public String hodimlar(){
        return "Admin/hodimlar";
    }

    @PostMapping("/add-hodim")
    public ResponseEntity<?> addHodim(@RequestBody Users user){
        ApiResponse apiResponse = adminService.addHodim(user);
        return ResponseEntity.status(apiResponse.isHolat()?200:208).body(apiResponse.getMessage());
    }
    @GetMapping("/hodim-delete/{id}")
    public ResponseEntity<?> hodimDelete(@PathVariable Long id){
        ApiResponse apiResponse = adminService.deleteHodim(id);
        return ResponseEntity.status(apiResponse.isHolat()?200:208).body(apiResponse.getMessage());
    }
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
}
