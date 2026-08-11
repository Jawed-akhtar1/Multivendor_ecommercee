package com.multivendor.ecommerce.service.impl;

import com.multivendor.ecommerce.service.CommissionService;
import com.multivendor.ecommerce.dto.request.ProductRequest;
import com.multivendor.ecommerce.dto.request.VendorRegisterRequest;
import com.multivendor.ecommerce.dto.response.ProductResponse;
import com.multivendor.ecommerce.dto.response.VendorResponse;
import com.multivendor.ecommerce.entity.Category;
import com.multivendor.ecommerce.entity.Product;
import com.multivendor.ecommerce.entity.User;
import com.multivendor.ecommerce.entity.Vendor;
import com.multivendor.ecommerce.exception.BadRequestException;
import com.multivendor.ecommerce.exception.ForbiddenException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.CategoryRepository;
import com.multivendor.ecommerce.repository.ProductRepository;
import com.multivendor.ecommerce.repository.UserRepository;
import com.multivendor.ecommerce.repository.VendorRepository;
import com.multivendor.ecommerce.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CommissionService commissionService;
    private final ProductMapper productMapper;

    @Override
    public VendorResponse registerVendor(Long userId, VendorRegisterRequest request) {
        if (vendorRepository.existsByUserId(userId)) {
            throw new BadRequestException("A vendor store already exists for this account");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vendor vendor = Vendor.builder()
                .user(user)
                .storeName(request.getStoreName())
                .storeDescription(request.getStoreDescription())
                .gstNumber(request.getGstNumber())
                .logoUrl(request.getLogoUrl())
                .bankAccountName(request.getBankAccountName())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankIfsc(request.getBankIfsc())
                .bankName(request.getBankName())
                .approved(false) // requires Admin approval (Admin Module -> Vendor Approval)
                .build();

        vendor = vendorRepository.save(vendor);
        return toVendorResponse(vendor);
    }

    @Override
    public VendorResponse getMyStore(Long userId) {
        Vendor vendor = getVendorOrThrow(userId);
        return toVendorResponse(vendor);
    }

    @Override
    public VendorResponse updateMyStore(Long userId, VendorRegisterRequest request) {
        Vendor vendor = getVendorOrThrow(userId);
        vendor.setStoreName(request.getStoreName());
        vendor.setStoreDescription(request.getStoreDescription());
        vendor.setGstNumber(request.getGstNumber());
        vendor.setLogoUrl(request.getLogoUrl());
        vendor.setBankAccountName(request.getBankAccountName());
        vendor.setBankAccountNumber(request.getBankAccountNumber());
        vendor.setBankIfsc(request.getBankIfsc());
        vendor.setBankName(request.getBankName());
        vendor = vendorRepository.save(vendor);
        return toVendorResponse(vendor);
    }

    @Override
    public ProductResponse addProduct(Long userId, ProductRequest request) {
        Vendor vendor = getApprovedVendorOrThrow(userId);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        String sku = (request.getSku() == null || request.getSku().isBlank())
                ? generateSku()
                : request.getSku();

        if (productRepository.existsBySku(sku)) {
            throw new BadRequestException("A product with this SKU already exists");
        }

        Product product = Product.builder()
                .vendor(vendor)
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .sku(sku)
                .stock(request.getStock())
                .imageUrl(request.getImageUrl())
                .active(true)
                .build();

        product = productRepository.save(product);
        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse updateProduct(Long userId, Long productId, ProductRequest request) {
        Vendor vendor = getApprovedVendorOrThrow(userId);
        Product product = getOwnedProductOrThrow(vendor.getId(), productId);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(category);
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());

        product = productRepository.save(product);
        return productMapper.toResponse(product);
    }

    @Override
    public void deleteProduct(Long userId, Long productId) {
        Vendor vendor = getApprovedVendorOrThrow(userId);
        Product product = getOwnedProductOrThrow(vendor.getId(), productId);
        // Soft delete so historical orders keep a valid product reference.
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    public ProductResponse updateStock(Long userId, Long productId, Integer stock) {
        Vendor vendor = getApprovedVendorOrThrow(userId);
        Product product = getOwnedProductOrThrow(vendor.getId(), productId);
        product.setStock(stock);
        product = productRepository.save(product);
        return productMapper.toResponse(product);
    }

    @Override
    public Page<ProductResponse> getMyProducts(Long userId, Pageable pageable) {
        Vendor vendor = getVendorOrThrow(userId);
        return productRepository.findByVendorId(vendor.getId(), pageable).map(productMapper::toResponse);
    }

    private Vendor getVendorOrThrow(Long userId) {
        return vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No vendor store found for this account. Register a store first."));
    }

    private Vendor getApprovedVendorOrThrow(Long userId) {
        Vendor vendor = getVendorOrThrow(userId);
        if (!vendor.isApproved()) {
            throw new ForbiddenException("Your vendor account is pending admin approval");
        }
        return vendor;
    }

    private Product getOwnedProductOrThrow(Long vendorId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (!product.getVendor().getId().equals(vendorId)) {
            throw new ForbiddenException("You do not have permission to modify this product");
        }
        return product;
    }

    private String generateSku() {
        return "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private VendorResponse toVendorResponse(Vendor vendor) {
        CommissionService.CommissionBreakdown breakdown = commissionService.getBreakdown(vendor);
        return VendorResponse.builder()
                .id(vendor.getId())
                .userId(vendor.getUser().getId())
                .storeName(vendor.getStoreName())
                .storeDescription(vendor.getStoreDescription())
                .gstNumber(vendor.getGstNumber())
                .logoUrl(vendor.getLogoUrl())
                .approved(vendor.isApproved())
                .ownerName(vendor.getUser().getName())
                .ownerEmail(vendor.getUser().getEmail())
                .baseCommissionRate(breakdown.baseRate())
                .baseCommissionRateIsCustom(breakdown.baseRateIsCustom())
                .averageRating(breakdown.averageRating())
                .reviewCount(breakdown.reviewCount())
                .ratingMultiplier(breakdown.ratingMultiplier())
                .effectiveCommissionRate(breakdown.effectiveRate())
                .bankAccountName(vendor.getBankAccountName())
                .bankAccountNumber(vendor.getBankAccountNumber())
                .bankIfsc(vendor.getBankIfsc())
                .bankName(vendor.getBankName())
                .build();
    }

}
