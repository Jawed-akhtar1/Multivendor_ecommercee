package com.multivendor.ecommerce.service.impl;

import com.multivendor.ecommerce.dto.request.AddressRequest;
import com.multivendor.ecommerce.entity.Address;
import com.multivendor.ecommerce.entity.User;
import com.multivendor.ecommerce.exception.ForbiddenException;
import com.multivendor.ecommerce.exception.ResourceNotFoundException;
import com.multivendor.ecommerce.repository.AddressRepository;
import com.multivendor.ecommerce.repository.UserRepository;
import com.multivendor.ecommerce.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public Address add(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .addressLine(request.getAddressLine())
                .landmark(request.getLandmark())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry() == null || request.getCountry().isBlank() ? "India" : request.getCountry())
                .isDefault(request.isDefault())
                .build();

        return addressRepository.save(address);
    }

    @Override
    public Address update(Long userId, Long addressId, AddressRequest request) {
        Address address = getOwnedOrThrow(userId, addressId);
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
        address.setDefault(request.isDefault());
        return addressRepository.save(address);
    }

    @Override
    public void delete(Long userId, Long addressId) {
        Address address = getOwnedOrThrow(userId, addressId);
        addressRepository.delete(address);
    }

    @Override
    public List<Address> getMyAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
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
}
