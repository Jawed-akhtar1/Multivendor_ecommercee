package com.multivendor.ecommerce.dto.response;

import com.multivendor.ecommerce.entity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The vendor/admin view of a sub-order — includes commission and payout
 * figures, which are deliberately left out of the customer-facing
 * OrderResponse.SubOrderResponse.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorOrderResponse {
    private Long id;
    private String subOrderNumber;
    private String parentOrderNumber;
    private Long vendorId;
    private String vendorStoreName;
    private OrderStatus status;

    private BigDecimal subtotal;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private BigDecimal payoutAmount;

    private boolean settled;
    private Long settlementId;

    private List<OrderResponse.OrderItemResponse> items;
    private ShipmentResponse shipment;

    private LocalDateTime createdAt;
}
