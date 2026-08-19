package org.dromara.asset.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.asset.domain.AssetApplicationItem;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;

import java.math.BigDecimal;

/** 资产申请明细业务对象。 */
@Data
@AutoMapper(target = AssetApplicationItem.class, reverseConvertGenerate = false)
public class AssetApplicationItemBo {
    private Long itemId;
    private Long assetId;

    @NotNull(message = "资产分类不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long categoryId;

    @NotBlank(message = "明细名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 200, message = "明细名称不能超过200个字符", groups = {AddGroup.class, EditGroup.class})
    private String itemName;

    @Size(max = 200, message = "规格型号不能超过200个字符", groups = {AddGroup.class, EditGroup.class})
    private String specification;

    @NotNull(message = "数量不能为空", groups = {AddGroup.class, EditGroup.class})
    @Min(value = 1, message = "数量必须大于0", groups = {AddGroup.class, EditGroup.class})
    private Integer quantity;

    @Size(max = 20, message = "计量单位不能超过20个字符", groups = {AddGroup.class, EditGroup.class})
    private String unit;

    @DecimalMin(value = "0.00", message = "预估单价不能小于0", groups = {AddGroup.class, EditGroup.class})
    private BigDecimal estimatedUnitPrice;
    private Long targetDeptId;

    @Size(max = 200, message = "目标地点不能超过200个字符", groups = {AddGroup.class, EditGroup.class})
    private String targetLocation;

    @Size(max = 500, message = "明细备注不能超过500个字符", groups = {AddGroup.class, EditGroup.class})
    private String remark;
}
