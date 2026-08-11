package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.entity.Vendor;

import java.math.BigDecimal;

public interface CommissionService {

    /**
     * The commission rate that would apply to a vendor's next sale right now:
     * their own override (or the platform default) adjusted by their current
     * average product rating. This is what gets snapshotted onto a VendorOrder
     * at order-placement time.
     */
    BigDecimal calculateEffectiveRate(Vendor vendor);

    /** Exposes the pieces behind calculateEffectiveRate for display/transparency (e.g. vendor dashboard). */
    CommissionBreakdown getBreakdown(Vendor vendor);

    record CommissionBreakdown(
            BigDecimal baseRate,
            boolean baseRateIsCustom,
            Double averageRating,
            Long reviewCount,
            BigDecimal ratingMultiplier,
            BigDecimal effectiveRate
    ) {}
}
