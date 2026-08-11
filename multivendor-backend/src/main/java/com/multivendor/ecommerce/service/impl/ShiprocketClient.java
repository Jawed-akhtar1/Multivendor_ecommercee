package com.multivendor.ecommerce.service.impl;

import com.multivendor.ecommerce.config.ShiprocketProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around Shiprocket's REST API (https://apiv2.shiprocket.in/v1/external).
 * Written from their publicly documented API without the ability to make a
 * live call from this environment — field names for order creation in
 * particular (billing_*, order_items[].*, etc.) follow their published docs
 * as of this writing but MUST be verified against a real test call before
 * relying on this. If Shiprocket rejects a request, start by comparing the
 * payload shape here against their current API reference.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShiprocketClient {

    private final RestTemplate restTemplate;
    private final ShiprocketProperties properties;

    private String cachedToken;
    private LocalDateTime tokenExpiry;

    /** Shiprocket tokens are valid ~10 days; we refresh a day early to be safe. */
    private synchronized String getToken() {
        if (cachedToken != null && tokenExpiry != null && LocalDateTime.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }

        Map<String, String> body = Map.of("email", properties.getEmail(), "password", properties.getPassword());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var response = restTemplate.postForEntity(
                properties.getBaseUrl() + "/auth/login", new HttpEntity<>(body, headers), Map.class);

        Map<?, ?> respBody = response.getBody();
        if (respBody == null || respBody.get("token") == null) {
            throw new IllegalStateException("Shiprocket login did not return a token");
        }

        cachedToken = (String) respBody.get("token");
        tokenExpiry = LocalDateTime.now().plusDays(9);
        return cachedToken;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getToken());
        return headers;
    }

    /** POST /orders/create/adhoc — books a new order/shipment. Returns the raw response body. */
    public Map<?, ?> createOrder(Map<String, Object> orderPayload) {
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(orderPayload, authHeaders());
        var response = restTemplate.postForEntity(
                properties.getBaseUrl() + "/orders/create/adhoc", entity, Map.class);
        return response.getBody();
    }

    /** GET /courier/track/awb/{awb} — current tracking status for an AWB. */
    public Map<?, ?> trackByAwb(String awb) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        var response = restTemplate.exchange(
                properties.getBaseUrl() + "/courier/track/awb/" + awb, HttpMethod.GET, entity, Map.class);
        return response.getBody();
    }

    /** POST /orders/cancel — cancels one or more Shiprocket orders by their Shiprocket order id. */
    public Map<?, ?> cancelOrder(Long shiprocketOrderId) {
        Map<String, Object> body = Map.of("ids", List.of(shiprocketOrderId));
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, authHeaders());
        var response = restTemplate.postForEntity(
                properties.getBaseUrl() + "/orders/cancel", entity, Map.class);
        return response.getBody();
    }
}
