package com.navrotskyi.trippyapi.service;

import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.dto.UpdateUserRequest;
import com.navrotskyi.trippyapi.dto.UserResponse;
import com.navrotskyi.trippyapi.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    @Value("${app.file-server.base-url:http://localhost:8888/uploads/}")
    private String fileServerBaseUrl;

    public UserResponse uploadProfilePhoto(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        String savedFilename = fileStorageService.savePhoto(file);

        user.setPhotoUrl(savedFilename);
        User savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToResponse(user);
    }

    public UserResponse updateCurrentUser(String email, UpdateUserRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            boolean emailTaken = userRepository.findByEmail(request.getEmail())
                    .filter(existing -> !existing.getId().equals(user.getId()))
                    .isPresent();
            if (emailTaken) {
                throw new IllegalArgumentException("Email is already in use");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    private UserResponse mapToResponse(User user) {
        String fullPhotoUrl = (user.getPhotoUrl() != null) ? fileServerBaseUrl + user.getPhotoUrl() : null;

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isVerified(),
                fullPhotoUrl
        );
    }
}