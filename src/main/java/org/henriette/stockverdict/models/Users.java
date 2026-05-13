package org.henriette.stockverdict.models;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * Represents a system user (Trader or Admin).
 * Contains authentication details, profile information, and role-based access control.
 */
@Entity
@Table(name = "users")
public class Users implements Serializable {
    private static final long serialVersionUID = 1L;
        /**
         * The unique identifier for the user.
         */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /**
         * The full name of the user.
         */
        @Column(nullable = false)
        private String name;

        /**
         * The user's email address, which is unique and often used for logging in.
         */
        @Column(nullable = false, unique = true)
        private String email;

        /**
         * The hashed password used for authentication.
         */
        @Column(nullable = false)
        private String password;

        /**
         * The access level or role of the user (e.g., "ADMIN", "TRADER").
         */
        @Column(nullable = false)
        private String role;

        /**
         * The timestamp indicating when the user account was created.
         */
        @Column(name = "created_at")
        private LocalDateTime createdAt;

        /**
         * The timestamp indicating the last time the user account was updated.
         */
        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        @Column(name = "business_name")
        private String businessName;

        @Column(name = "momo_code")
        private String momoCode;

        @Column(name = "bank_account_number")
        private String bankAccountNumber;

        @Column(name = "profile_image_url")
        private String profileImageUrl;

        @Column(nullable = true)
        private String status = "PENDING"; // State: PENDING, ACTIVE, INACTIVE

        // Constructors
        /**
         * Default constructor.
         * Initializes the creation and update timestamps to the current date and time.
         */
        public Users() {
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            this.status = "PENDING";
        }

        /**
         * Constructs a new User with the specified details.
         *
         * @param name     the full name of the user
         * @param email    the user's email address
         * @param password the hashed user's password
         * @param role     the designated role for the user
         */
        public Users(String name, String email, String password, String role) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.role = role;
            this.status = "PENDING";
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public String getBusinessName() { return businessName; }
        public void setBusinessName(String businessName) { this.businessName = businessName; }

        public String getMomoCode() { return momoCode; }
        public void setMomoCode(String momoCode) { this.momoCode = momoCode; }

        public String getBankAccountNumber() { return bankAccountNumber; }
        public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }

        public String getProfileImageUrl() { return profileImageUrl; }
        public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    }


