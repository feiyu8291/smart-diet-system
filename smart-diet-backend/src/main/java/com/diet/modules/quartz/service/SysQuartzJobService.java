package com.diet.modules.quartz.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.common.exception.BusinessException;
import com.diet.modules.quartz.mapper.SysQuartzJobMapper;
import com.diet.modules.quartz.model.entity.SysQuartzJob;
import com.diet.modules.quartz.model.po.SysQuartzJobQueryPO;
import com.diet.modules.quartz.model.po.SysQuartzJobSavePO;
import com.diet.modules.quartz.model.po.SysQuartzJobUpdatePO;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Objects;

/**
 * 任务业务逻辑实现
 *
 * @author FeiYu
 * @date 2026-06-20
 */
@Service
@Slf4j
public class SysQuartzJobService extends ServiceImpl<SysQuartzJobMapper, SysQuartzJob> {

    private final DynamicJobService dynamicJobService;

    public SysQuartzJobService(DynamicJobService dynamicJobService) {
        this.dynamicJobService = dynamicJobService;
    }

    public Page<SysQuartzJob> page(SysQuartzJobQueryPO queryPO) {
        return lambdaQuery()
                .like(StrUtil.isNotBlank(queryPO.getJobName()), SysQuartzJob::getJobName, queryPO.getJobName())
                .eq(queryPO.getJobType() != null, SysQuartzJob::getJobType, queryPO.getJobType())
                .eq(queryPO.getDelFlag() != null, SysQuartzJob::getDelFlag, queryPO.getDelFlag())
                .orderByDesc(SysQuartzJob::getCreateTime)
                .page(new Page<>(queryPO.getPageNo(), queryPO.getPageSize()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveJob(SysQuartzJobSavePO req) {
        if (!CronExpression.isValidExpression(req.getCronExpression())) {
            throw BusinessException.withMessageParamsError("Cron 表达式不合法");
        }
        boolean exists = lambdaQuery().eq(SysQuartzJob::getJobName, req.getJobName()).exists();
        if (exists) {
            throw BusinessException.withMessage("同名任务已存在");
        }

        SysQuartzJob job = new SysQuartzJob();
        BeanUtil.copyProperties(req, job);
        job.setJobGroup(StrUtil.isBlank(req.getJobGroup()) ? "DEFAULT" : req.getJobGroup());
        job.setDelFlag(0);
        save(job);

        try {
            dynamicJobService.addJobToScheduler(job);
        } catch (Exception e) {
            log.error("添加任务到调度器失败", e);
            throw BusinessException.withMessage("启动定时任务失败: " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateJob(SysQuartzJobUpdatePO req) {
        if (!CronExpression.isValidExpression(req.getCronExpression())) {
            throw BusinessException.withMessageParamsError("Cron 表达式不合法");
        }
        SysQuartzJob job = getById(req.getId());
        if (job == null) {
            throw BusinessException.withMessage("任务不存在");
        }

        BeanUtil.copyProperties(req, job, "id", "jobGroup", "delFlag", "createTime");
        updateById(job);

        try {
            if (Objects.equals(0, job.getDelFlag())) {
                dynamicJobService.modifySchedulerJob(job, req.getCronExpression());
            }
        } catch (Exception e) {
            log.error("修改调度器任务失败", e);
            throw BusinessException.withMessage("修改调度器任务失败: " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(Collection<Long> ids) {
        for (Long id : ids) {
            SysQuartzJob job = getById(id);
            if (job != null) {
                try {
                    dynamicJobService.removeJobFromScheduler(job);
                } catch (Exception e) {
                    log.error("从调度器移除任务失败 {}", id, e);
                }
                job.setDelFlag(1);
                updateById(job);
            }
        }
    }

    public void triggerJob(Long id) {
        SysQuartzJob job = getById(id);
        if (job != null) {
            try {
                dynamicJobService.triggerJob(job);
            } catch (Exception e) {
                log.error("手动触发任务失败", e);
                throw BusinessException.withMessage("手动触发任务失败: " + e.getMessage());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void pauseJob(Long id) {
        SysQuartzJob job = getById(id);
        if (job != null) {
            try {
                dynamicJobService.pauseJob(job);
            } catch (Exception e) {
                log.error("暂停任务失败", e);
                throw BusinessException.withMessage("暂停任务失败: " + e.getMessage());
            }
            job.setDelFlag(1);
            updateById(job);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void resumeJob(Long id) {
        SysQuartzJob job = getById(id);
        if (job != null) {
            try {
                dynamicJobService.resumeJob(job);
            } catch (Exception e) {
                log.error("恢复任务失败", e);
                throw BusinessException.withMessage("恢复任务失败: " + e.getMessage());
            }
            job.setDelFlag(0);
            updateById(job);
        }
    }
}
