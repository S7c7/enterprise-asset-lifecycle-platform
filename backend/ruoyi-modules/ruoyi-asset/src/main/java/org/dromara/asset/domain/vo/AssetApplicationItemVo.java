package org.dromara.asset.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.asset.domain.AssetApplicationItem;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/** 资产申请明细视图。 */
@Data
@AutoMapper(target = AssetApplicationItem.class)
public class AssetApplicationItemVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long itemId;
    private Long applicationId;
    private Long assetId;
    private Long categoryId;
    private String categoryName;
    private String itemName;
    private String specification;
    private Integer quantity;
    private String unit;
    private BigDecimal estimatedUnitPrice;
    private Long targetDeptId;

    @Translation(type = TransConstant.DEPT_ID_TO_NAME, mapper = "targetDeptId")
    private String targetDeptName;

    private String targetLocation;
    private String remark;
}
