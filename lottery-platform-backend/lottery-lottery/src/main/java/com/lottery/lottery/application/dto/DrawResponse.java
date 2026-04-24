package com.lottery.lottery.application.dto;

import java.math.BigDecimal;

public record DrawResponse(
        String recordNo,
        Long userId,
        Integer drawStatus,
        Long prizeId,
        String prizeCode,
        String prizeName,
        String prizeLevel,
        BigDecimal hitProbability,
        String drawRemark
) {
}
