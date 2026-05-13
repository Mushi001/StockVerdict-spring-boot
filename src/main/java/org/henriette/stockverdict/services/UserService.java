package org.henriette.stockverdict.services;

import org.henriette.stockverdict.models.Otp;
import org.henriette.stockverdict.models.Users;
import org.henriette.stockverdict.repositories.OtpRepository;
import org.henriette.stockverdict.repositories.UserRepository;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Users and their authentication via OTP.
 * Handles user registration, login, email OTPs, and password hashing.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.name}")
    private String appName;

    @Autowired
    public UserService(UserRepository userRepository, OtpRepository otpRepository, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.mailSender = mailSender;
    }

    // ==================== Password Utilities ====================

    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    private boolean checkPassword(String plainPassword, String hashedPassword) {
        if (hashedPassword != null && hashedPassword.startsWith("$2b$")) {
            hashedPassword = "$2a$" + hashedPassword.substring(4);
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    // ==================== User CRUD ====================

    /**
     * Registers a new user. Hashes the password and sets timestamps.
     */
    @Transactional
    public boolean registerUser(Users user) {
        try {
            user.setPassword(hashPassword(user.getPassword()));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Authenticates a user by email and password.
     */
    public Users loginUser(String email, String password) {
        Optional<Users> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            Users user = optionalUser.get();
            if (checkPassword(password, user.getPassword())) {
                if (user.getStatus() == null) {
                    user.setStatus("PENDING");
                }
                return user;
            }
        }
        return null;
    }

    /**
     * Finds a user by their ID.
     */
    public Users findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Checks if an email is already registered.
     */
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Updates an existing user's name, role, and password.
     */
    @Transactional
    public boolean updateUser(Users updatedUser) {
        try {
            Optional<Users> optionalUser = userRepository.findById(updatedUser.getId());
            if (optionalUser.isEmpty()) return false;

            Users existingUser = optionalUser.get();
            existingUser.setName(updatedUser.getName());
            existingUser.setRole(updatedUser.getRole());
            existingUser.setPassword(hashPassword(updatedUser.getPassword()));
            existingUser.setUpdatedAt(LocalDateTime.now());

            userRepository.save(existingUser);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates profile details without touching the password.
     */
    @Transactional
    public boolean updateProfileDetails(Long userId, String name, String businessName,
                                        String momoCode, String bankAccountNumber, String profileImageUrl) {
        try {
            Optional<Users> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) return false;

            Users existingUser = optionalUser.get();
            existingUser.setName(name);
            existingUser.setBusinessName(businessName);
            existingUser.setMomoCode(momoCode);
            existingUser.setBankAccountNumber(bankAccountNumber);
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                existingUser.setProfileImageUrl(profileImageUrl);
            }
            existingUser.setUpdatedAt(LocalDateTime.now());

            userRepository.save(existingUser);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves safe payment info for a specific trader.
     */
    public Users getTraderPaymentInfo(Long id) {
        Optional<Users> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            Users user = optionalUser.get();
            if ("TRADER".equalsIgnoreCase(user.getRole())) {
                Users safeUser = new Users();
                safeUser.setName(user.getName());
                safeUser.setEmail(user.getEmail());
                safeUser.setBusinessName(user.getBusinessName());
                safeUser.setMomoCode(user.getMomoCode());
                safeUser.setBankAccountNumber(user.getBankAccountNumber());
                safeUser.setProfileImageUrl(user.getProfileImageUrl());
                return safeUser;
            }
        }
        return null;
    }

    /**
     * Changes a user's password after verifying the current password.
     */
    @Transactional
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        try {
            Optional<Users> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) return false;

            Users user = optionalUser.get();
            if (!checkPassword(currentPassword, user.getPassword())) {
                return false;
            }

            user.setPassword(hashPassword(newPassword));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a user by their ID.
     */
    @Transactional
    public boolean deleteUser(Long userId) {
        try {
            if (!userRepository.existsById(userId)) return false;
            userRepository.deleteById(userId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all users with a specific role.
     */
    public List<Users> getAllUsersByRole(String role) {
        return userRepository.findAll().stream()
                .filter(u -> role.equalsIgnoreCase(u.getRole()))
                .sorted((a, b) -> {
                    if (b.getCreatedAt() == null) return -1;
                    if (a.getCreatedAt() == null) return 1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .toList();
    }

    /**
     * Counts users by their account status.
     */
    public Long countUsersByStatus(String status) {
        return userRepository.findAll().stream()
                .filter(u -> status.equalsIgnoreCase(u.getStatus()))
                .count();
    }

    /**
     * Updates the status of a specific user.
     */
    @Transactional
    public boolean updateUserStatus(Long userId, String status) {
        try {
            Optional<Users> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) return false;

            Users user = optionalUser.get();
            user.setStatus(status);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== OTP Management ====================

    /**
     * Saves a new OTP for the given user, invalidating old unused ones first.
     */
    @Transactional
    public boolean saveOtp(Users user, String otpCode, LocalDateTime expiryTime) {
        try {
            otpRepository.deleteByUserIdAndUsedFalse(user.getId());

            Users managedUser = userRepository.findById(user.getId()).orElse(null);
            if (managedUser == null) return false;

            Otp otp = new Otp(managedUser, otpCode, expiryTime);
            otpRepository.save(otp);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the most recently created OTP for a user.
     */
    public Otp getLatestOtp(Users user) {
        return otpRepository.findFirstByUserIdOrderByIdDesc(user.getId()).orElse(null);
    }

    /**
     * Marks an OTP as used.
     */
    @Transactional
    public void markOtpAsUsed(Otp otp) {
        otp.setUsed(true);
        otpRepository.save(otp);
    }

    /**
     * Sends the OTP to the user's email.
     */
    public boolean sendOtpEmail(String recipientEmail, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("Your Login OTP - " + appName);

            String htmlContent = "<div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; padding: 30px; background-color: #f4f7f6; border-radius: 8px;\">"
                    + "<div style=\"text-align: center; margin-bottom: 30px;\">"
                    + "<h1 style=\"color: #2c3e50; margin: 0; font-size: 28px; letter-spacing: 1px;\">" + appName + "</h1>"
                    + "<div style=\"height: 3px; background-color: #27ae60; width: 50px; margin: 10px auto;\"></div>"
                    + "</div>"
                    + "<div style=\"background-color: #ffffff; padding: 40px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);\">"
                    + "<h2 style=\"color: #333333; margin-top: 0; font-size: 22px;\">Authentication Required</h2>"
                    + "<p style=\"color: #666666; font-size: 16px; line-height: 1.6; margin-bottom: 25px;\">You are attempting to sign in to your " + appName + " account. Please use the verification code below to complete the secure login process:</p>"
                    + "<div style=\"text-align: center; margin: 35px 0;\">"
                    + "<span style=\"display: inline-block; font-size: 36px; font-weight: 700; color: #2ecc71; background-color: #eaeded; padding: 20px 40px; border-radius: 8px; letter-spacing: 6px; border: 1px solid #d5f5e3;\">" + otpCode + "</span>"
                    + "</div>"
                    + "<p style=\"color: #666666; font-size: 15px; line-height: 1.6;\">This code is valid for <strong>5 minutes</strong>. For your security, please do not share this code with anyone.</p>"
                    + "<div style=\"margin-top: 40px; padding-top: 20px; border-top: 1px solid #eeeeee;\">"
                    + "<p style=\"color: #999999; font-size: 13px; line-height: 1.5; margin: 0;\">If you did not initiate this request, please ignore it or contact our support team immediately.</p>"
                    + "</div>"
                    + "</div>"
                    + "<div style=\"text-align: center; margin-top: 25px; color: #aaaaaa; font-size: 12px;\">"
                    + "&copy; " + java.time.Year.now().getValue() + " " + appName + ". All rights reserved."
                    + "</div>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("OTP email sent successfully to " + recipientEmail);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
