package com.lottery.lottery.application.strategy;

public interface LotteryDrawStrategy {

    DrawStrategyType getType();

    DrawStrategyResult selectPrize(DrawStrategyContext context);
}
