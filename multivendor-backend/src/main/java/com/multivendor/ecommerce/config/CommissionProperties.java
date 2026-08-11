package com.multivendor.ecommerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "app.commission")
@Getter
@Setter
public class CommissionProperties {

    /** Used for any vendor whose own Vendor.commissionRate is null. */
    private BigDecimal defaultRatePercent = new BigDecimal("10.00");

    // --- Rating-based commission tiers ---
    // A vendor's average product rating (across their whole catalog) adjusts their
    // effective commission rate at order time: well-rated vendors are rewarded with
    // a lower commission (they keep more), poorly-rated vendors pay more. See
    // CommissionServiceImpl for the calculation.

    /** avg rating >= this → reward multiplier applies (vendor pays LESS commission). */
    private BigDecimal excellentRatingThreshold = new BigDecimal("4.5");
    private BigDecimal excellentMultiplier = new BigDecimal("0.85");

    /** avg rating >= this (and below excellent) → standard rate, no adjustment. */
    private BigDecimal goodRatingThreshold = new BigDecimal("4.0");
    private BigDecimal standardMultiplier = new BigDecimal("1.00");

    /** avg rating >= this (and below good) → small surcharge. */
    private BigDecimal belowAverageRatingThreshold = new BigDecimal("3.0");
    private BigDecimal belowAverageMultiplier = new BigDecimal("1.10");

    /** avg rating below belowAverageRatingThreshold → largest surcharge. */
    private BigDecimal poorMultiplier = new BigDecimal("1.25");

    /** Vendors with no reviews yet get this multiplier (benefit of the doubt). */
    private BigDecimal noRatingMultiplier = new BigDecimal("1.00");

    /** Minimum number of reviews before rating affects commission at all — avoids
     *  one bad/good review from swinging a brand-new vendor's rate wildly. */
    private int minimumReviewsForAdjustment = 3;
}
