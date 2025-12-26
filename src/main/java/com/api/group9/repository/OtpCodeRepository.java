package com.api.group9.repository;

import com.api.group9.model.OtpCode;
import com.api.group9.model.User;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findTopByUserAndCodeAndPurposeAndIsUsedFalseAndExpiryTimeAfterOrderByCreatedAtDesc(
        User user, 
        String code, 
        OtpCode.OtpPurpose purpose,
        Instant expiryTime
    );

    void deleteAllByUser(User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM OtpCode o WHERE o.expiryTime < :thresholdTime OR o.createdAt < :cleanupTime")
    void cleanupOldOtps(Instant thresholdTime, Instant cleanupTime);
}