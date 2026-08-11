package com.multivendor.ecommerce.service.impl;

import com.multivendor.ecommerce.config.ShiprocketProperties;
import com.multivendor.ecommerce.dto.request.CreateShipmentRequest;
import com.multivendor.ecommerce.dto.response.ShipmentResponse;
import com.multivendor.ecommerce.entity.Order;
import com.multivendor.ecommerce.entity.OrderItem;
import com.multivendor.ecommerce.entity.Shipment;
import com.multivendor.ecommerce.entity.VendorOrder;
import com.multivendor.ecommerce.entity.enums.PaymentMethod;
import com.multivendor.ecommerce.entity.enums.ShipmentStatus;
import com.multivendor.ecommerce.exception.BadRequestException;
import com.multivendor.ecommerce.exception.ForbiddenException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.ShipmentRepository;
import com.multivendor.ecommerce.repository.VendorOrderRepository;
import com.multivendor.ecommerce.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final VendorOrderRepository vendorOrderRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShiprocketClient shiprocketClient;
    private final ShiprocketProperties shiprocketProperties;

    @Override
    public ShipmentResponse createShipment(Long vendorOrderId, CreateShipmentRequest request) {
        if (!shiprocketProperties.isEnabled()) {
            throw new BadRequestException(
                    "Shipping isn't configured yet. Set SHIPROCKET_ENABLED=true and your account "
                            + "credentials once you have a Shiprocket account.");
        }

        VendorOrder vendorOrder = vendorOrderRepository.findById(vendorOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-order not found"));

        Shipment shipment = shipmentRepository.findByVendorOrderId(vendorOrderId).orElse(null);
        if (shipment != null && shipment.getStatus() != ShipmentStatus.FAILED) {
            throw new BadRequestException("A shipment has already been booked for this sub-order");
        }
        if (shipment == null) {
            shipment = Shipment.builder().vendorOrder(vendorOrder).build();
        }

        try {
            Map<String, Object> payload = buildOrderPayload(vendorOrder, request);
            Map<?, ?> response = shiprocketClient.createOrder(payload);

            Object shiprocketOrderId = response != null ? response.get("order_id") : null;
            Object shiprocketShipmentId = response != null ? response.get("shipment_id") : null;

            shipment.setShiprocketOrderId(toLong(shiprocketOrderId));
            shipment.setShiprocketShipmentId(toLong(shiprocketShipmentId));
            shipment.setStatus(ShipmentStatus.PICKUP_SCHEDULED);
            shipment.setLastError(null);
            shipment.setUpdatedAt(LocalDateTime.now());
        } catch (RestClientException | IllegalStateException e) {
            // Don't let a Shiprocket outage/misconfiguration surface as a raw 500 — record
            // the failure on the Shipment so admins/vendors can see it and retry.
            log.warn("Shiprocket createOrder failed for vendorOrder {}: {}", vendorOrderId, e.getMessage());
            shipment.setStatus(ShipmentStatus.FAILED);
            shipment.setLastError(e.getMessage());
            shipment.setUpdatedAt(LocalDateTime.now());
        }

        shipment = shipmentRepository.save(shipment);
        return toResponse(shipment, vendorOrder);
    }

    @Override
    public ShipmentResponse refreshTracking(Long vendorOrderId) {
        VendorOrder vendorOrder = vendorOrderRepository.findById(vendorOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-order not found"));

        Shipment shipment = shipmentRepository.findByVendorOrderId(vendorOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("No shipment booked for this sub-order yet"));

        if (shipment.getAwbNumber() == null) {
            throw new BadRequestException("No AWB assigned yet — the courier hasn't been confirmed by Shiprocket");
        }

        try {
            Map<?, ?> response = shiprocketClient.trackByAwb(shipment.getAwbNumber());
            applyTrackingResponse(shipment, response);
            shipment.setLastError(null);
        } catch (RestClientException e) {
            log.warn("Shiprocket tracking refresh failed for vendorOrder {}: {}", vendorOrderId, e.getMessage());
            shipment.setLastError(e.getMessage());
        }
        shipment.setUpdatedAt(LocalDateTime.now());
        shipment = shipmentRepository.save(shipment);
        return toResponse(shipment, vendorOrder);
    }

    @Override
    public ShipmentResponse getShipment(Long vendorOrderId) {
        VendorOrder vendorOrder = vendorOrderRepository.findById(vendorOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-order not found"));
        Shipment shipment = shipmentRepository.findByVendorOrderId(vendorOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("No shipment booked for this sub-order yet"));
        return toResponse(shipment, vendorOrder);
    }

    @Override
    public void handleWebhook(String providedSecret, Map<String, Object> payload) {
        if (shiprocketProperties.getWebhookSecret() == null
                || !shiprocketProperties.getWebhookSecret().equals(providedSecret)) {
            throw new ForbiddenException("Invalid webhook secret");
        }

        String awb = firstNonNull(payload.get("awb"), payload.get("awb_code"));
        if (awb == null) {
            log.warn("Shiprocket webhook payload had no awb/awb_code field: {}", payload);
            return;
        }

        Shipment shipment = shipmentRepository.findByAwbNumber(awb).orElse(null);
        if (shipment == null) {
            log.warn("Shiprocket webhook referenced unknown AWB {}", awb);
            return;
        }

        applyTrackingResponse(shipment, payload);
        shipment.setUpdatedAt(LocalDateTime.now());
        shipmentRepository.save(shipment);
    }

    // ---------- helpers ----------

    private Map<String, Object> buildOrderPayload(VendorOrder vendorOrder, CreateShipmentRequest request) {
        Order order = vendorOrder.getOrder();

        List<Map<String, Object>> items = vendorOrder.getItems().stream()
                .map(this::toShiprocketItem)
                .toList();

        String[] nameParts = splitName(order.getShippingFullName());

        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("order_id", vendorOrder.getSubOrderNumber());
        payload.put("order_date", vendorOrder.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        payload.put("pickup_location", shiprocketProperties.getPickupLocation());
        payload.put("billing_customer_name", nameParts[0]);
        payload.put("billing_last_name", nameParts[1]);
        payload.put("billing_address", order.getShippingAddressLine());
        payload.put("billing_city", order.getShippingCity());
        payload.put("billing_pincode", order.getShippingPincode());
        payload.put("billing_state", order.getShippingState());
        payload.put("billing_country", "India");
        payload.put("billing_email", order.getUser().getEmail());
        payload.put("billing_phone", order.getShippingPhone());
        payload.put("shipping_is_billing", true);
        payload.put("order_items", items);
        payload.put("payment_method", order.getPaymentMethod() == PaymentMethod.COD ? "COD" : "Prepaid");
        payload.put("sub_total", vendorOrder.getSubtotal());
        payload.put("length", request.getLengthCm() != null ? request.getLengthCm() : 10.0);
        payload.put("breadth", request.getBreadthCm() != null ? request.getBreadthCm() : 10.0);
        payload.put("height", request.getHeightCm() != null ? request.getHeightCm() : 10.0);
        payload.put("weight", request.getWeightKg() != null ? request.getWeightKg() : 0.5);
        return payload;
    }

    private Map<String, Object> toShiprocketItem(OrderItem item) {
        return Map.of(
                "name", item.getProduct().getName(),
                "sku", item.getProduct().getSku(),
                "units", item.getQuantity(),
                "selling_price", item.getPriceAtPurchase()
        );
    }

    private void applyTrackingResponse(Shipment shipment, Map<?, ?> response) {
        if (response == null) return;

        String courier = asString(response.get("courier_name"));
        if (courier != null) shipment.setCourierName(courier);

        String awb = asString(firstNonNull(response.get("awb"), response.get("awb_code")));
        if (awb != null) shipment.setAwbNumber(awb);

        String trackingUrl = asString(response.get("track_url"));
        if (trackingUrl != null) shipment.setTrackingUrl(trackingUrl);

        String eta = asString(firstNonNull(response.get("etd"), response.get("edd")));
        if (eta != null) shipment.setEstimatedDeliveryDate(eta);

        String statusText = asString(firstNonNull(
                response.get("current_status"), response.get("shipment_status"), response.get("status")));
        if (statusText != null) {
            shipment.setStatus(mapShiprocketStatus(statusText));
        }
    }

    /** Best-effort mapping — Shiprocket's exact status vocabulary should be confirmed against a live account. */
    private ShipmentStatus mapShiprocketStatus(String raw) {
        String s = raw.toLowerCase();
        if (s.contains("delivered")) return ShipmentStatus.DELIVERED;
        if (s.contains("out for delivery")) return ShipmentStatus.OUT_FOR_DELIVERY;
        if (s.contains("transit")) return ShipmentStatus.IN_TRANSIT;
        if (s.contains("picked")) return ShipmentStatus.PICKED_UP;
        if (s.contains("rto") || s.contains("return")) return ShipmentStatus.RTO;
        if (s.contains("cancel")) return ShipmentStatus.CANCELLED;
        if (s.contains("pickup")) return ShipmentStatus.PICKUP_SCHEDULED;
        return ShipmentStatus.PICKUP_SCHEDULED;
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) return new String[]{"Customer", ""};
        String[] parts = fullName.trim().split("\\s+", 2);
        return parts.length == 2 ? parts : new String[]{parts[0], ""};
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private String firstNonNull(Object... values) {
        for (Object v : values) {
            if (v != null) return asString(v);
        }
        return null;
    }

    private ShipmentResponse toResponse(Shipment shipment, VendorOrder vendorOrder) {
        return ShipmentResponse.builder()
                .id(shipment.getId())
                .vendorOrderId(vendorOrder.getId())
                .subOrderNumber(vendorOrder.getSubOrderNumber())
                .courierName(shipment.getCourierName())
                .awbNumber(shipment.getAwbNumber())
                .trackingUrl(shipment.getTrackingUrl())
                .estimatedDeliveryDate(shipment.getEstimatedDeliveryDate())
                .status(shipment.getStatus())
                .lastError(shipment.getLastError())
                .updatedAt(shipment.getUpdatedAt())
                .build();
    }
}
