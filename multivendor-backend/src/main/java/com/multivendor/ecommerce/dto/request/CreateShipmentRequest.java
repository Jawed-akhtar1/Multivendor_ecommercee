package com.multivendor.ecommerce.dto.request;

import lombok.Getter;
import lombok.Setter;

/** Package details for booking a courier. All optional — sane defaults are used if omitted. */
@Getter
@Setter
public class CreateShipmentRequest {
    private Double weightKg;      // default 0.5
    private Double lengthCm;      // default 10
    private Double breadthCm;     // default 10
    private Double heightCm;      // default 10
}
