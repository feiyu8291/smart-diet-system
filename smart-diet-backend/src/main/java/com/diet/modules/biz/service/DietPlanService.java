package com.diet.modules.biz.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.DietPlanMapper;
import com.diet.modules.biz.model.dto.DietStartPlanDTO;
import com.diet.modules.biz.model.dto.DietWeightRecordDTO;
import com.diet.modules.biz.model.entity.DietFamilyPlanProgress;
import com.diet.modules.biz.model.entity.DietPlan;
import com.diet.modules.biz.model.entity.DietUserHealthProfile;
import com.diet.modules.biz.model.entity.DietWeightRecord;
import com.diet.modules.biz.model.vo.DietPlanProgressVO;
import com.diet.modules.biz.model.vo.DietPlanVO;
import com.diet.modules.biz.model.vo.DietWeightRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 计划进度与体重分析业务服务类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DietPlanService extends ServiceImpl<DietPlanMapper, DietPlan> {

    private final DietFamilyPlanProgressService familyPlanProgressService;
    private final DietWeightRecordService weightRecordService;
    private final DietUserHealthProfileService userHealthProfileService;

    /**
     * 获取全量计划模板列表
     */
    public List<DietPlanVO> getTemplates() {
        List<DietPlan> list = this.lambdaQuery()
                .list();
        return list.stream().map(entity -> {
            DietPlanVO vo = new DietPlanVO();
            BeanUtil.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 根据家庭组 ID 查询当前处于活动（进行中）状态的配餐天数进度及计划模板
     */
    public DietPlanProgressVO getCurrentProgress(Long groupId) {
        DietPlanProgressVO vo = new DietPlanProgressVO();

        DietFamilyPlanProgress progress = familyPlanProgressService.lambdaQuery()
                .eq(DietFamilyPlanProgress::getGroupId, groupId)
                .eq(DietFamilyPlanProgress::getProgressStatus, 1)
                .one();

        if (Objects.nonNull(progress)) {
            vo.setHasActivePlan(true);
            vo.setProgress(progress);

            DietPlan plan = this.baseMapper.selectById(progress.getPlanId());
            vo.setTemplate(plan);
        } else {
            vo.setHasActivePlan(false);
        }
        return vo;
    }

    /**
     * 开启新配餐健康计划 (如果当前已有进行中的计划，将其标记为废弃(3))
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean startPlan(DietStartPlanDTO dto) {
        if (Objects.isNull(dto) || Objects.isNull(dto.getGroupId()) || Objects.isNull(dto.getPlanId())) {
            return false;
        }

        // 1. 将现存活跃的计划置为废弃
        DietFamilyPlanProgress active = familyPlanProgressService.lambdaQuery()
                .eq(DietFamilyPlanProgress::getGroupId, dto.getGroupId())
                .eq(DietFamilyPlanProgress::getProgressStatus, 1)
                .one();
        if (Objects.nonNull(active)) {
            active.setProgressStatus(3);
            active.setUpdateTime(LocalDateTime.now());
            familyPlanProgressService.updateById(active);
        }

        // 2. 插入新的一条进度记录
        DietFamilyPlanProgress newProgress = new DietFamilyPlanProgress();
        newProgress.setGroupId(dto.getGroupId());
        newProgress.setPlanId(dto.getPlanId());
        newProgress.setStartDate(LocalDate.now());
        newProgress.setCurrentDay(1);
        newProgress.setProgressStatus(1);
        newProgress.setDelFlag(0);

        return familyPlanProgressService.save(newProgress);
    }

    /**
     * 记录成员体重并同步更新健康档案
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean recordWeight(DietWeightRecordDTO dto) {
        if (Objects.isNull(dto) || Objects.isNull(dto.getProfileId()) || Objects.isNull(dto.getRecordWeight())) {
            return false;
        }

        DietWeightRecord record = new DietWeightRecord();
        record.setProfileId(dto.getProfileId());
        record.setRecordWeight(dto.getRecordWeight());
        record.setRecordDate(LocalDate.now());
        record.setDelFlag(0);

        boolean inserted = weightRecordService.save(record);
        if (!inserted) {
            return false;
        }

        // 同步修改成员健康档案的当前体重项
        DietUserHealthProfile profile = userHealthProfileService.getById(dto.getProfileId());
        if (Objects.nonNull(profile)) {
            profile.setMemberWeight(dto.getRecordWeight());
            userHealthProfileService.saveOrUpdateProfile(profile);
        }

        return true;
    }

    /**
     * 获取成员体重变迁历史记录
     */
    public List<DietWeightRecordVO> getWeightHistory(Long profileId) {
        List<DietWeightRecord> list = weightRecordService.lambdaQuery()
                .eq(DietWeightRecord::getProfileId, profileId)
                .orderByAsc(DietWeightRecord::getRecordDate)
                .list();
        return list.stream().map(entity -> {
            DietWeightRecordVO vo = new DietWeightRecordVO();
            BeanUtil.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}

