package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.domain.TokenBlacklist;
import com.navrotskyi.trippyapi.repository.RefreshTokenRepository;
import com.navrotskyi.trippyapi.repository.TokenBlacklistRepository;
import com.navrotskyi.trippyapi.repository.UserRepository;
import com.navrotskyi.trippyapi.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        final String jwt = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwt);

        // Blacklist the access token
        Date expirationDate = jwtService.extractExpiration(jwt);
        if (expirationDate != null) {
            TokenBlacklist blacklistedToken = TokenBlacklist.builder()
                    .token(jwt)
                    .expiryDate(expirationDate.toInstant())
                    .build();
            tokenBlacklistRepository.save(blacklistedToken);
        }

        // Invalidate the refresh token
        if (userEmail != null) {
            userRepository.findByEmail(userEmail)
                    .ifPresent(refreshTokenRepository::deleteByUser);
        }

        SecurityContextHolder.clearContext();
    }
}
