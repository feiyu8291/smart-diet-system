package com.diet.modules.biz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.DietPlanMapper;
import com.diet.modules.biz.mapper.DietFamilyPlanProgressMapper;
import com.diet.modules.biz.mapper.DietWeightRecordMapper;
import com.diet.modules.biz.model.dto.DietStartPlanDTO;
import com.diet.modules.biz.model.dto.DietWeightRecordDTO;
import com.diet.modules.biz.model.entity.DietPlan;
import com.diet.modules.biz.model.entity.DietFamilyPlanProgress;
import com.diet.modules.biz.model.entity.DietUserHealthProfile;
import com.diet.modules.biz.model.entity.DietWeightRecord;
import com.diet.modules.biz.model.vo.DietPlanProgressVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 计划进度与体重分析业务服务类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DietPlanService extends ServiceImpl<DietWeightRecordMapper, DietWeightRecord> {

    private final DietPlanMapper dietPlanMapper;
    private final DietFamilyPlanProgressMapper familyPlanProgressMapper;
    private final DietWeightRecordMapper weightRecordMapper;
    private final DietUserHealthProfileService userHealthProfileService;

    /**
     * 获取全量计划模板列表
     */
    public List<DietPlan> getTemplates() {
        LambdaQueryWrapper<DietPlan> query = new LambdaQueryWrapper<>();
        query.eq(DietPlan::getDelFlag, 0);
        return dietPlanMapper.selectList(query);
    }

    /**
     * 根据家庭组 ID 查询当前处于活动（进行中）状态的配餐天数进度及计划模板
     */
    public DietPlanProgressVO getCurrentProgress(Long groupId) {
        DietPlanProgressVO vo = new DietPlanProgressVO();

        LambdaQueryWrapper<DietFamilyPlanProgress> progressQuery = new LambdaQueryWrapper<>();
        progressQuery.eq(DietFamilyPlanProgress::getGroupId, groupId)
                .eq(DietFamilyPlanProgress::getProgressStatus, 1)
                .eq(DietFamilyPlanProgress::getDelFlag, 0);
        DietFamilyPlanProgress progress = familyPlanProgressMapper.selectOne(progressQuery);

        if (progress != null) {
            vo.setHasActivePlan(true);
            vo.setProgress(progress);

            DietPlan plan = dietPlanMapper.selectById(progress.getPlanId());
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
        if (dto == null || dto.getGroupId() == null || dto.getPlanId() == null) {
            return false;
        }

        // 1. 将现存活跃的计划置为废弃
        LambdaQueryWrapper<DietFamilyPlanProgress> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(DietFamilyPlanProgress::getGroupId, dto.getGroupId())
                .eq(DietFamilyPlanProgress::getProgressStatus, 1);
        DietFamilyPlanProgress active = familyPlanProgressMapper.selectOne(updateWrapper);
        if (active != null) {
            active.setProgressStatus(3);
            active.setUpdateTime(LocalDateTime.now());
            familyPlanProgressMapper.updateById(active);
        }

        // 2. 插入新的一条进度记录
        DietFamilyPlanProgress newProgress = new DietFamilyPlanProgress();
        newProgress.setGroupId(dto.getGroupId());
        newProgress.setPlanId(dto.getPlanId());
        newProgress.setStartDate(LocalDate.now());
        newProgress.setCurrentDay(1);
        newProgress.setProgressStatus(1);
        newProgress.setDelFlag(0);

        return familyPlanProgressMapper.insert(newProgress) > 0;
    }

    /**
     * 记录成员体重并同步更新健康档案
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean recordWeight(DietWeightRecordDTO dto) {
        if (dto == null || dto.getProfileId() == null || dto.getRecordWeight() == null) {
            return false;
        }

        DietWeightRecord record = new DietWeightRecord();
        record.setProfileId(dto.getProfileId());
        record.setRecordWeight(dto.getRecordWeight());
        record.setRecordDate(LocalDate.now());
        record.setDelFlag(0);

        int inserted = weightRecordMapper.insert(record);
        if (inserted <= 0) return false;

        // 同步修改成员健康档案的当前体重项
        DietUserHealthProfile profile = userHealthProfileService.getById(dto.getProfileId());
        if (profile != null) {
            profile.setMemberWeight(dto.getRecordWeight());
            userHealthProfileService.saveOrUpdateProfile(profile);
        }

        return true;
    }

    /**
     * 获取成员体重变迁历史记录
     */
    public List<DietWeightRecord> getWeightHistory(Long profileId) {
        LambdaQueryWrapper<DietWeightRecord> query = new LambdaQueryWrapper<>();
        query.eq(DietWeightRecord::getProfileId, profileId)
                .eq(DietWeightRecord::getDelFlag, 0)
                .orderByAsc(DietWeightRecord::getRecordDate);
        return weightRecordMapper.selectList(query);
    }
}
