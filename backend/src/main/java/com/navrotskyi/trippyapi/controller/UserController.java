package com.navrotskyi.trippyapi.controller;

import com.navrotskyi.trippyapi.dto.UpdateUserRequest;
import com.navrotskyi.trippyapi.dto.UserResponse;
import com.navrotskyi.trippyapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints for managing user accounts")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user", description = "Retrieves the details of the currently authenticated user context.")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserResponse response = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user", description = "Updates details (name, email, password) for the currently authenticated user.")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateUserRequest request
    ) {
        UserResponse response = userService.updateCurrentUser(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
}
 
/*
**How it works end to end:**
```
PUT /api/users/me
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "name": "New Name",        // optional
  "email": "new@email.com",  // optional — checked for uniqueness
  "password": "newSecret123" // optional — re-hashed before saving
}

→ 200 OK  { id, name, email, role, isVerified }
→ 400     if email is already taken by another account
→ 403     if no/invalid JWT 
*/