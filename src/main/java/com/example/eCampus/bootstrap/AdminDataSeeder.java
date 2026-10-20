package com.example.eCampus.bootstrap;

import com.example.eCampus.Admin.Admin;
import com.example.eCampus.User.User;
import com.example.eCampus.User.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class AdminDataSeeder implements StartupDataSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.username:admin}") String username,
            @Value("${app.bootstrap-admin.password:admin}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void seed() {
        User existingUser = userRepository.findByLogin(username).orElse(null);
        if (existingUser != null) {
            if (!(existingUser instanceof Admin)) {
                throw new IllegalStateException("Bootstrap admin username is already used by a non-admin account");
            }
            return;
        }
        Admin admin = new Admin(null, username, passwordEncoder.encode(password), 1);
        userRepository.save(admin);
    }
}