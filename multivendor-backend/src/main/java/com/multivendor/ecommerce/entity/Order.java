package com.multivendor.ecommerce.entity;

import com.multivendor.ecommerce.entity.enums.OrderStatus;
import com.multivendor.ecommerce.entity.enums.PaymentMethod;
import com.multivendor.ecommerce.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<VendorOrder> vendorOrders = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal totalAmount;

    // Aggregate/derived status across all vendorOrders — see OrderServiceImpl.recomputeOrderStatus().
    // "Mixed" states (e.g. one vendor shipped, another still placed) show as PLACED/CONFIRMED
    // until every sub-order reaches the same status; the per-vendor truth always lives on
    // VendorOrder.status, this field is just a convenient summary for list views.
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PLACED;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // Shipping snapshot (kept independent of Address entity in case address is later edited/deleted)
    @Column(nullable = false)
    private String shippingFullName;
    @Column(nullable = false)
    private String shippingPhone;
    @Column(nullable = false)
    private String shippingAddressLine;
    @Column(nullable = false)
    private String shippingCity;
    @Column(nullable = false)
    private String shippingState;
    @Column(nullable = false)
    private String shippingPincode;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
