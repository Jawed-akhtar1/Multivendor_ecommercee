package com.multivendor.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarkSettlementPaidRequest {

    @NotBlank(message = "A payment reference (bank transfer ref / UTR) is required")
    private String paymentReference;

    private String notes;
}
