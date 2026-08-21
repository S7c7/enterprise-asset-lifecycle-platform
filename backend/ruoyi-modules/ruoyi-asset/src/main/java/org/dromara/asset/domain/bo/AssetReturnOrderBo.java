package org.dromara.asset.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/** 归还执行单查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssetReturnOrderBo extends BaseEntity {
    private String returnNo;
    private String status;
    private Long returnerId;
    private Long returnDeptId;
}
