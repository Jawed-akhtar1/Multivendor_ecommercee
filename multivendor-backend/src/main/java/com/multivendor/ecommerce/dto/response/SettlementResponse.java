package com.multivendor.ecommerce.dto.response;

import com.multivendor.ecommerce.entity.enums.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementResponse {
    private Long id;
    private String settlementNumber;
    private Long vendorId;
    private String vendorStoreName;
    private Integer subOrderCount;
    private BigDecimal grossSales;
    private BigDecimal totalCommission;
    private BigDecimal netPayout;
    private SettlementStatus status;
    private String paymentReference;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private List<String> subOrderNumbers; // which sub-orders this settlement covers
}
