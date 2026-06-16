package com.example.gilam888.Service;

import com.example.gilam888.Configurations.ApiResponse;
import com.example.gilam888.Entity.Mijoz;
import com.example.gilam888.Entity.Users;
import com.example.gilam888.Repository.MijozRepository;
import com.example.gilam888.Repository.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final MijozRepository mijozRepository;

    public AdminService(UsersRepository usersRepository, PasswordEncoder passwordEncoder, MijozRepository mijozRepository) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.mijozRepository = mijozRepository;
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

    public ApiResponse addMijoz(Mijoz mijoz) {
        if(mijozRepository.findByPassport(mijoz.getPassport()).isPresent()){
            return new ApiResponse("Bu passport allaqachon ishlatilgan",false);
        }
        Mijoz mijoz1 = new Mijoz();
        mijoz1.setIsm(mijoz.getIsm());
        mijoz1.setTel(mijoz.getTel());
        mijoz1.setFamiliya(mijoz.getFamiliya());
        mijoz1.setSharif(mijoz.getSharif());
        mijoz1.setTuman(mijoz.getTuman());
        mijoz1.setManzil(mijoz.getManzil());
        mijoz1.setPassport(mijoz.getPassport());
        mijozRepository.save(mijoz1);
        return new ApiResponse("Mijoz qo'shildi",true);
    }
}
