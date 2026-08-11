package com.multivendor.ecommerce.service.impl;

import com.multivendor.ecommerce.config.CommissionProperties;
import com.multivendor.ecommerce.entity.Vendor;
import com.multivendor.ecommerce.repository.ProductReviewRepository;
import com.multivendor.ecommerce.service.CommissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

    private final CommissionProperties properties;
    private final ProductReviewRepository productReviewRepository;

    @Override
    public BigDecimal calculateEffectiveRate(Vendor vendor) {
        return getBreakdown(vendor).effectiveRate();
    }

    @Override
    public CommissionBreakdown getBreakdown(Vendor vendor) {
        boolean baseRateIsCustom = vendor.getCommissionRate() != null;
        BigDecimal baseRate = baseRateIsCustom ? vendor.getCommissionRate() : properties.getDefaultRatePercent();

        Double avgRating = productReviewRepository.findAverageRatingForVendor(vendor.getId());
        Long reviewCount = productReviewRepository.countForVendor(vendor.getId());
        if (reviewCount == null) reviewCount = 0L;

        BigDecimal multiplier = resolveMultiplier(avgRating, reviewCount);

        BigDecimal effectiveRate = baseRate.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        // Clamp to a sane [0, 100] range regardless of how tiers are configured.
        if (effectiveRate.compareTo(BigDecimal.ZERO) < 0) effectiveRate = BigDecimal.ZERO;
        if (effectiveRate.compareTo(BigDecimal.valueOf(100)) > 0) effectiveRate = BigDecimal.valueOf(100);

        return new CommissionBreakdown(baseRate, baseRateIsCustom, avgRating, reviewCount, multiplier, effectiveRate);
    }

    private BigDecimal resolveMultiplier(Double avgRating, long reviewCount) {
        if (avgRating == null || reviewCount < properties.getMinimumReviewsForAdjustment()) {
            return properties.getNoRatingMultiplier();
        }
        BigDecimal rating = BigDecimal.valueOf(avgRating);

        if (rating.compareTo(properties.getExcellentRatingThreshold()) >= 0) {
            return properties.getExcellentMultiplier();
        }
        if (rating.compareTo(properties.getGoodRatingThreshold()) >= 0) {
            return properties.getStandardMultiplier();
        }
        if (rating.compareTo(properties.getBelowAverageRatingThreshold()) >= 0) {
            return properties.getBelowAverageMultiplier();
        }
        return properties.getPoorMultiplier();
    }
}
