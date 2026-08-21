package org.dromara.asset.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.dromara.asset.domain.AssetInfo;
import org.dromara.asset.domain.vo.AssetInfoVo;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 资产台账 Mapper。
 */
public interface AssetInfoMapper extends BaseMapperPlus<AssetInfo, AssetInfoVo> {

    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "keeper_id")
    })
    default <P extends IPage<AssetInfoVo>> P selectVoPage(IPage<AssetInfo> page, Wrapper<AssetInfo> wrapper) {
        return selectVoPage(page, wrapper, this.currentVoClass());
    }

    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "keeper_id")
    })
    default List<AssetInfoVo> selectVoList(Wrapper<AssetInfo> wrapper) {
        return selectVoList(wrapper, this.currentVoClass());
    }

    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "keeper_id")
    })
    List<AssetInfo> selectByIds(@Param(Constants.COLL) Collection<? extends Serializable> idList);

    @Override
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "keeper_id")
    })
    int updateById(@Param(Constants.ENTITY) AssetInfo entity);

    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "keeper_id")
    })
    default int updateLifecycleState(AssetInfo entity, Wrapper<AssetInfo> wrapper) {
        return update(entity, wrapper);
    }
}
