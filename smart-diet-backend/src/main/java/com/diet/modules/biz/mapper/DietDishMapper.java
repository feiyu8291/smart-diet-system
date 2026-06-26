package com.diet.modules.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.diet.modules.biz.model.entity.DietDish;
import com.diet.modules.biz.model.po.DietDishQueryPO;
import com.diet.modules.biz.model.vo.DietDishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DietDishMapper extends BaseMapper<DietDish> {

    /**
     * 关联分页查询菜品摘要视图及统计信息
     *
     * @param page 分页参数
     * @param po   查询参数 PO
     * @return 分页视图列表
     */
    IPage<DietDishVO> selectDishPage(IPage<?> page, @Param("po") DietDishQueryPO po);
}
