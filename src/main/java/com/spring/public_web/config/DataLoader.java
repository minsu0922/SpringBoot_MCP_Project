package com.spring.public_web.config;

import com.spring.public_web.entity.User;
import com.spring.public_web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // 관리자 계정이 없으면 생성
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ROLE_ADMIN");
            admin.setEnabled(true);
            
            userRepository.save(admin);
            System.out.println("===========================================");
            System.out.println("테스트 관리자 계정이 생성되었습니다.");
            System.out.println("ID: admin");
            System.out.println("PW: admin123");
            System.out.println("===========================================");
        }
    }
}
