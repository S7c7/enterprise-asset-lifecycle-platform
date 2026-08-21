package org.dromara.asset.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/** 领用执行单查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssetIssueOrderBo extends BaseEntity {
    private String issueNo;
    private String status;
    private Long recipientId;
    private Long recipientDeptId;
}
