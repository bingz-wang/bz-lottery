package com.lottery.award.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.award.application.dto.PrizeView;
import com.lottery.award.domain.entity.Prize;
import com.lottery.award.infrastructure.mapper.PrizeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrizeQueryService {

    private final PrizeMapper prizeMapper;

    public PrizeQueryService(PrizeMapper prizeMapper) {
        this.prizeMapper = prizeMapper;
    }

    public List<PrizeView> listAvailablePrizes() {
        return prizeMapper.selectList(new LambdaQueryWrapper<Prize>()
                        .eq(Prize::getDeleted, false)
                        .eq(Prize::getStatus, 1)
                        .gt(Prize::getAvailableStock, 0)
                        .orderByAsc(Prize::getSort)
                        .orderByAsc(Prize::getPrizeLevelSort)
                        .orderByAsc(Prize::getId))
                .stream()
                .map(prize -> new PrizeView(
                        prize.getId(),
                        prize.getPrizeCode(),
                        prize.getPrizeName(),
                        prize.getPrizeLevel(),
                        prize.getPrizeLevelSort(),
                        prize.getProbability(),
                        prize.getTotalStock(),
                        prize.getAvailableStock(),
                        prize.getPrizeDesc(),
                        prize.getPrizeImage(),
                        prize.getSort()
                ))
                .toList();
    }
}
