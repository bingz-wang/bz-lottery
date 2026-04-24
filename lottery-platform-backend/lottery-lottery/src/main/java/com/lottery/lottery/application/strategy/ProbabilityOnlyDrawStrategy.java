package com.lottery.lottery.application.strategy;

import com.lottery.common.exception.BusinessException;
import com.lottery.lottery.domain.entity.Prize;
import org.springframework.stereotype.Component;

@Component
public class ProbabilityOnlyDrawStrategy implements LotteryDrawStrategy {

    @Override
    public DrawStrategyType getType() {
        return DrawStrategyType.PROBABILITY_ONLY;
    }

    @Override
    public DrawStrategyResult selectPrize(DrawStrategyContext context) {
        Prize prize = WeightedPrizeSelector.select(context.availablePrizes());
        if (prize == null) {
            throw new BusinessException("Prize probability configuration is invalid");
        }
        return new DrawStrategyResult(prize, getType(), null, null);
    }
}
