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

/** 审批通过后等待资产管理员验收入库的归还单。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset_return_order")
public class AssetReturnOrder extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "return_id")
    private Long returnId;
    private String returnNo;
    private Long applicationId;
    private Long returnerId;
    private Long returnDeptId;
    private String status;
    private Long returnedBy;
    private LocalDateTime returnedTime;

    @Version
    private Long version;

    @TableLogic
    private Long delFlag;
}
