package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.response.SettlementResponse;
import com.multivendor.ecommerce.dto.response.VendorOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SettlementService {

    /** Sub-orders that would be included if a settlement were generated for this vendor right now. */
    List<VendorOrderResponse> getEligiblePreview(Long vendorId);

    /** Bundles every eligible (delivered + paid + not-yet-settled) sub-order into a new PENDING settlement. */
    SettlementResponse generateSettlement(Long vendorId);

    SettlementResponse markPaid(Long settlementId, String paymentReference, String notes);

    Page<SettlementResponse> getVendorSettlements(Long vendorId, Pageable pageable);

    Page<SettlementResponse> getAllSettlements(Pageable pageable);
}
