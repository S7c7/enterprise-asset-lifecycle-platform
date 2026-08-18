package org.dromara.asset.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 资产分类实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset_category")
public class AssetCategory extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "category_id")
    private Long categoryId;

    private Long parentId;

    private String categoryCode;

    private String categoryName;

    private Integer depreciationYears;

    private Integer orderNum;

    private String status;

    @TableLogic
    private Long delFlag;
}
