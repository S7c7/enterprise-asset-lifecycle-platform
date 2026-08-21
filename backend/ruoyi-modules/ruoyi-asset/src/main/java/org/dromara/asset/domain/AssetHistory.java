package org.dromara.asset.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/** 资产状态、责任人和位置的变更履历。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset_history")
public class AssetHistory extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "history_id")
    private Long historyId;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private String businessType;
    private Long businessId;
    private String beforeStatus;
    private String afterStatus;
    private Long beforeDeptId;
    private Long afterDeptId;
    private Long beforeKeeperId;
    private Long afterKeeperId;
    private String beforeLocation;
    private String afterLocation;
    private Long operatorId;
    private LocalDateTime operationTime;
    private String description;

    @TableLogic
    private Long delFlag;
}
