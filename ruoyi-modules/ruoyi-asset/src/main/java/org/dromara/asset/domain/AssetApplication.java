package org.dromara.asset.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;

/** 资产申请主表。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset_application")
public class AssetApplication extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "application_id")
    private Long applicationId;
    private String applicationNo;
    private String applicationType;
    private String title;
    private Long applicantId;
    private Long applyDeptId;
    private BigDecimal totalAmount;
    private LocalDate expectedDate;
    private String reason;
    private String status;
    private String flowCode;

    @TableLogic
    private Long delFlag;
}
