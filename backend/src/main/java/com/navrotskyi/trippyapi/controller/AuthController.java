package com.navrotskyi.trippyapi.controller;

import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.domain.VerificationToken;
import com.navrotskyi.trippyapi.dto.AuthResponse;
import com.navrotskyi.trippyapi.dto.LoginRequest;
import com.navrotskyi.trippyapi.dto.RegisterRequest;
import com.navrotskyi.trippyapi.repository.UserRepository;
import com.navrotskyi.trippyapi.repository.VerificationTokenRepository;
import com.navrotskyi.trippyapi.service.AuthenticationService;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService service;
    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public AuthController(AuthenticationService service,
                          VerificationTokenRepository tokenRepository,
                          UserRepository userRepository) {
        this.service = service;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @GetMapping("/register")
    public ResponseEntity<RegisterRequest> getRegisterInfo() {
        return ResponseEntity.ok(new RegisterRequest("John Doe", "john@example.com", "password123"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @GetMapping("/login")
    public ResponseEntity<LoginRequest> getLoginInfo() {
        return ResponseEntity.ok(new LoginRequest("john@example.com", "password123"));
    }

    @GetMapping("/check")
    public ResponseEntity<String> check() {
        return ResponseEntity.ok("Authorized");
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        VerificationToken vToken = tokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Nieprawidłowy lub nieistniejący token!"));

        if (vToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Błąd: Token weryfikacyjny wygasł!");
        }

        User user = vToken.getUser();
        user.setVerified(true);
        userRepository.save(user);

        tokenRepository.delete(vToken);

        return ResponseEntity.ok("Sukces! Twoje konto zostało pomyślnie zweryfikowane.");
    }
}