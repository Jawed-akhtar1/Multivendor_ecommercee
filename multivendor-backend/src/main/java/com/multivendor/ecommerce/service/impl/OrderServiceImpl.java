package com.multivendor.ecommerce.service.impl;

import com.multivendor.ecommerce.dto.request.PlaceOrderRequest;
import com.multivendor.ecommerce.dto.response.OrderResponse;
import com.multivendor.ecommerce.dto.response.PaymentResponse;
import com.multivendor.ecommerce.dto.response.ShipmentResponse;
import com.multivendor.ecommerce.dto.response.VendorOrderResponse;
import com.multivendor.ecommerce.entity.*;
import com.multivendor.ecommerce.entity.enums.OrderStatus;
import com.multivendor.ecommerce.entity.enums.PaymentMethod;
import com.multivendor.ecommerce.exception.BadRequestException;
import com.multivendor.ecommerce.exception.ForbiddenException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.*;
import com.multivendor.ecommerce.service.CommissionService;
import com.multivendor.ecommerce.service.OrderService;
import com.multivendor.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Set<OrderStatus> CANCELLABLE = Set.of(OrderStatus.PLACED, OrderStatus.CONFIRMED);
    private static final List<OrderStatus> PROGRESS_ORDER =
            List.of(OrderStatus.PLACED, OrderStatus.CONFIRMED, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final OrderRepository orderRepository;
    private final VendorOrderRepository vendorOrderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final VendorRepository vendorRepository;
    private final ShipmentRepository shipmentRepository;
    private final PaymentService paymentService;
    private final CommissionService commissionService;

    @Override
    @Transactional
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Your cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new ForbiddenException("This address does not belong to you");
        }

        PaymentMethod method = parsePaymentMethod(request.getPaymentMethod());

        // Validate stock for every line before committing anything.
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.isActive()) {
                throw new BadRequestException("\"" + product.getName() + "\" is no longer available");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BadRequestException("Only " + product.getStock() + " unit(s) of \"" + product.getName() + "\" left in stock");
            }
        }

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .totalAmount(BigDecimal.ZERO) // set after sub-orders are built
                .status(OrderStatus.PLACED)
                .paymentMethod(method)
                .shippingFullName(address.getFullName())
                .shippingPhone(address.getPhone())
                .shippingAddressLine(address.getAddressLine())
                .shippingCity(address.getCity())
                .shippingState(address.getState())
                .shippingPincode(address.getPincode())
                .build();
        order = orderRepository.save(order);

        // Group cart items by vendor — each vendor gets its own VendorOrder (sub-order).
        Map<Long, List<CartItem>> byVendor = new LinkedHashMap<>();
        for (CartItem item : cart.getItems()) {
            Long vendorId = item.getProduct().getVendor().getId();
            byVendor.computeIfAbsent(vendorId, k -> new ArrayList<>()).add(item);
        }

        BigDecimal orderTotal = BigDecimal.ZERO;
        int subOrderIndex = 1;
        for (List<CartItem> vendorItems : byVendor.values()) {
            Vendor vendor = vendorItems.get(0).getProduct().getVendor();

            BigDecimal subtotal = BigDecimal.ZERO;
            for (CartItem item : vendorItems) {
                subtotal = subtotal.add(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            BigDecimal commissionRate = commissionService.calculateEffectiveRate(vendor);
            BigDecimal commissionAmount = subtotal.multiply(commissionRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal payoutAmount = subtotal.subtract(commissionAmount);

            VendorOrder vendorOrder = VendorOrder.builder()
                    .subOrderNumber(order.getOrderNumber() + "-V" + subOrderIndex++)
                    .order(order)
                    .vendor(vendor)
                    .status(OrderStatus.PLACED)
                    .subtotal(subtotal)
                    .commissionRate(commissionRate)
                    .commissionAmount(commissionAmount)
                    .payoutAmount(payoutAmount)
                    .build();

            for (CartItem item : vendorItems) {
                Product product = item.getProduct();
                OrderItem orderItem = OrderItem.builder()
                        .vendorOrder(vendorOrder)
                        .product(product)
                        .quantity(item.getQuantity())
                        .priceAtPurchase(product.getPrice())
                        .build();
                vendorOrder.getItems().add(orderItem);

                // Decrement stock (Inventory Management)
                product.setStock(product.getStock() - item.getQuantity());
                productRepository.save(product);
            }

            order.getVendorOrders().add(vendorOrder);
            orderTotal = orderTotal.add(subtotal);
        }

        order.setTotalAmount(orderTotal);
        order = orderRepository.save(order);

        // Create the payment record — PENDING for both COD (collected on delivery) and
        // CCAVENUE (the client must call /api/payments/ccavenue/initiate/{orderId} next).
        paymentService.createPendingPayment(user, order, method);

        // Clear the cart now that the order is placed.
        cart.getItems().clear();
        cartRepository.save(cart);

        return toResponse(order);
    }

    @Override
    public Page<OrderResponse> getMyOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    @Override
    public OrderResponse getMyOrder(Long userId, Long orderId) {
        Order order = getOwnedOrderOrThrow(userId, orderId);
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = getOwnedOrderOrThrow(userId, orderId);

        boolean allCancellable = order.getVendorOrders().stream()
                .allMatch(vo -> CANCELLABLE.contains(vo.getStatus()));
        if (!allCancellable) {
            throw new BadRequestException(
                    "This order can no longer be cancelled — at least one vendor has already started shipping it");
        }

        for (VendorOrder vendorOrder : order.getVendorOrders()) {
            vendorOrder.setStatus(OrderStatus.CANCELLED);
            for (OrderItem item : vendorOrder.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
            vendorOrderRepository.save(vendorOrder);
        }

        Payment refunded = paymentService.refund(order);
        order.setPaymentStatus(refunded.getStatus());
        recomputeOrderStatus(order);

        order = orderRepository.save(order);
        return toResponse(order);
    }

    @Override
    public Page<VendorOrderResponse> getVendorOrders(Long vendorUserId, Pageable pageable) {
        Vendor vendor = vendorRepository.findByUserId(vendorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No vendor store found for this account"));
        return vendorOrderRepository.findByVendorId(vendor.getId(), pageable).map(this::toVendorOrderResponse);
    }

    @Override
    @Transactional
    public VendorOrderResponse updateVendorOrderStatus(Long vendorUserId, Long vendorOrderId, OrderStatus status) {
        Vendor vendor = vendorRepository.findByUserId(vendorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No vendor store found for this account"));

        VendorOrder vendorOrder = vendorOrderRepository.findById(vendorOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-order not found"));

        if (!vendorOrder.getVendor().getId().equals(vendor.getId())) {
            throw new ForbiddenException("This order does not belong to your store");
        }

        vendorOrder.setStatus(status);
        vendorOrder.setUpdatedAt(LocalDateTime.now());
        vendorOrder = vendorOrderRepository.save(vendorOrder);

        Order order = vendorOrder.getOrder();
        recomputeOrderStatus(order);

        boolean allDelivered = order.getVendorOrders().stream().allMatch(vo -> vo.getStatus() == OrderStatus.DELIVERED);
        if (allDelivered && order.getPaymentMethod() == PaymentMethod.COD) {
            Payment collected = paymentService.markCodCollected(order);
            order.setPaymentStatus(collected.getStatus());
        }
        orderRepository.save(order);

        return toVendorOrderResponse(vendorOrder);
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    private Order getOwnedOrderOrThrow(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(userId)) {
            throw new ForbiddenException("This order does not belong to you");
        }
        return order;
    }

    private PaymentMethod parsePaymentMethod(String raw) {
        try {
            return PaymentMethod.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Payment method must be COD or CCAVENUE");
        }
    }

    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "ORD-" + datePart + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    /**
     * Order.status is a convenience summary across all sub-orders: if every
     * sub-order shares one status, that's the order's status. If they've
     * diverged (e.g. one vendor shipped, another still packing), the order
     * shows the least-progressed *active* status so a customer glancing at
     * their order list isn't told "Delivered" when part of it isn't.
     * Cancelled/returned sub-orders are ignored for this unless *everything*
     * is cancelled/returned, in which case that becomes the summary too.
     */
    private void recomputeOrderStatus(Order order) {
        List<VendorOrder> subOrders = order.getVendorOrders();
        if (subOrders.isEmpty()) return;

        Set<OrderStatus> distinct = new HashSet<>();
        for (VendorOrder vo : subOrders) distinct.add(vo.getStatus());

        if (distinct.size() == 1) {
            order.setStatus(distinct.iterator().next());
            return;
        }

        OrderStatus leastProgressed = null;
        for (VendorOrder vo : subOrders) {
            int idx = PROGRESS_ORDER.indexOf(vo.getStatus());
            if (idx == -1) continue; // CANCELLED/RETURNED/RETURN_REQUESTED — skip while others are active
            if (leastProgressed == null || idx < PROGRESS_ORDER.indexOf(leastProgressed)) {
                leastProgressed = vo.getStatus();
            }
        }
        order.setStatus(leastProgressed != null ? leastProgressed : OrderStatus.CANCELLED);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.SubOrderResponse> subOrders = order.getVendorOrders().stream()
                .map(this::toSubOrderResponse)
                .toList();

        PaymentResponse paymentResponse = null;
        try {
            Payment payment = paymentService.getByOrderId(order.getId());
            paymentResponse = PaymentResponse.builder()
                    .id(payment.getId())
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .amount(payment.getAmount())
                    .method(payment.getMethod())
                    .status(payment.getStatus())
                    .transactionId(payment.getTransactionId())
                    .methodDetail(payment.getMethodDetail())
                    .failureReason(payment.getFailureReason())
                    .createdAt(payment.getCreatedAt())
                    .paidAt(payment.getPaidAt())
                    .refundedAt(payment.getRefundedAt())
                    .build();
        } catch (ResourceNotFoundException ignored) {
            // Shouldn't happen for orders placed through placeOrder(), but don't let a
            // missing payment record break the whole order response.
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())
                .subOrders(subOrders)
                .shippingFullName(order.getShippingFullName())
                .shippingPhone(order.getShippingPhone())
                .shippingAddressLine(order.getShippingAddressLine())
                .shippingCity(order.getShippingCity())
                .shippingState(order.getShippingState())
                .shippingPincode(order.getShippingPincode())
                .payment(paymentResponse)
                .build();
    }

    private OrderResponse.SubOrderResponse toSubOrderResponse(VendorOrder vendorOrder) {
        List<OrderResponse.OrderItemResponse> items = vendorOrder.getItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .build())
                .toList();

        return OrderResponse.SubOrderResponse.builder()
                .id(vendorOrder.getId())
                .subOrderNumber(vendorOrder.getSubOrderNumber())
                .vendorId(vendorOrder.getVendor().getId())
                .vendorStoreName(vendorOrder.getVendor().getStoreName())
                .status(vendorOrder.getStatus())
                .subtotal(vendorOrder.getSubtotal())
                .items(items)
                .shipment(toShipmentResponse(vendorOrder).orElse(null))
                .build();
    }

    private VendorOrderResponse toVendorOrderResponse(VendorOrder vendorOrder) {
        List<OrderResponse.OrderItemResponse> items = vendorOrder.getItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .build())
                .toList();

        return VendorOrderResponse.builder()
                .id(vendorOrder.getId())
                .subOrderNumber(vendorOrder.getSubOrderNumber())
                .parentOrderNumber(vendorOrder.getOrder().getOrderNumber())
                .vendorId(vendorOrder.getVendor().getId())
                .vendorStoreName(vendorOrder.getVendor().getStoreName())
                .status(vendorOrder.getStatus())
                .subtotal(vendorOrder.getSubtotal())
                .commissionRate(vendorOrder.getCommissionRate())
                .commissionAmount(vendorOrder.getCommissionAmount())
                .payoutAmount(vendorOrder.getPayoutAmount())
                .settled(vendorOrder.getSettlement() != null)
                .settlementId(vendorOrder.getSettlement() != null ? vendorOrder.getSettlement().getId() : null)
                .items(items)
                .shipment(toShipmentResponse(vendorOrder).orElse(null))
                .createdAt(vendorOrder.getCreatedAt())
                .build();
    }

    private Optional<ShipmentResponse> toShipmentResponse(VendorOrder vendorOrder) {
        return shipmentRepository.findByVendorOrderId(vendorOrder.getId()).map(s -> ShipmentResponse.builder()
                .id(s.getId())
                .vendorOrderId(vendorOrder.getId())
                .subOrderNumber(vendorOrder.getSubOrderNumber())
                .courierName(s.getCourierName())
                .awbNumber(s.getAwbNumber())
                .trackingUrl(s.getTrackingUrl())
                .estimatedDeliveryDate(s.getEstimatedDeliveryDate())
                .status(s.getStatus())
                .lastError(s.getLastError())
                .updatedAt(s.getUpdatedAt())
                .build());
    }
}
