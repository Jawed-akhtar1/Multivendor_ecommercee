package com.multivendor.ecommerce.entity;

import com.multivendor.ecommerce.entity.enums.PaymentMethod;
import com.multivendor.ecommerce.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Simulated payment record. No real money moves — {@link #transactionId} is a
 * locally-generated mock reference, standing in for what a gateway (Razorpay,
 * etc.) would normally return. Kept as its own entity (rather than fields on
 * Order) so a customer has a proper payment history and so refunds have
 * somewhere to be recorded distinctly from the order's own status.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    // Mock gateway reference, e.g. "PAY-8F3C1A2B"
    @Column(unique = true)
    private String transactionId;

    // Non-sensitive display hint only — e.g. "UPI: name@bank" or "Card ending 4242".
    // Full card/UPI details are never persisted.
    private String methodDetail;

    private String failureReason;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime paidAt;

    private LocalDateTime refundedAt;
}
