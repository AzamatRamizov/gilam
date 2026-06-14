package com.example.gilam888.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @PreAuthorize("hasRole('owner')")
    @GetMapping("/dashboard")
    public String admin(){
        return "dashboard";
    }
    @GetMapping("/user-add")
    public String userAdd(){
        return "add-page";
    }
}
