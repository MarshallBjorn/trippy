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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh")
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
}
