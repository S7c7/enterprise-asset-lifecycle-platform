package org.dromara.asset.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;

/** 资产申请明细。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset_application_item")
public class AssetApplicationItem extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "item_id")
    private Long itemId;
    private Long applicationId;
    private Long assetId;
    private Long categoryId;
    private String itemName;
    private String specification;
    private Integer quantity;
    private String unit;
    private BigDecimal estimatedUnitPrice;
    private Long targetDeptId;
    private String targetLocation;

    @TableLogic
    private Long delFlag;
}
