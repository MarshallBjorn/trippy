package com.navrotskyi.trippyapi.controller;

import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.domain.VerificationToken;
import com.navrotskyi.trippyapi.dto.LoginRequest;
import com.navrotskyi.trippyapi.dto.RegisterRequest;
import com.navrotskyi.trippyapi.repository.UserRepository;
import com.navrotskyi.trippyapi.repository.VerificationTokenRepository;
import com.navrotskyi.trippyapi.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication (Legacy)", description = "Additional or legacy authentication endpoints")
public class AuthController {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public AuthController(AuthenticationService service,
                          VerificationTokenRepository tokenRepository,
                          UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/register")
    @Operation(summary = "Get registration info payload template")
    public ResponseEntity<RegisterRequest> getRegisterInfo() {
        return ResponseEntity.ok(new RegisterRequest("John Doe", "john@example.com", "password123"));
    }

    @GetMapping("/login")
    @Operation(summary = "Get login payload template")
    public ResponseEntity<LoginRequest> getLoginInfo() {
        return ResponseEntity.ok(new LoginRequest("john@example.com", "password123"));
    }

    @GetMapping("/check")
    @Operation(summary = "Check authentication status")
    public ResponseEntity<String> check() {
        return ResponseEntity.ok("Authorized");
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify email via token", description = "Confirms a user's email address using the token sent to their inbox.")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam("token") String token) {
        
        var vTokenOpt = tokenRepository.findByToken(token);
        
        if (vTokenOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Nieprawidłowy lub nieistniejący token!"));
        }

        VerificationToken vToken = vTokenOpt.get();

        if (vToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Błąd: Token weryfikacyjny wygasł!"));
        }

        User user = vToken.getUser();
        user.setVerified(true);
        userRepository.save(user);

        tokenRepository.delete(vToken);

        return ResponseEntity.ok(Map.of("message", "Sukces! Twoje konto zostało pomyślnie zweryfikowane."));
    }
}