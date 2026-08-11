package com.multivendor.ecommerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.shiprocket")
@Getter
@Setter
public class ShiprocketProperties {

    /** Keep false until real credentials are configured. */
    private boolean enabled = false;

    private String baseUrl = "https://apiv2.shiprocket.in/v1/external";
    private String email;
    private String password;

    /** Must exactly match a pickup location nickname registered in your Shiprocket dashboard. */
    private String pickupLocation;

    /** Shared secret you configure in the Shiprocket webhook dashboard, checked on incoming webhooks. */
    private String webhookSecret;
}
