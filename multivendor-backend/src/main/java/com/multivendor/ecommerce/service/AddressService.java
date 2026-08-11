package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.request.AddressRequest;
import com.multivendor.ecommerce.entity.Address;

import java.util.List;

public interface AddressService {
    Address add(Long userId, AddressRequest request);
    Address update(Long userId, Long addressId, AddressRequest request);
    void delete(Long userId, Long addressId);
    List<Address> getMyAddresses(Long userId);
    Address getOwnedOrThrow(Long userId, Long addressId);
}
