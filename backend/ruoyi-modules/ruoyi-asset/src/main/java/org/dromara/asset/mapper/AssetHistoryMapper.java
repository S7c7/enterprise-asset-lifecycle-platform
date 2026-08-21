package org.dromara.asset.mapper;

import org.dromara.asset.domain.AssetHistory;
import org.dromara.asset.domain.vo.AssetHistoryVo;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/** 资产履历 Mapper。 */
@DataPermission({
    @DataColumn(key = "deptName", value = "after_dept_id"),
    @DataColumn(key = "userName", value = "after_keeper_id")
})
public interface AssetHistoryMapper extends BaseMapperPlus<AssetHistory, AssetHistoryVo> {
}
