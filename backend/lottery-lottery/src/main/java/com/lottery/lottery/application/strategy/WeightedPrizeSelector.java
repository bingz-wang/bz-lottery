package com.lottery.lottery.application.strategy;

import com.lottery.lottery.domain.entity.Prize;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

final class WeightedPrizeSelector {

    private WeightedPrizeSelector() {
    }

    static Prize select(List<Prize> prizes) {
        BigDecimal totalProbability = prizes.stream()
                .map(Prize::getProbability)
                .filter(probability -> probability != null && probability.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalProbability.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        double randomValue = ThreadLocalRandom.current().nextDouble(totalProbability.doubleValue());
        double cumulativeProbability = 0D;
        Prize lastPrize = null;

        for (Prize prize : prizes) {
            if (prize.getProbability() == null || prize.getProbability().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            cumulativeProbability += prize.getProbability().doubleValue();
            lastPrize = prize;
            if (randomValue < cumulativeProbability) {
                return prize;
            }
        }

        return lastPrize;
    }
}
