package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.request.PlaceOrderRequest;
import com.multivendor.ecommerce.dto.response.OrderResponse;
import com.multivendor.ecommerce.dto.response.VendorOrderResponse;
import com.multivendor.ecommerce.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse placeOrder(Long userId, PlaceOrderRequest request);

    Page<OrderResponse> getMyOrders(Long userId, Pageable pageable);

    OrderResponse getMyOrder(Long userId, Long orderId);

    /** Cancels every sub-order belonging to this order (only while all are still cancellable). */
    OrderResponse cancelOrder(Long userId, Long orderId);

    // Vendor: view/manage only their own sub-orders
    Page<VendorOrderResponse> getVendorOrders(Long vendorUserId, Pageable pageable);

    VendorOrderResponse updateVendorOrderStatus(Long vendorUserId, Long vendorOrderId, OrderStatus status);

    // Admin: monitor all orders
    Page<OrderResponse> getAllOrders(Pageable pageable);
}
