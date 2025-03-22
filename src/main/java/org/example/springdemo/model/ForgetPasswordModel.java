package org.example.springdemo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forget_password")
public class ForgetPasswordModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fp_id")
    private Integer fpId;

    @Column(name = "fp_user_id", nullable = false)
    private Integer fpUserId;

    @Column(name = "fp_verification_code", nullable = false, length = 6)
    private String fp_verification_code;

    @Column(name = "fp_expiry", nullable = false)
    private LocalDateTime fpExpiry;

    @Column(name = "fp_is_used")
    private boolean fp_is_used = false;

    // Constructors
    public ForgetPasswordModel() {}

    // Getters and Setters
    public Integer getFpId() { return fpId; }
    public void setFpId(Integer fpId) { this.fpId = fpId; }

    public Integer getFp_user_id() { return fpUserId; }
    public void setFp_user_id(Integer fpUserId) { this.fpUserId = fpUserId; }

    public String getFp_verification_code() { return fp_verification_code; }
    public void setFp_verification_code(String fp_verification_code) { this.fp_verification_code = fp_verification_code; }

    public LocalDateTime getFp_expiry() { return fpExpiry; }
    public void setFp_expiry(LocalDateTime fp_expiry) { this.fpExpiry = fp_expiry; }

    public boolean isFp_is_used() { return fp_is_used; }
    public void setFp_is_used(boolean fp_is_used) { this.fp_is_used = fp_is_used; }
}