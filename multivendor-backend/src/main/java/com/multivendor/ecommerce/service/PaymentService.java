package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.response.CCAvenueInitiateResponse;
import com.multivendor.ecommerce.entity.Order;
import com.multivendor.ecommerce.entity.Payment;
import com.multivendor.ecommerce.entity.User;
import com.multivendor.ecommerce.entity.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    /** Creates the initial Payment row when an order is placed (PENDING for both COD and CCAVENUE). */
    Payment createPendingPayment(User user, Order order, PaymentMethod method);

    /** Builds the encrypted CCAvenue request the frontend must POST-redirect the browser to. */
    CCAvenueInitiateResponse initiateCCAvenuePayment(Long orderId, Long userId);

    /**
     * Decrypts and processes CCAvenue's callback POST, updates the matching
     * Payment/Order, and returns the order number so the controller can
     * build a redirect back to the frontend.
     */
    String handleCCAvenueCallback(String encResp);

    /** Marks a COD payment as collected — call when a COD order is delivered. */
    Payment markCodCollected(Order order);

    /** Marks a payment refunded (called on order cancellation/return for a paid order). */
    Payment refund(Order order);

    Payment getByOrderId(Long orderId);

    Page<Payment> getMyPayments(Long userId, Pageable pageable);
}
