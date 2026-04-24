package com.lottery.award.application.dto;

import java.math.BigDecimal;

public record PrizeView(
        Long id,
        String prizeCode,
        String prizeName,
        String prizeLevel,
        Integer prizeLevelSort,
        BigDecimal probability,
        Integer totalStock,
        Integer availableStock,
        String prizeDesc,
        String prizeImage,
        Integer sort
) {
}
