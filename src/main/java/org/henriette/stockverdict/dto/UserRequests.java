package org.henriette.stockverdict.dto;

public class UserRequests {

    public record RegisterRequest(String name, String email, String password) {}

    public record LoginRequest(String email, String password) {}

    public record VerifyOtpRequest(Long userId, String otp) {}

    public record UpdateUserRequest(String name, String role, String password) {}

    public record UpdateProfileRequest(String name, String businessName, String momoCode, 
                                       String bankAccountNumber, String profileImageUrl) {}

    public record ChangePasswordRequest(String currentPassword, String newPassword, String confirmPassword) {}

}
