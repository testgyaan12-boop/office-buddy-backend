package com.officebuddy.payment;

import com.officebuddy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "payment_config")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfig extends BaseEntity {

    public static final String RAZORPAY = "razorpay";
    public static final String PHONEPE = "phonepe";
    public static final String STRIPE = "stripe";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider", nullable = false, unique = true)
    private String provider;

    @Column(name = "key_id")
    private String keyId;

    @Column(name = "secret_key")
    private String secretKey;

    public boolean isUsable() {
        return Integer.valueOf(1).equals(getIsActive())
                && !Integer.valueOf(1).equals(getIsDeleted())
                && keyId != null && !keyId.isBlank();
    }
}
