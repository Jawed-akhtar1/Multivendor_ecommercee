package com.multivendor.ecommerce.entity;

import com.multivendor.ecommerce.entity.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A vendor's slice of a multi-vendor checkout. A customer's single Order can
 * fan out into several VendorOrders (one per vendor whose products were in
 * the cart) — each one ships independently, has its own fulfilment status,
 * and its own commission/payout math, while the parent Order stays the
 * single source of truth for payment (the customer pays once, for the whole
 * cart) and the shared delivery address.
 */
@Entity
@Table(name = "vendor_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g. "ORD-20260810-4F2C9A-V1" — human-readable, unique, traceable back to the parent order.
    @Column(nullable = false, unique = true)
    private String subOrderNumber;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Builder.Default
    @OneToMany(mappedBy = "vendorOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PLACED;

    // What the customer paid for just this vendor's items (sum of quantity * priceAtPurchase).
    @Column(nullable = false)
    private BigDecimal subtotal;

    // Snapshot of the rate applied at order time, so later changes to Vendor.commissionRate
    // don't retroactively change historical orders' commission.
    @Column(nullable = false)
    private BigDecimal commissionRate;

    @Column(nullable = false)
    private BigDecimal commissionAmount;

    // subtotal - commissionAmount — what the vendor is owed for this sub-order.
    @Column(nullable = false)
    private BigDecimal payoutAmount;

    // Null until included in a Settlement. Once set, this sub-order is excluded
    // from future settlement-eligible queries regardless of the Settlement's own status.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}
