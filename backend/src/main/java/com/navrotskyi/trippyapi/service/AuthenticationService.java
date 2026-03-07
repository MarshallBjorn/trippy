package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.domain.Role;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.domain.VerificationToken;
import com.navrotskyi.trippyapi.dto.AuthResponse;
import com.navrotskyi.trippyapi.dto.LoginRequest;
import com.navrotskyi.trippyapi.dto.RegisterRequest;
import com.navrotskyi.trippyapi.repository.UserRepository;
import com.navrotskyi.trippyapi.repository.VerificationTokenRepository;
import com.navrotskyi.trippyapi.security.JwtService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;

    public AuthenticationService(UserRepository userRepository, 
                                 PasswordEncoder passwordEncoder, 
                                 JwtService jwtService, 
                                 AuthenticationManager authenticationManager,
                                 EmailService emailService,
                                 VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    public AuthResponse register(RegisterRequest request) {
        var user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.USER
        );
        user.setVerified(false);
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken vToken = new VerificationToken(token, user, LocalDateTime.now().plusHours(24));

        verificationTokenRepository.save(vToken);

        emailService.sendVerificationEmail(user.getEmail(), token);

        var jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    public AuthResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        if (!user.isVerified()) {
            throw new RuntimeException("Konto nie zostało zweryfikowane! Sprawdź swoją skrzynkę email.");
        }

        var jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }
}
