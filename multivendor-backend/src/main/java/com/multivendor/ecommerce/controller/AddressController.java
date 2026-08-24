package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.dto.request.AddressRequest;
import com.multivendor.ecommerce.dto.response.AddressResponse;
import com.multivendor.ecommerce.service.AddressService;
import com.multivendor.ecommerce.util.ApiResponse;
import com.multivendor.ecommerce.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getMyAddresses() {
        return ResponseEntity.ok(ApiResponse.success(addressService.getMyAddresses(SecurityUtils.getCurrentUserId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> add(@Valid @RequestBody AddressRequest request) {
        AddressResponse address = addressService.add(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Address added", address));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> update(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        AddressResponse address = addressService.update(SecurityUtils.getCurrentUserId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated", address));
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefault(@PathVariable Long id) {
        AddressResponse address = addressService.setDefault(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Default address updated", address));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        addressService.delete(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Address removed", null));
    }
}
