package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.dto.ApiResponse;
import org.henriette.stockverdict.models.Users;
import org.henriette.stockverdict.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.henriette.stockverdict.dto.UserRequests.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for User Management and Authentication.
 * Handles endpoints for user registration, OTP login, profile updates, and authentication token generation.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users & Authentication", description = "Endpoints for registering, logging in, OTP verification, and user management")
public class UserController {

    private final UserService userService;
    private final org.henriette.stockverdict.security.JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, org.henriette.stockverdict.security.JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // ==================== Authentication ====================

    /**
     * Registers a new user account on the platform.
     * 
     * @param request The data transfer object containing registration details (name, email, password, etc.).
     * @return ApiResponse indicating success or failure.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account. Accounts default to PENDING status requiring admin approval.")
    public ResponseEntity<ApiResponse<Void>> registerUser(@RequestBody RegisterRequest request) {
        String name = request.name();
        String email = request.email();
        String password = request.password();

        if (userService.isEmailExists(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, "Email already exists"));
        }

        Users user = new Users(name, email, password, ""); // Business name empty by default

        if (userService.registerUser(user)) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Registration successful. Please wait for admin approval."));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Registration failed"));
    }

    /**
     * Initiates the login process by sending a One-Time Password (OTP) to the user's email.
     * 
     * @param request The data transfer object containing the user's email and password.
     * @return ApiResponse indicating whether the OTP was sent successfully.
     */
    @PostMapping("/login")
    @Operation(summary = "Login to account", description = "Validates credentials and sends a 6-digit OTP to the user's registered email address.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> loginUser(@RequestBody LoginRequest request) {
        String email = request.email();
        String password = request.password();

        Users user = userService.loginUser(email, password);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid email or password"));
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Your account is awaiting administrative approval"));
        }

        // Generate JWT token directly without OTP
        String token = jwtUtil.generateToken(user);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("token", token);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", data));
    }

    /**
     * Verifies the OTP sent to the user's email and generates an authentication token.
     * 
     * @param request The data transfer object containing the email and the 6-digit OTP.
     * @return ApiResponse containing the JWT authentication token and user details.
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP & Generate Token", description = "Validates the OTP and returns a JWT Bearer token for accessing protected endpoints.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyOtp(@RequestBody VerifyOtpRequest request) {
        Long userId = request.userId();
        String otp = request.otp();

        Users user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "User not found"));
        }

        org.henriette.stockverdict.models.Otp latestOtp = userService.getLatestOtp(user);
        if (latestOtp == null || !latestOtp.getOtpCode().equals(otp) || latestOtp.isUsed() || latestOtp.getExpiryTime().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid or expired OTP"));
        }

        userService.markOtpAsUsed(latestOtp);

        // Generate JWT Token
        String token = jwtUtil.generateToken(user);

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());
        userData.put("status", user.getStatus());
        userData.put("businessName", user.getBusinessName());
        userData.put("profileImageUrl", user.getProfileImageUrl());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("user", userData);

        return ResponseEntity.ok(new ApiResponse<>(true, "Authentication successful", responseData));
    }

    // ==================== User Management ====================

    /**
     * Retrieves a specific user's details by their ID.
     * 
     * @param id The unique identifier of the user.
     * @return ApiResponse containing the user's details.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves the details of a specific user account.")
    public ResponseEntity<ApiResponse<Users>> getUserById(@PathVariable Long id) {
        Users user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "User not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, user));
    }

    /**
     * Updates an existing user's profile information.
     * 
     * @param id The unique identifier of the user to update.
     * @param request The data transfer object containing the updated profile details.
     * @return ApiResponse indicating success or failure.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user profile", description = "Updates the name, business name, or profile image of an existing user.")
    public ResponseEntity<ApiResponse<Void>> updateUser(@PathVariable Long id,
                                                        @RequestBody UpdateProfileRequest request) {
        Users existingUser = userService.findById(id);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "User not found"));
        }

        if (userService.updateProfileDetails(id, request.name(), request.businessName(), request.momoCode(), request.bankAccountNumber(), request.profileImageUrl())) {
            return ResponseEntity.ok(new ApiResponse<>(true, "User updated successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to update user"));
    }

    /**
     * Deletes a user account from the system.
     * 
     * @param id The unique identifier of the user to delete.
     * @return ApiResponse indicating success or failure.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Permanently deletes a user account.")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "User not found or deletion failed"));
    }
}
