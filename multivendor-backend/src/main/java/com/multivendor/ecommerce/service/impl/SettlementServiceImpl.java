package com.multivendor.ecommerce.service.impl;

import com.multivendor.ecommerce.dto.response.OrderResponse;
import com.multivendor.ecommerce.dto.response.SettlementResponse;
import com.multivendor.ecommerce.dto.response.ShipmentResponse;
import com.multivendor.ecommerce.dto.response.VendorOrderResponse;
import com.multivendor.ecommerce.entity.Settlement;
import com.multivendor.ecommerce.entity.Vendor;
import com.multivendor.ecommerce.entity.VendorOrder;
import com.multivendor.ecommerce.entity.enums.OrderStatus;
import com.multivendor.ecommerce.entity.enums.SettlementStatus;
import com.multivendor.ecommerce.entity.enums.PaymentStatus;
import com.multivendor.ecommerce.exception.BadRequestException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.SettlementRepository;
import com.multivendor.ecommerce.repository.ShipmentRepository;
import com.multivendor.ecommerce.repository.VendorOrderRepository;
import com.multivendor.ecommerce.repository.VendorRepository;
import com.multivendor.ecommerce.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final VendorOrderRepository vendorOrderRepository;
    private final VendorRepository vendorRepository;
    private final ShipmentRepository shipmentRepository;

    @Override
    public List<VendorOrderResponse> getEligiblePreview(Long vendorId) {
        return vendorOrderRepository.findSettlementEligible(vendorId, OrderStatus.DELIVERED, PaymentStatus.PAID)
                .stream().map(this::toVendorOrderResponse).toList();
    }

    @Override
    @Transactional
    public SettlementResponse generateSettlement(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        List<VendorOrder> eligible = vendorOrderRepository.findSettlementEligible(vendorId, OrderStatus.DELIVERED, PaymentStatus.PAID);
        if (eligible.isEmpty()) {
            throw new BadRequestException("No delivered, paid, unsettled sub-orders to settle for this vendor");
        }
        if (vendor.getBankAccountNumber() == null || vendor.getBankAccountNumber().isBlank()) {
            throw new BadRequestException("This vendor hasn't added payout bank details yet");
        }

        BigDecimal grossSales = BigDecimal.ZERO;
        BigDecimal totalCommission = BigDecimal.ZERO;
        BigDecimal netPayout = BigDecimal.ZERO;
        for (VendorOrder vo : eligible) {
            grossSales = grossSales.add(vo.getSubtotal());
            totalCommission = totalCommission.add(vo.getCommissionAmount());
            netPayout = netPayout.add(vo.getPayoutAmount());
        }

        Settlement settlement = Settlement.builder()
                .settlementNumber(generateSettlementNumber())
                .vendor(vendor)
                .subOrderCount(eligible.size())
                .grossSales(grossSales)
                .totalCommission(totalCommission)
                .netPayout(netPayout)
                .status(SettlementStatus.PENDING)
                .build();
        settlement = settlementRepository.save(settlement);

        for (VendorOrder vo : eligible) {
            vo.setSettlement(settlement);
            vendorOrderRepository.save(vo);
        }
        settlement.setVendorOrders(eligible);

        return toSettlementResponse(settlement);
    }

    @Override
    @Transactional
    public SettlementResponse markPaid(Long settlementId, String paymentReference, String notes) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found"));

        if (settlement.getStatus() == SettlementStatus.PAID) {
            throw new BadRequestException("This settlement is already marked paid");
        }

        // NOTE: this only records that a payout happened — it does not itself move money.
        // Actually transferring funds to the vendor's bank account (NEFT/IMPS/UPI payout API,
        // etc.) is a separate step outside this codebase; this is the record-keeping half.
        settlement.setStatus(SettlementStatus.PAID);
        settlement.setPaymentReference(paymentReference);
        settlement.setNotes(notes);
        settlement.setPaidAt(LocalDateTime.now());
        settlement = settlementRepository.save(settlement);

        return toSettlementResponse(settlement);
    }

    @Override
    public Page<SettlementResponse> getVendorSettlements(Long vendorId, Pageable pageable) {
        return settlementRepository.findByVendorId(vendorId, pageable).map(this::toSettlementResponse);
    }

    @Override
    public Page<SettlementResponse> getAllSettlements(Pageable pageable) {
        return settlementRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toSettlementResponse);
    }

    private String generateSettlementNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "STL-" + datePart + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private SettlementResponse toSettlementResponse(Settlement settlement) {
        List<String> subOrderNumbers = settlement.getVendorOrders().stream()
                .map(VendorOrder::getSubOrderNumber).toList();

        return SettlementResponse.builder()
                .id(settlement.getId())
                .settlementNumber(settlement.getSettlementNumber())
                .vendorId(settlement.getVendor().getId())
                .vendorStoreName(settlement.getVendor().getStoreName())
                .subOrderCount(settlement.getSubOrderCount())
                .grossSales(settlement.getGrossSales())
                .totalCommission(settlement.getTotalCommission())
                .netPayout(settlement.getNetPayout())
                .status(settlement.getStatus())
                .paymentReference(settlement.getPaymentReference())
                .notes(settlement.getNotes())
                .createdAt(settlement.getCreatedAt())
                .paidAt(settlement.getPaidAt())
                .subOrderNumbers(subOrderNumbers)
                .build();
    }

    private VendorOrderResponse toVendorOrderResponse(VendorOrder vendorOrder) {
        List<OrderResponse.OrderItemResponse> items = vendorOrder.getItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .build())
                .toList();

        ShipmentResponse shipmentResponse = shipmentRepository.findByVendorOrderId(vendorOrder.getId())
                .map(s -> ShipmentResponse.builder()
                        .id(s.getId())
                        .vendorOrderId(vendorOrder.getId())
                        .subOrderNumber(vendorOrder.getSubOrderNumber())
                        .courierName(s.getCourierName())
                        .awbNumber(s.getAwbNumber())
                        .trackingUrl(s.getTrackingUrl())
                        .estimatedDeliveryDate(s.getEstimatedDeliveryDate())
                        .status(s.getStatus())
                        .lastError(s.getLastError())
                        .updatedAt(s.getUpdatedAt())
                        .build())
                .orElse(null);

        return VendorOrderResponse.builder()
                .id(vendorOrder.getId())
                .subOrderNumber(vendorOrder.getSubOrderNumber())
                .parentOrderNumber(vendorOrder.getOrder().getOrderNumber())
                .vendorId(vendorOrder.getVendor().getId())
                .vendorStoreName(vendorOrder.getVendor().getStoreName())
                .status(vendorOrder.getStatus())
                .subtotal(vendorOrder.getSubtotal())
                .commissionRate(vendorOrder.getCommissionRate())
                .commissionAmount(vendorOrder.getCommissionAmount())
                .payoutAmount(vendorOrder.getPayoutAmount())
                .settled(vendorOrder.getSettlement() != null)
                .settlementId(vendorOrder.getSettlement() != null ? vendorOrder.getSettlement().getId() : null)
                .items(items)
                .shipment(shipmentResponse)
                .createdAt(vendorOrder.getCreatedAt())
                .build();
    }
}
