package com.devhub.devhub.auth;

import com.devhub.devhub.auth.dto.AuthResponse;
import com.devhub.devhub.auth.dto.LoginRequest;
import com.devhub.devhub.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout(String refreshToken);

}