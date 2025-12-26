package com.api.group9.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "otp_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpCode {

    public enum OtpPurpose {
        REGISTER,
        FORGOT_PASSWORD,
        CHANGE_PASSWORD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "otp_code", nullable = false, length = 6)
    private String code;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "expiry_time", nullable = false)
    private Instant expiryTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 20)
    private OtpPurpose purpose;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        // Nếu muốn tự động set updatedAt luôn thì thêm:
        // this.updatedAt = Instant.now();
    }
}