package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Called directly by Shiprocket's servers when a shipment's status changes —
 * must stay public (see SecurityConfig), authenticated instead via the
 * shared secret configured in your Shiprocket webhook dashboard, sent back
 * as the header named below (confirm the exact header name Shiprocket uses
 * against their current webhook docs — this was not verified live).
 */
@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestHeader(value = "X-Api-Key", required = false) String apiKey,
            @RequestBody Map<String, Object> payload) {
        shippingService.handleWebhook(apiKey, payload);
        return ResponseEntity.ok().build();
    }
}
