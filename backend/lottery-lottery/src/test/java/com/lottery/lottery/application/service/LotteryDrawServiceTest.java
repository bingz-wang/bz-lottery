package com.lottery.lottery.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.common.exception.BusinessException;
import com.lottery.lottery.application.dto.DrawRequest;
import com.lottery.lottery.application.dto.DrawResponse;
import com.lottery.lottery.application.strategy.*;
import com.lottery.lottery.domain.entity.DrawRecord;
import com.lottery.lottery.domain.entity.Prize;
import com.lottery.lottery.infrastructure.mapper.DrawRecordMapper;
import com.lottery.lottery.infrastructure.mapper.PrizeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LotteryDrawServiceTest {

    @Mock
    private PrizeMapper prizeMapper;

    @Mock
    private DrawRecordMapper drawRecordMapper;

    @Mock
    private LotteryStrategyResolver lotteryStrategyResolver;

    @InjectMocks
    private LotteryDrawService lotteryDrawService;

    @Test
    void shouldWriteGuaranteeDowngradeRemarkForHighLevelPrize() {
        Prize prize = prize(2L, "First Prize", 2, "iPad Air", new BigDecimal("0.008"));
        LotteryDrawStrategy strategy = mock(LotteryDrawStrategy.class);
        DrawStrategyResult result = new DrawStrategyResult(
                prize,
                DrawStrategyType.GUARANTEE_LADDER,
                GuaranteeTier.SPECIAL,
                GuaranteeTier.MID_TIER
        );

        when(drawRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(drawRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(), List.of());
        when(prizeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(prize));
        when(lotteryStrategyResolver.resolve()).thenReturn(strategy);
        when(strategy.selectPrize(any())).thenReturn(result);
        when(prizeMapper.decreaseStock(2L)).thenReturn(1);
        when(drawRecordMapper.insert(any(DrawRecord.class))).thenReturn(1);

        DrawResponse response = lotteryDrawService.draw(new DrawRequest(10001L, "REQ-TEST-1"));

        ArgumentCaptor<DrawRecord> captor = ArgumentCaptor.forClass(DrawRecord.class);
        verify(drawRecordMapper).insert(captor.capture());
        assertEquals("First Prize", response.prizeLevel());
        assertEquals("special-tier guarantee downgraded to 1-3 tier due to stock exhaustion, pending manual review", captor.getValue().getDrawRemark());
        assertEquals(2, captor.getValue().getDrawStatus());
    }

    @Test
    void shouldFailWhenStockUpdateReturnsZero() {
        Prize prize = prize(5L, "Lucky Prize", 5, "Coffee Gift Box", new BigDecimal("0.18"));
        LotteryDrawStrategy strategy = mock(LotteryDrawStrategy.class);
        DrawStrategyResult result = new DrawStrategyResult(prize, DrawStrategyType.PROBABILITY_ONLY, null, null);

        when(drawRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(drawRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(prizeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(prize));
        when(lotteryStrategyResolver.resolve()).thenReturn(strategy);
        when(strategy.selectPrize(any())).thenReturn(result);
        when(prizeMapper.decreaseStock(5L)).thenReturn(0);

        BusinessException error = assertThrows(BusinessException.class, () -> lotteryDrawService.draw(new DrawRequest(10001L, "REQ-TEST-2")));
        assertEquals("Prize stock changed, please try again", error.getMessage());
    }

    private Prize prize(Long id, String level, int levelSort, String name, BigDecimal probability) {
        Prize prize = new Prize();
        prize.setId(id);
        prize.setPrizeLevel(level);
        prize.setPrizeLevelSort(levelSort);
        prize.setPrizeName(name);
        prize.setPrizeCode("CODE-" + id);
        prize.setProbability(probability);
        prize.setAvailableStock(10);
        prize.setStatus(1);
        return prize;
    }
}
