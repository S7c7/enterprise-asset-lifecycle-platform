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

/**
 * 资产台账实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset_info")
public class AssetInfo extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "asset_id")
    private Long assetId;

    private String assetCode;

    private String assetName;

    private Long categoryId;

    private String specification;

    private String brand;

    private String unit;

    private LocalDate purchaseDate;

    private BigDecimal originalValue;

    private String assetStatus;

    private Long deptId;

    private Long keeperId;

    private String location;

    private LocalDate warrantyExpiryDate;

    @TableLogic
    private Long delFlag;
}
