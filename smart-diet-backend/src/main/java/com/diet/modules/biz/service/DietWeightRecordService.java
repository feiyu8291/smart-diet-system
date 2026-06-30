package com.diet.modules.biz.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.diet.modules.biz.mapper.DietWeightRecordMapper;
import com.diet.modules.biz.model.entity.DietWeightRecord;
import org.springframework.stereotype.Service;

/**
 * 成员体重记录历史业务服务类
 *
 * @author FeiYu
 * @date 2026-06-30
 */
@Service
public class DietWeightRecordService extends ServiceImpl<DietWeightRecordMapper, DietWeightRecord> {
}
