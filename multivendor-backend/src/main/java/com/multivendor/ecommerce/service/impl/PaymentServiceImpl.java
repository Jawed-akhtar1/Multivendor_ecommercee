package com.multivendor.ecommerce.service.impl;

import com.multivendor.ecommerce.config.CCAvenueProperties;
import com.multivendor.ecommerce.dto.response.CCAvenueInitiateResponse;
import com.multivendor.ecommerce.entity.Order;
import com.multivendor.ecommerce.entity.Payment;
import com.multivendor.ecommerce.entity.User;
import com.multivendor.ecommerce.entity.enums.PaymentMethod;
import com.multivendor.ecommerce.entity.enums.PaymentStatus;
import com.multivendor.ecommerce.exception.BadRequestException;
import com.multivendor.ecommerce.exception.ForbiddenException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.OrderRepository;
import com.multivendor.ecommerce.repository.PaymentRepository;
import com.multivendor.ecommerce.service.PaymentService;
import com.multivendor.ecommerce.util.CCAvenueCrypto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CCAvenueProperties ccAvenueProperties;

    @Override
    public Payment createPendingPayment(User user, Order order, PaymentMethod method) {
        Payment payment = Payment.builder()
                .order(order)
                .user(user)
                .amount(order.getTotalAmount())
                .method(method)
                .status(PaymentStatus.PENDING)
                .methodDetail(method == PaymentMethod.COD ? "Cash on Delivery" : "CCAvenue — awaiting payment")
                .build();
        return paymentRepository.save(payment);
    }

    @Override
    public CCAvenueInitiateResponse initiateCCAvenuePayment(Long orderId, Long userId) {
        if (!ccAvenueProperties.isEnabled()) {
            throw new BadRequestException(
                    "Online payment isn't configured yet. Set CCAVENUE_ENABLED=true and your merchant "
                            + "credentials once you have a CCAvenue account, or choose Cash on Delivery.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(userId)) {
            throw new ForbiddenException("This order does not belong to you");
        }
        if (order.getPaymentMethod() != PaymentMethod.CCAVENUE) {
            throw new BadRequestException("This order was not placed for online payment");
        }

        Payment payment = getByOrderId(orderId);
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException("This order has already been paid for");
        }

        String orderParams = buildOrderParams(order);
        String encRequest = CCAvenueCrypto.encrypt(orderParams, ccAvenueProperties.getWorkingKey());

        return CCAvenueInitiateResponse.builder()
                .encRequest(encRequest)
                .accessCode(ccAvenueProperties.getAccessCode())
                .transactionUrl(ccAvenueProperties.getTransactionUrl())
                .build();
    }

    @Override
    @Transactional
    public String handleCCAvenueCallback(String encResp) {
        String decrypted = CCAvenueCrypto.decrypt(encResp, ccAvenueProperties.getWorkingKey());
        Map<String, String> params = parseQueryString(decrypted);

        String orderNumber = params.get("order_id");
        if (orderNumber == null) {
            throw new BadRequestException("CCAvenue response did not include an order_id");
        }

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No matching order for CCAvenue order_id " + orderNumber));

        Payment payment = getByOrderId(order.getId());

        // Idempotency: CCAvenue may retry the callback; don't reprocess a settled payment.
        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.REFUNDED) {
            return order.getOrderNumber();
        }

        String orderStatus = params.getOrDefault("order_status", "");
        String respAmount = params.get("amount");

        boolean amountMatches = respAmount != null
                && new BigDecimal(respAmount).compareTo(payment.getAmount()) == 0;

        if (!amountMatches) {
            log.warn("CCAvenue callback amount mismatch for order {}: expected {}, got {}",
                    orderNumber, payment.getAmount(), respAmount);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Amount mismatch on gateway callback — treated as failed for safety");
        } else if ("Success".equalsIgnoreCase(orderStatus)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            payment.setTransactionId(params.getOrDefault("tracking_id", params.get("bank_ref_no")));
            payment.setMethodDetail(buildMethodDetail(params));
            order.setPaymentStatus(PaymentStatus.PAID);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(params.getOrDefault("failure_message", "Payment " + orderStatus));
            order.setPaymentStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);
        orderRepository.save(order);

        return order.getOrderNumber();
    }

    @Override
    public Payment markCodCollected(Order order) {
        Payment payment = getByOrderId(order.getId());
        if (payment.getMethod() != PaymentMethod.COD) {
            return payment;
        }
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            payment = paymentRepository.save(payment);
        }
        return payment;
    }

    @Override
    public Payment refund(Order order) {
        Payment payment = getByOrderId(order.getId());
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            // NOTE: this only updates our own records. Actually returning funds
            // requires calling CCAvenue's separate Refund API (needs additional
            // approval from CCAvenue and its own credentials) or issuing the
            // refund manually from the CCAvenue merchant dashboard. Wire that
            // call in here once you have Refund API access.
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundedAt(LocalDateTime.now());
            payment = paymentRepository.save(payment);
        } else if (payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Order cancelled before payment was collected/confirmed");
            payment = paymentRepository.save(payment);
        }
        return payment;
    }

    @Override
    public Payment getByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment record found for this order"));
    }

    @Override
    public Page<Payment> getMyPayments(Long userId, Pageable pageable) {
        return paymentRepository.findByUserId(userId, pageable);
    }

    // ---------- CCAvenue request/response helpers ----------

    private String buildOrderParams(Order order) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("merchant_id", ccAvenueProperties.getMerchantId());
        params.put("order_id", order.getOrderNumber());
        params.put("currency", "INR");
        params.put("amount", order.getTotalAmount().setScale(2, java.math.RoundingMode.HALF_UP).toString());
        params.put("redirect_url", ccAvenueProperties.getRedirectUrl());
        params.put("cancel_url", ccAvenueProperties.getRedirectUrl());
        params.put("language", "EN");

        params.put("billing_name", order.getShippingFullName());
        params.put("billing_address", order.getShippingAddressLine());
        params.put("billing_city", order.getShippingCity());
        params.put("billing_state", order.getShippingState());
        params.put("billing_zip", order.getShippingPincode());
        params.put("billing_country", "India");
        params.put("billing_tel", order.getShippingPhone());
        params.put("billing_email", order.getUser().getEmail());

        params.put("delivery_name", order.getShippingFullName());
        params.put("delivery_address", order.getShippingAddressLine());
        params.put("delivery_city", order.getShippingCity());
        params.put("delivery_state", order.getShippingState());
        params.put("delivery_zip", order.getShippingPincode());
        params.put("delivery_country", "India");
        params.put("delivery_tel", order.getShippingPhone());

        return toQueryString(params);
    }

    private String buildMethodDetail(Map<String, String> params) {
        String paymentMode = params.get("payment_mode");
        String cardName = params.get("card_name");
        if (paymentMode != null && cardName != null) {
            return paymentMode + " — " + cardName;
        }
        if (paymentMode != null) {
            return paymentMode;
        }
        return "CCAvenue";
    }

    private String toQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) sb.append('&');
            sb.append(entry.getKey()).append('=').append(urlEncode(entry.getValue()));
        }
        return sb.toString();
    }

    private Map<String, String> parseQueryString(String query) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx < 0) continue;
            String key = pair.substring(0, idx);
            String value = urlDecode(pair.substring(idx + 1));
            result.put(key, value);
        }
        return result;
    }

    private String urlEncode(String value) {
        if (value == null) return "";
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
