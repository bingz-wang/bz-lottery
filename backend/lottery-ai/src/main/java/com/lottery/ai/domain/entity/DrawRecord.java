package com.lottery.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("lottery_platform.lottery_draw_record")
public class DrawRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long prizeId;
    private String prizeName;
    private String prizeLevel;
    private Integer prizeLevelSort;
    private BigDecimal hitProbability;
    private Integer drawStatus;
    private String drawRemark;
    private LocalDateTime createdAt;
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPrizeId() {
        return prizeId;
    }

    public void setPrizeId(Long prizeId) {
        this.prizeId = prizeId;
    }

    public String getPrizeName() {
        return prizeName;
    }

    public void setPrizeName(String prizeName) {
        this.prizeName = prizeName;
    }

    public String getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(String prizeLevel) {
        this.prizeLevel = prizeLevel;
    }

    public Integer getPrizeLevelSort() {
        return prizeLevelSort;
    }

    public void setPrizeLevelSort(Integer prizeLevelSort) {
        this.prizeLevelSort = prizeLevelSort;
    }

    public BigDecimal getHitProbability() {
        return hitProbability;
    }

    public void setHitProbability(BigDecimal hitProbability) {
        this.hitProbability = hitProbability;
    }

    public Integer getDrawStatus() {
        return drawStatus;
    }

    public void setDrawStatus(Integer drawStatus) {
        this.drawStatus = drawStatus;
    }

    public String getDrawRemark() {
        return drawRemark;
    }

    public void setDrawRemark(String drawRemark) {
        this.drawRemark = drawRemark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
