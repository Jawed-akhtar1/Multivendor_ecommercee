package com.multivendor.ecommerce.service;

import com.multivendor.ecommerce.dto.request.LoginRequest;
import com.multivendor.ecommerce.dto.request.RegisterRequest;
import com.multivendor.ecommerce.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
