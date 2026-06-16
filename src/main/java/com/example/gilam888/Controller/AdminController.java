package com.example.gilam888.Controller;

import com.example.gilam888.Configurations.ApiResponse;
import com.example.gilam888.Entity.Mijoz;
import com.example.gilam888.Entity.Users;
import com.example.gilam888.Repository.MijozRepository;
import com.example.gilam888.Repository.UsersRepository;
import com.example.gilam888.Service.AdminService;
import com.example.gilam888.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final UsersRepository usersRepository;
    private final MijozRepository mijozRepository;

    public AdminController(AdminService adminService, UsersRepository usersRepository, MijozRepository mijozRepository) {
        this.adminService = adminService;
        this.usersRepository = usersRepository;
        this.mijozRepository = mijozRepository;
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
        return ResponseEntity.ok(mijozRepository.findAll());
    }
    @GetMapping("/mijozlar-delete/{id}")
    public ResponseEntity<?> mijozlarDelete(@PathVariable Long id){
        mijozRepository.deleteById(id);
        return ResponseEntity.ok("Mijoz o'chirildi");
    }
    @PostMapping("/add-mijoz")
    public ResponseEntity<?> addMijoz(@RequestBody Mijoz mijoz){
        ApiResponse apiResponse=adminService.addMijoz(mijoz);
        return ResponseEntity.status(apiResponse.isHolat()?200:208).body(apiResponse.getMessage());
    }
}
