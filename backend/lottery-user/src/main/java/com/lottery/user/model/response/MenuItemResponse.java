package com.lottery.user.model.response;

public record MenuItemResponse(
        String menuCode,
        String menuName,
        String path,
        String routeName,
        String component,
        String icon
) {
}
