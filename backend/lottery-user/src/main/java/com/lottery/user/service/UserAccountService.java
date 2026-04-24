package com.lottery.user.service;

import com.lottery.user.domain.entity.UserAccount;
import com.lottery.user.model.request.LoginRequest;
import com.lottery.user.model.request.RegisterRequest;
import com.lottery.user.model.response.AuthResponse;
import com.lottery.user.model.response.UserProfileResponse;
import com.lottery.user.security.AuthenticatedUser;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public interface UserAccountService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse exchangeKeycloakToken(Jwt jwt);

    void logout();

    UserProfileResponse currentUser();

    Optional<AuthenticatedUser> findAuthenticatedUser(Long userId);

    AuthenticatedUser loadAuthenticatedUserByUsername(String username);

    Optional<UserAccount> findById(Long userId);
}
