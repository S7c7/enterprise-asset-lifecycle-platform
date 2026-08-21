package org.dromara.asset.mapper;

import org.dromara.asset.domain.AssetIssueOrder;
import org.dromara.asset.domain.vo.AssetIssueOrderVo;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/** 领用执行单 Mapper。 */
@DataPermission({
    @DataColumn(key = "deptName", value = "recipient_dept_id"),
    @DataColumn(key = "userName", value = "recipient_id")
})
public interface AssetIssueOrderMapper extends BaseMapperPlus<AssetIssueOrder, AssetIssueOrderVo> {
}
