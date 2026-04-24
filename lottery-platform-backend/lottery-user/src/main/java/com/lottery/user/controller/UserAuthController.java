package com.lottery.user.controller;

import com.lottery.common.exception.BusinessException;
import com.lottery.common.response.ApiResponse;
import com.lottery.common.response.CommonErrorCode;
import com.lottery.user.model.request.LoginRequest;
import com.lottery.user.model.request.RegisterRequest;
import com.lottery.user.model.response.AuthResponse;
import com.lottery.user.model.response.UserProfileResponse;
import com.lottery.user.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/user/auth")
public class UserAuthController {

    private final UserAccountService userAccountService;

    public UserAuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(userAccountService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(userAccountService.login(request));
    }

    @PostMapping("/keycloak/exchange")
    public ApiResponse<AuthResponse> exchangeKeycloakToken(Principal principal) {
        if (principal instanceof JwtAuthenticationToken authenticationToken) {
            Jwt jwt = authenticationToken.getToken();
            return ApiResponse.success(userAccountService.exchangeKeycloakToken(jwt));
        }
        throw new BusinessException(CommonErrorCode.UNAUTHORIZED.code(), "Keycloak JWT is required");
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        userAccountService.logout();
        return ApiResponse.success();
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> currentUser() {
        return ApiResponse.success(userAccountService.currentUser());
    }
}
