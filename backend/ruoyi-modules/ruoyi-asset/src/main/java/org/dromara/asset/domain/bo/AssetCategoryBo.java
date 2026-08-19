package org.dromara.asset.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.asset.domain.AssetCategory;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 资产分类业务对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AssetCategory.class, reverseConvertGenerate = false)
public class AssetCategoryBo extends BaseEntity {

    @NotNull(message = "分类主键不能为空", groups = EditGroup.class)
    private Long categoryId;

    private Long parentId;

    @NotBlank(message = "分类编码不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 32, message = "分类编码长度不能超过32个字符")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "分类编码只能包含字母、数字、下划线和中划线")
    private String categoryCode;

    @NotBlank(message = "分类名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 100, message = "分类名称长度不能超过100个字符")
    private String categoryName;

    @Min(value = 0, message = "折旧年限不能小于0")
    private Integer depreciationYears;

    @Min(value = 0, message = "显示顺序不能小于0")
    private Integer orderNum;

    @Pattern(regexp = "^[01]$", message = "状态值不正确")
    private String status;
}
