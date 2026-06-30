package com.diet.modules.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.diet.modules.biz.model.entity.DietUserHealthProfile;
import com.diet.modules.biz.model.po.DietUserHealthProfileQueryPO;
import com.diet.modules.biz.model.vo.DietUserHealthProfileVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DietUserHealthProfileMapper extends BaseMapper<DietUserHealthProfile> {

    /**
     * 连表分页查询成员健康档案
     */
    IPage<DietUserHealthProfileVO> selectProfilePage(
            Page<DietUserHealthProfileVO> page,
            @Param("po") DietUserHealthProfileQueryPO po
    );
}
