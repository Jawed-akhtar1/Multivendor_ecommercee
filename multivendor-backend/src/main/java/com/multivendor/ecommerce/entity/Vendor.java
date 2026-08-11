package com.multivendor.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String storeName;

    @Column(length = 1000)
    private String storeDescription;

    private String gstNumber;

    private String logoUrl;

    // Vendor Approval workflow (Admin Module)
    @Builder.Default
    private boolean approved = false;

    // Commission — null means "use the platform default" (app.commission.default-rate-percent).
    // A percentage, e.g. 10.00 means the platform retains 10% of each sale from this vendor.
    private java.math.BigDecimal commissionRate;

    // Payout bank details — needed once settlements start transferring real money.
    // All nullable: a vendor can be approved and sell before filling these in,
    // but settlement generation should warn/block if they're missing.
    private String bankAccountName;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankName;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
