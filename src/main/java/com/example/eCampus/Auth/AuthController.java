package com.example.eCampus.Auth;

import com.example.eCampus.Admin.Admin;
import com.example.eCampus.Learner.Learner;
import com.example.eCampus.User.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Getter
    @Setter
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Getter
    @Setter
    public static class RegisterRequest {
        private String username;
        private String password;
    }

    @Getter
    @Setter
    public static class AdminRegisterRequest extends RegisterRequest {
        @NotNull
        @Min(1)
        @Max(3)
        private Integer privilegeLevel;
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }

    @PostMapping({"/login", "/learner/login"})
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return authenticate(loginRequest, "ROLE_LEARNER");
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginRequest loginRequest) {
        return authenticate(loginRequest, "ROLE_ADMIN");
    }

    private ResponseEntity<?> authenticate(LoginRequest loginRequest, String requiredAuthority) {
        if (loginRequest.getUsername() == null || loginRequest.getUsername().isBlank()
                || loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Username and password are required"));
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

                if (authentication.isAuthenticated() && authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals(requiredAuthority))) {
                String token = jwtService.generateToken(authentication);
                ResponseCookie cookie = ResponseCookie.from("jwt", token)
                        .httpOnly(true)
                        //PROD : should be true in production, but for testing purposes, we set it to false
                        .secure(false)
                        .path("/")
                        .sameSite("Strict")
                        .maxAge(24 * 60 * 60)
                        .build();

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                        .body(Map.of("message", "Login successful"));
            }
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid username or password"));
    }

    @PostMapping({"/register", "/learner/register"})
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        if (registerRequest.getUsername() == null || registerRequest.getUsername().isBlank()
                || registerRequest.getPassword() == null || registerRequest.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Username and password are required"));
        }

        if (userRepository.findByLogin(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Username already exists"));
        }

        Learner user = new Learner();
        user.setLogin(registerRequest.getUsername());
        user.setHashPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Registration successful"));
    }

        @PostMapping("/admin/register")
        public ResponseEntity<?> registerAdmin(@Valid @RequestBody AdminRegisterRequest registerRequest) {
        if (registerRequest.getUsername() == null || registerRequest.getUsername().isBlank()
            || registerRequest.getPassword() == null || registerRequest.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Username and password are required"));
        }

        if (userRepository.findByLogin(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Username already exists"));
        }

        Admin admin = new Admin(null, registerRequest.getUsername(),
            passwordEncoder.encode(registerRequest.getPassword()), registerRequest.getPrivilegeLevel());
        userRepository.save(admin);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("message", "Admin registration successful"));
        }
}
