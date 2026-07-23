package com.example.gilam888;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling          // <-- SMS eslatma scheduler ishlashi uchun QO'SHILDI
@SpringBootApplication
public class Gilam888Application {

    public static void main(String[] args) {
        SpringApplication.run(Gilam888Application.class, args);
    }

}