package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.dto.request.CreateShipmentRequest;
import com.multivendor.ecommerce.dto.request.OrderStatusUpdateRequest;
import com.multivendor.ecommerce.dto.request.ProductRequest;
import com.multivendor.ecommerce.dto.request.VendorRegisterRequest;
import com.multivendor.ecommerce.dto.response.*;
import com.multivendor.ecommerce.service.OrderService;
import com.multivendor.ecommerce.service.SettlementService;
import com.multivendor.ecommerce.service.ShippingService;
import com.multivendor.ecommerce.service.VendorService;
import com.multivendor.ecommerce.util.ApiResponse;
import com.multivendor.ecommerce.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * All endpoints here require ROLE_VENDOR (enforced in SecurityConfig on /api/vendor/**).
 * Product-mutating and order-fulfilment endpoints additionally require the vendor's store
 * to be admin-approved where relevant (enforced in the underlying services).
 */
@RestController
@RequestMapping("/api/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;
    private final OrderService orderService;
    private final ShippingService shippingService;
    private final SettlementService settlementService;

    // --- Store Profile ---

    @PostMapping("/store")
    public ResponseEntity<ApiResponse<VendorResponse>> registerStore(@Valid @RequestBody VendorRegisterRequest request) {
        VendorResponse response = vendorService.registerVendor(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Store registered. Awaiting admin approval.", response));
    }

    @GetMapping("/store")
    public ResponseEntity<ApiResponse<VendorResponse>> getMyStore() {
        return ResponseEntity.ok(ApiResponse.success(vendorService.getMyStore(SecurityUtils.getCurrentUserId())));
    }

    @PutMapping("/store")
    public ResponseEntity<ApiResponse<VendorResponse>> updateMyStore(@Valid @RequestBody VendorRegisterRequest request) {
        VendorResponse response = vendorService.updateMyStore(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Store updated", response));
    }

    // --- Product Management ---

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = vendorService.addProduct(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Product added", response));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> myProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(vendorService.getMyProducts(SecurityUtils.getCurrentUserId(), pageable)));
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long productId, @Valid @RequestBody ProductRequest request) {
        ProductResponse response = vendorService.updateProduct(SecurityUtils.getCurrentUserId(), productId, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated", response));
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        vendorService.deleteProduct(SecurityUtils.getCurrentUserId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Product removed", null));
    }

    // --- Inventory / Stock Management ---

    @PatchMapping("/products/{productId}/stock")
    public ResponseEntity<ApiResponse<ProductResponse>> updateStock(
            @PathVariable Long productId, @RequestBody Map<String, Integer> body) {
        Integer stock = body.get("stock");
        if (stock == null || stock < 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("A valid non-negative 'stock' value is required"));
        }
        ProductResponse response = vendorService.updateStock(SecurityUtils.getCurrentUserId(), productId, stock);
        return ResponseEntity.ok(ApiResponse.success("Stock updated", response));
    }

    // --- Order fulfilment (only this vendor's sub-orders within multi-vendor orders) ---

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<VendorOrderResponse>>> myOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(orderService.getVendorOrders(SecurityUtils.getCurrentUserId(), pageable)));
    }

    @PatchMapping("/orders/{vendorOrderId}/status")
    public ResponseEntity<ApiResponse<VendorOrderResponse>> updateOrderStatus(
            @PathVariable Long vendorOrderId, @Valid @RequestBody OrderStatusUpdateRequest request) {
        var response = orderService.updateVendorOrderStatus(SecurityUtils.getCurrentUserId(), vendorOrderId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Order status updated", response));
    }

    // --- Shipping (vendor-wise — each vendor books/tracks their own sub-order's courier) ---

    @PostMapping("/orders/{vendorOrderId}/shipment")
    public ResponseEntity<ApiResponse<ShipmentResponse>> bookShipment(
            @PathVariable Long vendorOrderId, @RequestBody(required = false) CreateShipmentRequest request) {
        CreateShipmentRequest body = request != null ? request : new CreateShipmentRequest();
        ShipmentResponse response = shippingService.createShipment(vendorOrderId, body);
        return ResponseEntity.ok(ApiResponse.success("Shipment booking submitted", response));
    }

    @GetMapping("/orders/{vendorOrderId}/shipment")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getShipment(@PathVariable Long vendorOrderId) {
        return ResponseEntity.ok(ApiResponse.success(shippingService.getShipment(vendorOrderId)));
    }

    @PostMapping("/orders/{vendorOrderId}/shipment/refresh")
    public ResponseEntity<ApiResponse<ShipmentResponse>> refreshShipment(@PathVariable Long vendorOrderId) {
        return ResponseEntity.ok(ApiResponse.success(shippingService.refreshTracking(vendorOrderId)));
    }

    // --- Settlements (own payout history) ---

    @GetMapping("/settlements")
    public ResponseEntity<ApiResponse<Page<SettlementResponse>>> mySettlements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long vendorId = vendorService.getMyStore(SecurityUtils.getCurrentUserId()).getId();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(settlementService.getVendorSettlements(vendorId, pageable)));
    }

    @GetMapping("/settlements/eligible")
    public ResponseEntity<ApiResponse<List<VendorOrderResponse>>> eligibleForSettlement() {
        Long vendorId = vendorService.getMyStore(SecurityUtils.getCurrentUserId()).getId();
        return ResponseEntity.ok(ApiResponse.success(settlementService.getEligiblePreview(vendorId)));
    }
}
