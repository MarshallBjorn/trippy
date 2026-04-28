package com.navrotskyi.trippyapi.controller;

import com.navrotskyi.trippyapi.dto.LoginRequest;
import com.navrotskyi.trippyapi.dto.AuthResponse;
import com.navrotskyi.trippyapi.dto.RefreshTokenRequest;
import com.navrotskyi.trippyapi.dto.RegisterRequest;
import com.navrotskyi.trippyapi.exception.TokenRefreshException;
import com.navrotskyi.trippyapi.domain.RefreshToken;
import com.navrotskyi.trippyapi.security.JwtService;
import com.navrotskyi.trippyapi.service.AuthenticationService;
import com.navrotskyi.trippyapi.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and token refresh")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user and sends a verification email.")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates a user and returns an access token along with a refresh token.")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Uses a valid refresh token to obtain a new access token.")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtService.generateToken(user);
                    return ResponseEntity.ok(AuthResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(request.getRefreshToken())
                            .build());
                })
                .orElseThrow(() -> new TokenRefreshException(request.getRefreshToken(), "Refresh token is not in database!"));
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify email via token", 
            description = "Confirms a user's email address using the token sent to their inbox.")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam("token") String token) {
        authenticationService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Sukces! Twoje konto zostało pomyślnie zweryfikowane."));
    }
}
