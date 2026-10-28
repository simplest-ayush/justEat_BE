package com.justeat.backend.service;

import com.justeat.backend.dto.request.*;
import com.justeat.backend.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);

    AuthResponse refresh(RefreshRequest request);

    void logout(RefreshRequest request);

    void forgotPassword(ForgotPasswordRequest request);
    
    void resetPassword(ResetPasswordRequest request);
}
