package com.lottery.user.model.response;

public record AuthResponse(
        String tokenName,
        String tokenValue,
        Long userId,
        UserProfileResponse profile
) {
}
