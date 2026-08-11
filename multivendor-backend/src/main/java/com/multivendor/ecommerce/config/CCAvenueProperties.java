package com.multivendor.ecommerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Credentials and endpoints come from your CCAvenue merchant dashboard
 * (Settings > API Keys). CCAvenue does not use a request-signed REST API —
 * it uses a hosted checkout page: the merchant AES-encrypts an order
 * parameter string with the Working Key and posts it (with the Access Code)
 * to CCAvenue's transaction URL, which redirects the browser to their page.
 *
 * IMPORTANT: verify the current transaction URL and parameter list against
 * CCAvenue's own integration kit/docs before going live — payment gateway
 * APIs occasionally change and this was written without the ability to hit
 * their servers to confirm.
 */
@Component
@ConfigurationProperties(prefix = "app.ccavenue")
@Getter
@Setter
public class CCAvenueProperties {

    /** Keep false until real merchant credentials are configured. */
    private boolean enabled = false;

    private String merchantId;
    private String workingKey;
    private String accessCode;

    /** CCAvenue's hosted transaction endpoint (test vs live differ — confirm in your dashboard). */
    private String transactionUrl = "https://securegw-stage.ccavenue.com/transaction/transaction.do?command=initiateTransaction";

    /** Publicly reachable URL CCAvenue will POST the encrypted result back to. Must be HTTPS in production. */
    private String redirectUrl;

    /** Where to send the customer's browser after we've processed the callback. */
    private String frontendReturnUrl = "http://localhost:5173/orders";
}
