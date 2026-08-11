package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.response.VendorResponse;

import java.math.BigDecimal;
import java.util.List;

public interface AdminService {
    List<VendorResponse> getPendingVendors();
    List<VendorResponse> getApprovedVendors();
    VendorResponse approveVendor(Long vendorId);
    VendorResponse rejectVendor(Long vendorId);
    VendorResponse setCommissionRate(Long vendorId, BigDecimal commissionRate);
}
