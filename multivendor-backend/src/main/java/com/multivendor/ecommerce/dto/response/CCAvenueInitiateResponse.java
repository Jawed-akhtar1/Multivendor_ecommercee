package com.multivendor.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Everything the frontend needs to auto-submit a hidden HTML form (POST) to
 * CCAvenue's hosted transaction page — the browser must actually navigate
 * there, this can't be done via a background fetch/XHR.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CCAvenueInitiateResponse {
    private String encRequest;
    private String accessCode;
    private String transactionUrl;
}
