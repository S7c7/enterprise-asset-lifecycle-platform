package org.dromara.asset.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/** 审批通过后等待资产管理员执行的领用单。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset_issue_order")
public class AssetIssueOrder extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "issue_id")
    private Long issueId;
    private String issueNo;
    private Long applicationId;
    private Long recipientId;
    private Long recipientDeptId;
    private String status;
    private Long issuedBy;
    private LocalDateTime issuedTime;

    @Version
    private Long version;

    @TableLogic
    private Long delFlag;
}
