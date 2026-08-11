package com.multivendor.ecommerce.service.impl;

import com.multivendor.ecommerce.service.CommissionService;
import com.multivendor.ecommerce.dto.response.VendorResponse;
import com.multivendor.ecommerce.entity.Vendor;
import com.multivendor.ecommerce.exception.BadRequestException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.VendorRepository;
import com.multivendor.ecommerce.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final VendorRepository vendorRepository;
    private final CommissionService commissionService;

    @Override
    public List<VendorResponse> getPendingVendors() {
        return vendorRepository.findByApprovedFalse().stream().map(this::toResponse).toList();
    }

    @Override
    public List<VendorResponse> getApprovedVendors() {
        return vendorRepository.findByApprovedTrue().stream().map(this::toResponse).toList();
    }

    @Override
    public VendorResponse approveVendor(Long vendorId) {
        Vendor vendor = getVendorOrThrow(vendorId);
        if (vendor.isApproved()) {
            throw new BadRequestException("Vendor is already approved");
        }
        vendor.setApproved(true);
        vendor = vendorRepository.save(vendor);
        return toResponse(vendor);
    }

    @Override
    public VendorResponse rejectVendor(Long vendorId) {
        Vendor vendor = getVendorOrThrow(vendorId);
        if (vendor.isApproved()) {
            throw new BadRequestException("An already-approved vendor cannot be rejected; disable them instead");
        }
        VendorResponse response = toResponse(vendor);
        vendorRepository.delete(vendor);
        return response;
    }

    @Override
    public VendorResponse setCommissionRate(Long vendorId, BigDecimal commissionRate) {
        Vendor vendor = getVendorOrThrow(vendorId);
        vendor.setCommissionRate(commissionRate);
        vendor = vendorRepository.save(vendor);
        return toResponse(vendor);
    }

    private Vendor getVendorOrThrow(Long vendorId) {
        return vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
    }

    private VendorResponse toResponse(Vendor vendor) {
        CommissionService.CommissionBreakdown breakdown = commissionService.getBreakdown(vendor);
        return VendorResponse.builder()
                .id(vendor.getId())
                .userId(vendor.getUser().getId())
                .storeName(vendor.getStoreName())
                .storeDescription(vendor.getStoreDescription())
                .gstNumber(vendor.getGstNumber())
                .logoUrl(vendor.getLogoUrl())
                .approved(vendor.isApproved())
                .ownerName(vendor.getUser().getName())
                .ownerEmail(vendor.getUser().getEmail())
                .baseCommissionRate(breakdown.baseRate())
                .baseCommissionRateIsCustom(breakdown.baseRateIsCustom())
                .averageRating(breakdown.averageRating())
                .reviewCount(breakdown.reviewCount())
                .ratingMultiplier(breakdown.ratingMultiplier())
                .effectiveCommissionRate(breakdown.effectiveRate())
                .bankAccountName(vendor.getBankAccountName())
                .bankAccountNumber(vendor.getBankAccountNumber())
                .bankIfsc(vendor.getBankIfsc())
                .bankName(vendor.getBankName())
                .build();
    }
}
