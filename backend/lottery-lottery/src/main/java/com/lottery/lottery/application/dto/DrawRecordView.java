package com.lottery.lottery.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DrawRecordView(
        String recordNo,
        Long userId,
        Integer drawStatus,
        Long prizeId,
        String prizeCode,
        String prizeName,
        String prizeLevel,
        BigDecimal hitProbability,
        String drawRemark,
        LocalDateTime createdAt
) {
}
