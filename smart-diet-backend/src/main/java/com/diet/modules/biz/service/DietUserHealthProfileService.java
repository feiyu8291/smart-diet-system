package com.diet.modules.biz.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.DietUserHealthProfileMapper;
import com.diet.modules.biz.model.dto.DietUserHealthProfileDTO;
import com.diet.modules.biz.model.entity.DietUserHealthProfile;
import com.diet.modules.biz.model.po.DietUserHealthProfileQueryPO;
import com.diet.modules.biz.model.vo.DietUserHealthProfileVO;
import com.diet.modules.biz.util.HealthMetricsUtil;
import com.diet.modules.common.entity.BaseDeleteDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 成员健康档案服务类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Service
public class DietUserHealthProfileService extends ServiceImpl<DietUserHealthProfileMapper, DietUserHealthProfile> {

    /**
     * 计算并更新用户健康指标 (BMR, TDEE, DailyTargetCalories)
     */
    public void calculateHealthMetrics(DietUserHealthProfile profile) {
        if (profile == null || profile.getMemberWeight() == null || profile.getMemberHeight() == null
                || profile.getMemberBirthday() == null || profile.getMemberGender() == null) {
            return;
        }

        double weight = profile.getMemberWeight().doubleValue();
        double height = profile.getMemberHeight().doubleValue();
        int age = java.time.Period.between(profile.getMemberBirthday(), java.time.LocalDate.now()).getYears();
        int gender = profile.getMemberGender();

        BigDecimal bmr = HealthMetricsUtil.calculateBmr(weight, height, age, gender);
        profile.setBmrCalories(bmr);

        BigDecimal tdee = HealthMetricsUtil.calculateTdee(bmr, profile.getActivityLevel());
        profile.setTdeeCalories(tdee);

        BigDecimal dailyTarget = HealthMetricsUtil.calculateDailyTargetCalories(bmr, tdee, profile.getDietSpeed());
        profile.setDailyTargetCalories(dailyTarget);
    }

    /**
     * 保存或更新就餐成员健康档案，并自动重算指标
     */
    public boolean saveOrUpdateProfile(DietUserHealthProfile profile) {
        if (profile == null) {
            return false;
        }
        // 自动计算指标并落库
        calculateHealthMetrics(profile);
        return saveOrUpdate(profile);
    }

    /**
     * 根据家庭组ID获取所有有效的家庭成员档案
     */
    public List<DietUserHealthProfile> getProfilesByGroupId(Long groupId) {
        return this.lambdaQuery()
                .eq(groupId != null, DietUserHealthProfile::getGroupId, groupId)
                .eq(DietUserHealthProfile::getDelFlag, 0)
                .list();
    }

    /**
     * 获取成员档案列表，并转化为 VO 响应
     */
    public List<DietUserHealthProfileVO> listProfiles(Long groupId) {
        List<DietUserHealthProfile> list = this.getProfilesByGroupId(groupId);
        return list.stream().map(entity -> {
            DietUserHealthProfileVO vo = new DietUserHealthProfileVO();
            BeanUtil.copyProperties(entity, vo);
            if (entity.getMemberBirthday() != null) {
                vo.setMemberAge(
                        Period.between(entity.getMemberBirthday(), java.time.LocalDate.now()).getYears());
            }
            return vo;
        }).toList();
    }

    /**
     * 保存/更新档案（DTO接收，自动重算）
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveProfile(DietUserHealthProfileDTO dto) {
        if (dto == null) {
            return false;
        }
        DietUserHealthProfile entity;
        if (dto.getProfileId() != null) {
            entity = this.getById(dto.getProfileId());
            if (entity == null) {
                throw new RuntimeException("档案不存在");
            }
        } else {
            entity = new DietUserHealthProfile();
            entity.setDelFlag(0);
        }

        BeanUtil.copyProperties(dto, entity);
        return this.saveOrUpdateProfile(entity);
    }

    /**
     * 批量或单条软删除档案
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteProfiles(BaseDeleteDTO dto) {
        if (dto == null) {
            return false;
        }
        Set<Long> ids = dto.allIds();
        if (CollUtil.isEmpty(ids)) {
            return false;
        }

        List<DietUserHealthProfile> list = ids.stream().map(id -> {
            DietUserHealthProfile entity = new DietUserHealthProfile();
            entity.setProfileId(id);
            entity.setDelFlag(1); // 软删除
            entity.setUpdateTime(LocalDateTime.now());
            return entity;
        }).collect(Collectors.toList());

        return updateBatchById(list);
    }

    /**
     * 获取成员档案分页列表，并转化为 VO 响应
     */
    public IPage<DietUserHealthProfileVO> pageProfiles(DietUserHealthProfileQueryPO po) {
        if (po == null) {
            po = new DietUserHealthProfileQueryPO();
        }
        Page<DietUserHealthProfileVO> page = new Page<>(po.getPageNo(), po.getPageSize());

        // 直接使用 baseMapper 连表查询
        IPage<DietUserHealthProfileVO> pageResult = baseMapper.selectProfilePage(page, po);

        // 统一在内存中计算并填充动态年龄
        if (pageResult.getRecords() != null) {
            pageResult.getRecords().forEach(vo -> {
                if (vo.getMemberBirthday() != null) {
                    vo.setMemberAge(java.time.Period.between(vo.getMemberBirthday(), java.time.LocalDate.now()).getYears());
                }
            });
        }

        return pageResult;
    }
}
