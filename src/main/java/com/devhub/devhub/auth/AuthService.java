package com.devhub.devhub.auth;

import com.devhub.devhub.auth.dto.LoginRequest;
import com.devhub.devhub.auth.dto.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);

    void login(LoginRequest request);

}