package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.request.CreateShipmentRequest;
import com.multivendor.ecommerce.dto.response.ShipmentResponse;

public interface ShippingService {

    /** Books a courier for a vendor's sub-order via Shiprocket. */
    ShipmentResponse createShipment(Long vendorOrderId, CreateShipmentRequest request);

    /** Pulls the latest status from Shiprocket for an already-booked shipment. */
    ShipmentResponse refreshTracking(Long vendorOrderId);

    ShipmentResponse getShipment(Long vendorOrderId);

    /** Processes a Shiprocket webhook payload (delivery status updates pushed to us). */
    void handleWebhook(String providedSecret, java.util.Map<String, Object> payload);
}
