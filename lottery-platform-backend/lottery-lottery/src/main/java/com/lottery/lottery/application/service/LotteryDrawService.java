package com.lottery.lottery.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lottery.common.exception.BusinessException;
import com.lottery.lottery.application.dto.DrawRecordView;
import com.lottery.lottery.application.dto.DrawRequest;
import com.lottery.lottery.application.dto.DrawResponse;
import com.lottery.lottery.application.strategy.*;
import com.lottery.lottery.domain.entity.DrawRecord;
import com.lottery.lottery.domain.entity.Prize;
import com.lottery.lottery.infrastructure.mapper.DrawRecordMapper;
import com.lottery.lottery.infrastructure.mapper.PrizeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class LotteryDrawService {

    private static final DateTimeFormatter RECORD_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final int MAX_GUARANTEE_HISTORY = 50;

    private final PrizeMapper prizeMapper;
    private final DrawRecordMapper drawRecordMapper;
    private final LotteryStrategyResolver lotteryStrategyResolver;

    public LotteryDrawService(
            PrizeMapper prizeMapper,
            DrawRecordMapper drawRecordMapper,
            LotteryStrategyResolver lotteryStrategyResolver
    ) {
        this.prizeMapper = prizeMapper;
        this.drawRecordMapper = drawRecordMapper;
        this.lotteryStrategyResolver = lotteryStrategyResolver;
    }

    public List<DrawRecordView> listRecords(Long userId, int limit) {
        int safeLimit = Math.clamp(limit, 1, 100);
        LambdaQueryWrapper<DrawRecord> queryWrapper = new LambdaQueryWrapper<DrawRecord>()
                .eq(DrawRecord::getDeleted, false)
                .orderByDesc(DrawRecord::getCreatedAt)
                .last("limit " + safeLimit);

        if (userId != null) {
            queryWrapper.eq(DrawRecord::getUserId, userId);
        }

        return drawRecordMapper.selectList(queryWrapper)
                .stream()
                .map(record -> new DrawRecordView(
                        record.getRecordNo(),
                        record.getUserId(),
                        record.getDrawStatus(),
                        record.getPrizeId(),
                        record.getPrizeCode(),
                        record.getPrizeName(),
                        record.getPrizeLevel(),
                        record.getHitProbability(),
                        record.getDrawRemark(),
                        record.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public DrawResponse draw(DrawRequest request) {
        if (request == null || request.userId() == null || request.userId() <= 0) {
            throw new BusinessException("userId must be a positive number");
        }

        String requestNo = isBlank(request.requestNo()) ? generateRequestNo(request.userId()) : request.requestNo();
        DrawRecord existingRecord = drawRecordMapper.selectOne(new LambdaQueryWrapper<DrawRecord>()
                .eq(DrawRecord::getDeleted, false)
                .eq(DrawRecord::getUserId, request.userId())
                .eq(DrawRecord::getRequestNo, requestNo)
                .last("limit 1"));
        if (existingRecord != null) {
            return toResponse(existingRecord);
        }

        List<Prize> availablePrizes = prizeMapper.selectList(new LambdaQueryWrapper<Prize>()
                .eq(Prize::getDeleted, false)
                .eq(Prize::getStatus, 1)
                .gt(Prize::getAvailableStock, 0)
                .orderByAsc(Prize::getSort)
                .orderByAsc(Prize::getPrizeLevelSort)
                .orderByAsc(Prize::getId));

        if (availablePrizes.isEmpty()) {
            throw new BusinessException("No available prizes for draw");
        }

        List<DrawRecord> recentRecords = listRecentRecords(request.userId(), MAX_GUARANTEE_HISTORY);
        DrawStrategyContext context = new DrawStrategyContext(
                request.userId(),
                availablePrizes,
                recentRecords,
                buildHistorySnapshot(recentRecords)
        );
        DrawStrategyResult selectionResult = lotteryStrategyResolver.resolve().selectPrize(context);
        Prize selectedPrize = selectionResult.prize();

        int updatedRows = prizeMapper.decreaseStock(selectedPrize.getId());
        if (updatedRows == 0) {
            throw new BusinessException("Prize stock changed, please try again");
        }

        DrawRecord drawRecord = new DrawRecord();
        int drawStatus = isHighLevelPrize(selectedPrize) ? 2 : 1;
        drawRecord.setRecordNo(generateRecordNo(request.userId()));
        drawRecord.setUserId(request.userId());
        drawRecord.setPrizeId(selectedPrize.getId());
        drawRecord.setPrizeCode(selectedPrize.getPrizeCode());
        drawRecord.setPrizeName(selectedPrize.getPrizeName());
        drawRecord.setPrizeLevel(selectedPrize.getPrizeLevel());
        drawRecord.setPrizeLevelSort(selectedPrize.getPrizeLevelSort());
        drawRecord.setHitProbability(selectedPrize.getProbability());
        drawRecord.setDrawStatus(drawStatus);
        drawRecord.setDrawRemark(buildRemark(selectedPrize, selectionResult));
        drawRecord.setRequestNo(requestNo);
        drawRecord.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        drawRecord.setCreatedAt(LocalDateTime.now());
        drawRecord.setUpdatedAt(LocalDateTime.now());
        drawRecord.setDeleted(false);

        drawRecordMapper.insert(drawRecord);
        return toResponse(drawRecord);
    }

    private List<DrawRecord> listRecentRecords(Long userId, int limit) {
        return drawRecordMapper.selectList(new LambdaQueryWrapper<DrawRecord>()
                .eq(DrawRecord::getDeleted, false)
                .eq(DrawRecord::getUserId, userId)
                .orderByDesc(DrawRecord::getCreatedAt)
                .last("limit " + limit));
    }

    private DrawHistorySnapshot buildHistorySnapshot(List<DrawRecord> recentRecords) {
        return new DrawHistorySnapshot(
                countMissesUntilHit(recentRecords, GuaranteeTier.LUCKY),
                countMissesUntilHit(recentRecords, GuaranteeTier.MID_TIER),
                countMissesUntilHit(recentRecords, GuaranteeTier.SPECIAL)
        );
    }

    private int countMissesUntilHit(List<DrawRecord> recentRecords, GuaranteeTier tier) {
        int missCount = 0;
        int ceiling = Math.max(tier.threshold() - 1, 0);

        for (DrawRecord record : recentRecords) {
            if (tier.matches(record.getPrizeLevelSort())) {
                return missCount;
            }

            missCount++;
            if (missCount >= ceiling) {
                return ceiling;
            }
        }

        return missCount;
    }

    private DrawResponse toResponse(DrawRecord drawRecord) {
        return new DrawResponse(
                drawRecord.getRecordNo(),
                drawRecord.getUserId(),
                drawRecord.getDrawStatus(),
                drawRecord.getPrizeId(),
                drawRecord.getPrizeCode(),
                drawRecord.getPrizeName(),
                drawRecord.getPrizeLevel(),
                drawRecord.getHitProbability(),
                drawRecord.getDrawRemark()
        );
    }

    private String buildRemark(Prize selectedPrize, DrawStrategyResult selectionResult) {
        String baseRemark;
        if (selectionResult.isDowngradedGuarantee()) {
            baseRemark = "%s guarantee downgraded to %s due to stock exhaustion".formatted(
                    selectionResult.targetGuaranteeTier().label(),
                    selectionResult.actualGuaranteeTier().label()
            );
        } else if (selectionResult.isGuaranteeHit()) {
            baseRemark = "Guaranteed %s hit after %d draws".formatted(
                    selectionResult.actualGuaranteeTier().label(),
                    selectionResult.targetGuaranteeTier().threshold()
            );
        } else if (selectionResult.strategyType() == com.lottery.lottery.application.strategy.DrawStrategyType.PROBABILITY_ONLY) {
            baseRemark = "Probability strategy draw success";
        } else {
            baseRemark = "Guarantee ladder probability draw success";
        }

        if (isHighLevelPrize(selectedPrize)) {
            return baseRemark + ", pending manual review";
        }
        return baseRemark;
    }

    private boolean isHighLevelPrize(Prize selectedPrize) {
        return selectedPrize.getPrizeLevelSort() != null && selectedPrize.getPrizeLevelSort() <= 2;
    }

    private String generateRecordNo(Long userId) {
        return "DRAW" + LocalDateTime.now().format(RECORD_NO_FORMATTER) + userId;
    }

    private String generateRequestNo(Long userId) {
        return "REQ" + LocalDateTime.now().format(RECORD_NO_FORMATTER) + userId;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
