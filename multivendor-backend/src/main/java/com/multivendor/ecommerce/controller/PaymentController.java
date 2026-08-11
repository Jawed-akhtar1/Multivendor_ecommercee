package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.dto.response.CCAvenueInitiateResponse;
import com.multivendor.ecommerce.dto.response.PaymentResponse;
import com.multivendor.ecommerce.entity.Payment;
import com.multivendor.ecommerce.service.PaymentService;
import com.multivendor.ecommerce.util.ApiResponse;
import com.multivendor.ecommerce.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> myPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponse> payments = paymentService.getMyPayments(SecurityUtils.getCurrentUserId(), pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> paymentForOrder(@PathVariable Long orderId) {
        Payment payment = paymentService.getByOrderId(orderId);
        if (!payment.getUser().getId().equals(SecurityUtils.getCurrentUserId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("This payment record does not belong to you"));
        }
        return ResponseEntity.ok(ApiResponse.success(toResponse(payment)));
    }

    /** Called by the frontend right after placing a CCAVENUE order, to get the redirect-form fields. */
    @PostMapping("/ccavenue/initiate/{orderId}")
    public ResponseEntity<ApiResponse<CCAvenueInitiateResponse>> initiate(@PathVariable Long orderId) {
        CCAvenueInitiateResponse response = paymentService.initiateCCAvenuePayment(orderId, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Called directly by CCAvenue's servers (application/x-www-form-urlencoded POST,
     * no JWT) once the customer finishes on their hosted page. Must stay public —
     * see SecurityConfig. Responds with a 302 redirect back to the frontend so the
     * customer's browser lands on an order status page either way.
     */
    @PostMapping(value = "/ccavenue/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RedirectView callback(@RequestParam("encResp") String encResp,
                                  @org.springframework.beans.factory.annotation.Value("${app.ccavenue.frontend-return-url}") String frontendReturnUrl) {
        String orderNumber;
        try {
            orderNumber = paymentService.handleCCAvenueCallback(encResp);
        } catch (Exception e) {
            // Never let a malformed/failed callback throw a raw 500 at CCAvenue's server —
            // send the customer to the frontend, which can show a generic "check your orders" state.
            return new RedirectView(frontendReturnUrl);
        }
        return new RedirectView(frontendReturnUrl + "?justPaid=" + orderNumber);
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
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
    }
}
