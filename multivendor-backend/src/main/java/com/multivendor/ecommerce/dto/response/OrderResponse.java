package com.multivendor.ecommerce.dto.response;

import com.multivendor.ecommerce.entity.enums.OrderStatus;
import com.multivendor.ecommerce.entity.enums.PaymentMethod;
import com.multivendor.ecommerce.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Customer-facing order view. Fans out into one SubOrderResponse per vendor
 * whose products were in the cart — deliberately excludes commission/payout
 * figures (those are vendor/admin-only, see VendorOrderResponse).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private BigDecimal totalAmount;
    private OrderStatus status; // aggregate summary — see Order.status javadoc
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    private List<SubOrderResponse> subOrders;

    // Shipping address snapshot, surfaced so the frontend doesn't need a second call
    private String shippingFullName;
    private String shippingPhone;
    private String shippingAddressLine;
    private String shippingCity;
    private String shippingState;
    private String shippingPincode;

    // Nullable: populated once a Payment record exists for this order
    private PaymentResponse payment;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubOrderResponse {
        private Long id;
        private String subOrderNumber;
        private Long vendorId;
        private String vendorStoreName;
        private OrderStatus status;
        private BigDecimal subtotal;
        private List<OrderItemResponse> items;
        private ShipmentResponse shipment; // nullable — not booked yet
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal priceAtPurchase;
    }
}
