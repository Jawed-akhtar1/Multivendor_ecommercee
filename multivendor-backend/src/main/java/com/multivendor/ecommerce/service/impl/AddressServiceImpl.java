package com.multivendor.ecommerce.service.impl;

import com.multivendor.ecommerce.dto.request.AddressRequest;
import com.multivendor.ecommerce.dto.response.AddressResponse;
import com.multivendor.ecommerce.entity.Address;
import com.multivendor.ecommerce.entity.User;
import com.multivendor.ecommerce.exception.BadRequestException;
import com.multivendor.ecommerce.exception.ForbiddenException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.AddressRepository;
import com.multivendor.ecommerce.repository.UserRepository;
import com.multivendor.ecommerce.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private static final int MAX_ADDRESSES_PER_USER = 15;

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AddressResponse add(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long existingCount = addressRepository.countByUserId(userId);
        if (existingCount >= MAX_ADDRESSES_PER_USER) {
            throw new BadRequestException(
                    "You've reached the limit of " + MAX_ADDRESSES_PER_USER + " saved addresses — remove one before adding another.");
        }
        boolean isFirstAddress = existingCount == 0;
        boolean shouldBeDefault = request.isDefaultAddress() || isFirstAddress;

        if (shouldBeDefault) {
            clearExistingDefault(userId);
        }

        Address address = Address.builder()
                .user(user)
                .label(request.getLabel())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .addressLine(request.getAddressLine())
                .landmark(request.getLandmark())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry() == null || request.getCountry().isBlank() ? "India" : request.getCountry())
                .defaultAddress(shouldBeDefault)
                .build();

        return toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse update(Long userId, Long addressId, AddressRequest request) {
        Address address = getOwnedOrThrow(userId, addressId);

        if (request.isDefaultAddress() && !address.isDefaultAddress()) {
            clearExistingDefault(userId);
        }
       
        boolean nextDefault = address.isDefaultAddress() || request.isDefaultAddress();

        address.setLabel(request.getLabel());
        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setAddressLine(request.getAddressLine());
        address.setLandmark(request.getLandmark());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        if (request.getCountry() != null && !request.getCountry().isBlank()) {
            address.setCountry(request.getCountry());
        }
        address.setDefaultAddress(nextDefault);

        return toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long addressId) {
        Address address = getOwnedOrThrow(userId, addressId);
        boolean wasDefault = address.isDefaultAddress();

        addressRepository.delete(address);

        if (wasDefault) {
            addressRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.setDefaultAddress(true);
                        addressRepository.save(next);
                    });
        }
    }

    @Override
    @Transactional
    public AddressResponse setDefault(Long userId, Long addressId) {
        Address address = getOwnedOrThrow(userId, addressId);
        if (!address.isDefaultAddress()) {
            clearExistingDefault(userId);
            address.setDefaultAddress(true);
            address = addressRepository.save(address);
        }
        return toResponse(address);
    }

    @Override
    public List<AddressResponse> getMyAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Address getOwnedOrThrow(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new ForbiddenException("This address does not belong to you");
        }
        return address;
    }

    private void clearExistingDefault(Long userId) {
        addressRepository.findFirstByUserIdAndDefaultAddressTrue(userId).ifPresent(current -> {
            current.setDefaultAddress(false);
            addressRepository.save(current);
        });
    }

    private AddressResponse toResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .addressLine(address.getAddressLine())
                .landmark(address.getLandmark())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .country(address.getCountry())
                .defaultAddress(address.isDefaultAddress())
                .build();
    }
}
