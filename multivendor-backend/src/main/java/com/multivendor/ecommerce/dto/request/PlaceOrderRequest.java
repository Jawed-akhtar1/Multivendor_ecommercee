package com.multivendor.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceOrderRequest {

    @NotNull(message = "Address id is required")
    private Long addressId;

    // "COD" or "CCAVENUE". For CCAVENUE, placing the order creates it in an
    // unpaid state — the client must then call POST /api/payments/ccavenue/initiate/{orderId}
    // to get the redirect form fields for CCAvenue's hosted payment page.
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}
