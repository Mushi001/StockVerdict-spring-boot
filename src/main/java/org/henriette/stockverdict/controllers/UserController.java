package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.models.Otp;
import org.henriette.stockverdict.models.Users;
import org.henriette.stockverdict.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for User authentication and management.
 * Handles registration, login, OTP verification, profile updates, and deletion.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users & Authentication", description = "Endpoints for registering, logging in, OTP verification, and user management")
public class git UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ==================== Authentication ====================

    /**
     * POST /api/users/register
     * Registers a new user with TRADER role and PENDING status.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new TRADER account that awaits admin approval.")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String password = request.get("password");

        if (name == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Name, email, and password are required"
            ));
        }

        if (userService.isEmailExists(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false,
                    "message", "Email already exists"
            ));
        }

        Users user = new Users(name, email, password, "TRADER");
        user.setStatus("PENDING");

        if (userService.registerUser(user)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registration successful. Awaiting admin approval.");
            response.put("userId", user.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Registration failed"
        ));
    }

    /**
     * POST /api/users/login
     * Authenticates user and generates OTP for 2FA.
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates using email/password and generates an OTP sent via email.")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email and password are required"
            ));
        }

        Users user = userService.loginUser(email, password);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Invalid email or password"
            ));
        }

        String status = user.getStatus() != null ? user.getStatus() : "ACTIVE";

        if ("PENDING".equalsIgnoreCase(status)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "Your account is awaiting administrative approval."
            ));
        }

        if ("INACTIVE".equalsIgnoreCase(status)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "Your account has been deactivated. Please contact support."
            ));
        }

        // Generate and send OTP
        String otpCode = String.valueOf((int) (Math.random() * 900000) + 100000);
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        if (!userService.saveOtp(user, otpCode, expiry)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to generate verification code"
            ));
        }

        boolean emailSent = userService.sendOtpEmail(user.getEmail(), otpCode);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", emailSent ? "OTP sent to your email" : "OTP generated (email delivery failed)");
        response.put("userId", user.getId());
        response.put("emailSent", emailSent);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/users/verify-otp
     * Verifies OTP and completes authentication.
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Validates the 6-digit OTP code sent to the user's email.")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> request) {
        String userIdStr = request.get("userId");
        String enteredOtp = request.get("otp");

        if (userIdStr == null || enteredOtp == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User ID and OTP are required"
            ));
        }

        Long userId = Long.parseLong(userIdStr);
        enteredOtp = enteredOtp.trim();

        Users user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "User not found"
            ));
        }

        Otp otp = userService.getLatestOtp(user);

        if (otp == null || otp.isUsed()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Invalid OTP"
            ));
        }

        if (LocalDateTime.now().isAfter(otp.getExpiryTime())) {
            return ResponseEntity.status(HttpStatus.GONE).body(Map.of(
                    "success", false,
                    "message", "OTP expired. Please login again."
            ));
        }

        if (!otp.getOtpCode().equals(enteredOtp)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Incorrect OTP"
            ));
        }

        userService.markOtpAsUsed(otp);

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());
        userData.put("status", user.getStatus());
        userData.put("businessName", user.getBusinessName());
        userData.put("profileImageUrl", user.getProfileImageUrl());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Authentication successful");
        response.put("user", userData);
        return ResponseEntity.ok(response);
    }

    // ==================== Profile Management ====================

    /**
     * GET /api/users/{id}
     * Retrieves a user by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        Users user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "User not found"
            ));
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());
        userData.put("status", user.getStatus());
        userData.put("businessName", user.getBusinessName());
        userData.put("momoCode", user.getMomoCode());
        userData.put("bankAccountNumber", user.getBankAccountNumber());
        userData.put("profileImageUrl", user.getProfileImageUrl());
        userData.put("createdAt", user.getCreatedAt());
        userData.put("updatedAt", user.getUpdatedAt());

        return ResponseEntity.ok(Map.of("success", true, "user", userData));
    }

    /**
     * PUT /api/users/{id}
     * Updates a user's name, role, and password.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id,
                                                          @RequestBody Map<String, String> request) {
        Users updatedUser = new Users();
        updatedUser.setId(id);
        updatedUser.setName(request.get("name"));
        updatedUser.setRole(request.get("role"));
        updatedUser.setPassword(request.get("password"));

        if (userService.updateUser(updatedUser)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "User updated successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false, "message", "User not found or update failed"
        ));
    }

    /**
     * PUT /api/users/{id}/profile
     * Updates profile details (name, business info, profile image) without touching the password.
     */
    @PutMapping("/{id}/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@PathVariable Long id,
                                                             @RequestBody Map<String, String> request) {
        boolean success = userService.updateProfileDetails(
                id,
                request.get("name"),
                request.get("businessName"),
                request.get("momoCode"),
                request.get("bankAccountNumber"),
                request.get("profileImageUrl")
        );

        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false, "message", "User not found or update failed"
        ));
    }

    /**
     * PUT /api/users/{id}/password
     * Changes the user's password after verifying the current one.
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<Map<String, Object>> changePassword(@PathVariable Long id,
                                                              @RequestBody Map<String, String> request) {
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");

        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "message", "Passwords do not match"
            ));
        }

        if (userService.changePassword(id, currentPassword, newPassword)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Password changed successfully"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false, "message", "Invalid current password or user not found"
        ));
    }

    /**
     * DELETE /api/users/{id}
     * Deletes a user by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "User deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false, "message", "User not found"
        ));
    }

    /**
     * GET /api/users/role/{role}
     * Retrieves all users with a specific role.
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<Map<String, Object>> getUsersByRole(@PathVariable String role) {
        List<Users> users = userService.getAllUsersByRole(role);
        return ResponseEntity.ok(Map.of("success", true, "count", users.size(), "users", users));
    }

    /**
     * GET /api/users/{id}/payment-info
     * Retrieves safe payment info for a trader (public endpoint).
     */
    @GetMapping("/{id}/payment-info")
    public ResponseEntity<Map<String, Object>> getPaymentInfo(@PathVariable Long id) {
        Users trader = userService.getTraderPaymentInfo(id);
        if (trader == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false, "message", "Trader not found"
            ));
        }

        Map<String, Object> paymentInfo = new HashMap<>();
        paymentInfo.put("name", trader.getName());
        paymentInfo.put("email", trader.getEmail());
        paymentInfo.put("businessName", trader.getBusinessName());
        paymentInfo.put("momoCode", trader.getMomoCode());
        paymentInfo.put("bankAccountNumber", trader.getBankAccountNumber());
        paymentInfo.put("profileImageUrl", trader.getProfileImageUrl());

        return ResponseEntity.ok(Map.of("success", true, "paymentInfo", paymentInfo));
    }
}
