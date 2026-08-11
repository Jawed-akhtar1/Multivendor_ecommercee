package com.multivendor.ecommerce.controller;

import com.multivendor.ecommerce.dto.request.AddressRequest;
import com.multivendor.ecommerce.entity.Address;
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
    public ResponseEntity<ApiResponse<List<Address>>> getMyAddresses() {
        return ResponseEntity.ok(ApiResponse.success(addressService.getMyAddresses(SecurityUtils.getCurrentUserId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Address>> add(@Valid @RequestBody AddressRequest request) {
        Address address = addressService.add(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Address added", address));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Address>> update(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        Address address = addressService.update(SecurityUtils.getCurrentUserId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated", address));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        addressService.delete(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Address removed", null));
    }
}
