package org.henriette.stockverdict.models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a One-Time Password (OTP) generated for user verification.
 * This entity tracks the OTP code, its expiration, and whether it has been used.
 */
@Entity
@Table(name = "user_otps")
public class Otp implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier for the OTP record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user associated with this OTP.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    /**
     * The generated OTP string.
     */
    @Column(name = "otp_code", nullable = false)
    private String otpCode;

    /**
     * The timestamp indicating when the OTP expires.
     */
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    /**
     * Indicates whether the OTP has been used.
     */
    @Column(nullable = false)
    private boolean used = false;

    /**
     * Default constructor.
     */
    public Otp() {}

    /**
     * Constructs a new OTP with the specified user, code, and expiry time.
     * The OTP is initially marked as unused.
     *
     * @param user       the user requesting the OTP
     * @param otpCode    the generated OTP code
     * @param expiryTime the time at which the OTP expires
     */
    public Otp(Users user, String otpCode, LocalDateTime expiryTime) {
        this.user = user;
        this.otpCode = otpCode;
        this.expiryTime = expiryTime;
        this.used = false;
    }

    /**
     * Constructs a full OTP record with all fields specified.
     *
     * @param used       whether the OTP is used
     * @param expiryTime the expiration time of the OTP
     * @param otpCode    the OTP code
     * @param user       the user associated with the OTP
     * @param id         the unique identifier of the OTP record
     */
    public Otp(boolean used, LocalDateTime expiryTime, String otpCode, Users user, Long id) {
        this.used = used;
        this.expiryTime = expiryTime;
        this.otpCode = otpCode;
        this.user = user;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Users getUser() {
        return user;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public boolean isUsed() {
        return used;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}