package com.multivendor.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorRegisterRequest {

    @NotBlank(message = "Store name is required")
    private String storeName;

    private String storeDescription;

    private String gstNumber;

    private String logoUrl;

    // Payout bank details — needed before settlements can be marked paid.
    private String bankAccountName;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankName;
}
