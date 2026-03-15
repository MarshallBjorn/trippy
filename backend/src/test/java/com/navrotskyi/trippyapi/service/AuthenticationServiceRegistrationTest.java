package com.navrotskyi.trippyapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.navrotskyi.trippyapi.domain.Role;
import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.domain.VerificationToken;
import com.navrotskyi.trippyapi.dto.AuthResponse;
import com.navrotskyi.trippyapi.dto.RegisterRequest;
import com.navrotskyi.trippyapi.repository.UserRepository;
import com.navrotskyi.trippyapi.repository.VerificationTokenRepository;
import com.navrotskyi.trippyapi.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceRegistrationTest {
    
    @Mock private UserRepository userRepository;
    @Mock private VerificationTokenRepository tokenRepository;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Captor
    private ArgumentCaptor<User> userCaptor;
    
    @Captor
    private ArgumentCaptor<VerificationToken> tokenCaptor;

    private RegisterRequest request;

    @BeforeEach
    void SetUp() {
        request = new RegisterRequest("John Doe", "john.doe@email.com", "redneck123!");
    }

    @Test
    void shouldSuccessfullyRegisterUserAndReturnJwt() {
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("ZaszyfrowaneHasloHash");
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-jwt-token");

        AuthResponse response = authenticationService.register(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());

        verify(emailService, times(1)).sendVerificationEmail(eq("john.doe@email.com"), anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(tokenRepository, times(1)).save(any(VerificationToken.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsAlreadyTaken() {
        User existingUser = new User();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.register(request);
        });

        assertEquals("Użytkownik z tym adresem email już istnieje!", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(tokenRepository, never()).save(any(VerificationToken.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void shouldEnforceSecurityConstraintsOnNewUser() {
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("Szyfr123");

        authenticationService.register(request);

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("John Doe", savedUser.getName());
        assertEquals("john.doe@email.com", savedUser.getEmail());
        assertEquals("Szyfr123", savedUser.getPassword(), "Hasło musi być zaszyfrowane!");
        assertNotEquals("redneck123!", savedUser.getPassword(), "Hasło w bazie nie może być jawnym tekstem!");
        assertEquals(Role.USER, savedUser.getRole(), "Nowy użytkownik musi mieć rolę USER!");
        assertFalse(savedUser.isVerified(), "Konto musi być zablokowane (niezweryfikowane)!");
    }

    @Test
    void shouldGenerateValidVerificationToken() {
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        authenticationService.register(request);

        verify(tokenRepository).save(tokenCaptor.capture());
        VerificationToken savedToken = tokenCaptor.getValue();

        assertNotNull(savedToken.getToken(), "Token nie może być nullem.");
        assertFalse(savedToken.getToken().isEmpty(), "Token nie może być pusty.");
        assertNotNull(savedToken.getExpiryDate(), "Data wygaśnięcia musi być ustawiona");
        assertNotNull(savedToken.getUser(), "Token musi być powiązany z użytkownikiem");
    }
}
