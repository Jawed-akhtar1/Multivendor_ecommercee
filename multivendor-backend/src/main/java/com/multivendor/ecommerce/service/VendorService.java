package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.request.ProductRequest;
import com.multivendor.ecommerce.dto.request.VendorRegisterRequest;
import com.multivendor.ecommerce.dto.response.ProductResponse;
import com.multivendor.ecommerce.dto.response.VendorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VendorService {

    VendorResponse registerVendor(Long userId, VendorRegisterRequest request);

    VendorResponse getMyStore(Long userId);

    VendorResponse updateMyStore(Long userId, VendorRegisterRequest request);

    ProductResponse addProduct(Long userId, ProductRequest request);

    ProductResponse updateProduct(Long userId, Long productId, ProductRequest request);

    void deleteProduct(Long userId, Long productId);

    ProductResponse updateStock(Long userId, Long productId, Integer stock);

    Page<ProductResponse> getMyProducts(Long userId, Pageable pageable);
}
