package com.multivendor.ecommerce.dto.response;

import com.multivendor.ecommerce.entity.enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {
    private Long id;
    private Long vendorOrderId;
    private String subOrderNumber;
    private String courierName;
    private String awbNumber;
    private String trackingUrl;
    private String estimatedDeliveryDate;
    private ShipmentStatus status;
    private String lastError;
    private LocalDateTime updatedAt;
}
