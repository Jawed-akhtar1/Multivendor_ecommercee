package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.request.AddressRequest;
import com.multivendor.ecommerce.dto.response.AddressResponse;
import com.multivendor.ecommerce.entity.Address;

import java.util.List;

public interface AddressService {

    AddressResponse add(Long userId, AddressRequest request);

    AddressResponse update(Long userId, Long addressId, AddressRequest request);

    void delete(Long userId, Long addressId);

    /** Marks this address as the user's default, un-defaulting any other address they have. */
    AddressResponse setDefault(Long userId, Long addressId);

    List<AddressResponse> getMyAddresses(Long userId);
    Address getOwnedOrThrow(Long userId, Long addressId);
}
