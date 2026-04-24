package com.lottery.user.model.response;

import java.util.List;

public record UserProfileResponse(
        Long id,
        String username,
        String nickname,
        String email,
        String mobile,
        String authSource,
        List<String> roles,
        List<MenuItemResponse> menus
) {
}
