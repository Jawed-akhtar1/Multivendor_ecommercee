package com.multivendor.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorResponse {
    private Long id;
    private Long userId;
    private String storeName;
    private String storeDescription;
    private String gstNumber;
    private String logoUrl;
    private boolean approved;
    private String ownerName;
    private String ownerEmail;

    // Commission — see CommissionService for how rating adjusts baseRate into effectiveRate.
    private BigDecimal baseCommissionRate;
    private boolean baseCommissionRateIsCustom;
    private Double averageRating;
    private Long reviewCount;
    private BigDecimal ratingMultiplier;
    private BigDecimal effectiveCommissionRate;

    private String bankAccountName;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankName;
}
