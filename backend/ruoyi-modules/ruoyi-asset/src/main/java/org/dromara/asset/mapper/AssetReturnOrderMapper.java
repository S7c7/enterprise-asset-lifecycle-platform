package org.dromara.asset.mapper;

import org.dromara.asset.domain.AssetReturnOrder;
import org.dromara.asset.domain.vo.AssetReturnOrderVo;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/** 归还执行单 Mapper。 */
@DataPermission({
    @DataColumn(key = "deptName", value = "return_dept_id"),
    @DataColumn(key = "userName", value = "returner_id")
})
public interface AssetReturnOrderMapper extends BaseMapperPlus<AssetReturnOrder, AssetReturnOrderVo> {
}
