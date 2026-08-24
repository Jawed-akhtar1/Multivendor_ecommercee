package com.multivendor.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String label;
    private String fullName;
    private String phone;
    private String addressLine;
    private String landmark;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private boolean defaultAddress;
}
