package org.henriette.stockverdict.repositories;

import org.henriette.stockverdict.models.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    /**
     * Finds the most recent OTP for a given user, ordered by ID descending.
     */
    Optional<Otp> findFirstByUserIdOrderByIdDesc(Long userId);

    /**
     * Deletes all unused OTPs for a given user.
     */
    void deleteByUserIdAndUsedFalse(Long userId);
}
