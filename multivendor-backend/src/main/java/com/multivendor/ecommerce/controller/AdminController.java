package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.dto.request.CategoryRequest;
import com.multivendor.ecommerce.dto.request.CreateShipmentRequest;
import com.multivendor.ecommerce.dto.request.MarkSettlementPaidRequest;
import com.multivendor.ecommerce.dto.request.UpdateCommissionRequest;
import com.multivendor.ecommerce.dto.response.*;
import com.multivendor.ecommerce.entity.Category;
import com.multivendor.ecommerce.service.*;
import com.multivendor.ecommerce.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * All endpoints here require ROLE_ADMIN (enforced in SecurityConfig on /api/admin/**).
 * Admin accounts are not self-registrable via /api/auth/register; seed one directly in the
 * database (see README) or promote an existing user's role.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final ShippingService shippingService;
    private final SettlementService settlementService;

    // --- Vendor Approval & Commission ---

    @GetMapping("/vendors/pending")
    public ResponseEntity<ApiResponse<List<VendorResponse>>> pendingVendors() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPendingVendors()));
    }

    @GetMapping("/vendors/approved")
    public ResponseEntity<ApiResponse<List<VendorResponse>>> approvedVendors() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getApprovedVendors()));
    }

    @PostMapping("/vendors/{vendorId}/approve")
    public ResponseEntity<ApiResponse<VendorResponse>> approveVendor(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success("Vendor approved", adminService.approveVendor(vendorId)));
    }

    @PostMapping("/vendors/{vendorId}/reject")
    public ResponseEntity<ApiResponse<VendorResponse>> rejectVendor(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success("Vendor application rejected", adminService.rejectVendor(vendorId)));
    }

    /** Sets a vendor-specific commission override. Send null/omit to fall back to the platform default. */
    @PutMapping("/vendors/{vendorId}/commission")
    public ResponseEntity<ApiResponse<VendorResponse>> setCommission(
            @PathVariable Long vendorId, @Valid @RequestBody UpdateCommissionRequest request) {
        VendorResponse response = adminService.setCommissionRate(vendorId, request.getCommissionRate());
        return ResponseEntity.ok(ApiResponse.success("Commission rate updated", response));
    }

    // --- Category Management ---

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<Category>> createCategory(@Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Category created", category));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Category updated", categoryService.update(id, request)));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }

    // --- Order Monitoring ---

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> allOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(orderService.getAllOrders(pageable)));
    }

    // --- Shipping (admin fallback — normally the vendor books their own sub-order's shipment) ---

    @PostMapping("/vendor-orders/{vendorOrderId}/shipment")
    public ResponseEntity<ApiResponse<ShipmentResponse>> bookShipment(
            @PathVariable Long vendorOrderId, @RequestBody(required = false) CreateShipmentRequest request) {
        CreateShipmentRequest body = request != null ? request : new CreateShipmentRequest();
        return ResponseEntity.ok(ApiResponse.success(shippingService.createShipment(vendorOrderId, body)));
    }

    // --- Settlements (commission / vendor payouts) ---

    @GetMapping("/settlements")
    public ResponseEntity<ApiResponse<Page<SettlementResponse>>> allSettlements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(settlementService.getAllSettlements(pageable)));
    }

    @GetMapping("/vendors/{vendorId}/settlements/eligible")
    public ResponseEntity<ApiResponse<List<VendorOrderResponse>>> eligibleForSettlement(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success(settlementService.getEligiblePreview(vendorId)));
    }

    @PostMapping("/vendors/{vendorId}/settlements/generate")
    public ResponseEntity<ApiResponse<SettlementResponse>> generateSettlement(@PathVariable Long vendorId) {
        SettlementResponse response = settlementService.generateSettlement(vendorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Settlement generated", response));
    }

    @PostMapping("/settlements/{settlementId}/mark-paid")
    public ResponseEntity<ApiResponse<SettlementResponse>> markSettlementPaid(
            @PathVariable Long settlementId, @Valid @RequestBody MarkSettlementPaidRequest request) {
        SettlementResponse response = settlementService.markPaid(settlementId, request.getPaymentReference(), request.getNotes());
        return ResponseEntity.ok(ApiResponse.success("Settlement marked paid", response));
    }
}
