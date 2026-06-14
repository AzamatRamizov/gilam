package com.example.gilam888.Configurations;

import com.example.gilam888.Entity.Users;
import com.example.gilam888.Repository.UsersRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if(usersRepository.count()==0){
            usersRepository.save(new Users("owner",passwordEncoder.encode("1"),"owner"));
        }
    }
}
