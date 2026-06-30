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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 成员健康档案服务类
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Slf4j
@Service
public class DietUserHealthProfileService extends ServiceImpl<DietUserHealthProfileMapper, DietUserHealthProfile> {

    /**
     * 计算并更新用户健康指标 (BMR, TDEE, DailyTargetCalories)
     */
    public void calculateHealthMetrics(DietUserHealthProfile profile) {
        if (Objects.isNull(profile) || Objects.isNull(profile.getMemberWeight()) || Objects.isNull(profile.getMemberHeight())
                || Objects.isNull(profile.getMemberBirthday()) || Objects.isNull(profile.getMemberGender())) {
            return;
        }

        double weight = profile.getMemberWeight().doubleValue();
        double height = profile.getMemberHeight().doubleValue();
        int age = Period.between(profile.getMemberBirthday(), LocalDate.now()).getYears();
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
        if (Objects.isNull(profile)) {
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
                .eq(Objects.nonNull(groupId), DietUserHealthProfile::getGroupId, groupId)
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
            if (Objects.nonNull(entity.getMemberBirthday())) {
                vo.setMemberAge(
                        Period.between(entity.getMemberBirthday(), LocalDate.now()).getYears());
            }
            return vo;
        }).toList();
    }

    /**
     * 保存/更新档案（DTO接收，自动重算）
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveProfile(DietUserHealthProfileDTO dto) {
        if (Objects.isNull(dto)) {
            return false;
        }
        DietUserHealthProfile entity;
        if (Objects.nonNull(dto.getProfileId())) {
            entity = this.getById(dto.getProfileId());
            if (Objects.isNull(entity)) {
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
        if (Objects.isNull(dto)) {
            return false;
        }
        Set<Long> ids = dto.allIds();
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        return this.removeBatchByIds(ids);
    }

    /**
     * 获取成员档案分页列表，并转化为 VO 响应
     */
    public IPage<DietUserHealthProfileVO> pageProfiles(DietUserHealthProfileQueryPO po) {
        if (Objects.isNull(po)) {
            po = new DietUserHealthProfileQueryPO();
        }
        Page<DietUserHealthProfileVO> page = new Page<>(po.getPageNo(), po.getPageSize());

        // 直接使用 baseMapper 连表查询
        IPage<DietUserHealthProfileVO> pageResult = baseMapper.selectProfilePage(page, po);

        // 统一在内存中计算并填充动态年龄
        if (Objects.nonNull(pageResult.getRecords())) {
            pageResult.getRecords().forEach(vo -> {
                if (Objects.nonNull(vo.getMemberBirthday())) {
                    vo.setMemberAge(Period.between(vo.getMemberBirthday(), LocalDate.now()).getYears());
                }
            });
        }

        return pageResult;
    }
}
