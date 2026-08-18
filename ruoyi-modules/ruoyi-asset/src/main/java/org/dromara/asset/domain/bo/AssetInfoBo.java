package org.dromara.asset.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.asset.domain.AssetInfo;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 资产台账业务对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AssetInfo.class, reverseConvertGenerate = false)
public class AssetInfoBo extends BaseEntity {

    @NotNull(message = "资产主键不能为空", groups = EditGroup.class)
    private Long assetId;

    @NotBlank(message = "资产编码不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 64, message = "资产编码长度不能超过64个字符")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "资产编码只能包含字母、数字、下划线和中划线")
    private String assetCode;

    @NotBlank(message = "资产名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 200, message = "资产名称长度不能超过200个字符")
    private String assetName;

    @NotNull(message = "资产分类不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long categoryId;

    @Size(max = 200, message = "规格型号长度不能超过200个字符")
    private String specification;

    @Size(max = 100, message = "品牌长度不能超过100个字符")
    private String brand;

    @Size(max = 20, message = "计量单位长度不能超过20个字符")
    private String unit;

    @PastOrPresent(message = "购置日期不能晚于今天")
    private LocalDate purchaseDate;

    @DecimalMin(value = "0.00", message = "资产原值不能小于0")
    private BigDecimal originalValue;

    @NotBlank(message = "资产状态不能为空", groups = {AddGroup.class, EditGroup.class})
    @Pattern(regexp = "^[0-4]$", message = "资产状态值不正确")
    private String assetStatus;

    private Long deptId;

    private Long keeperId;

    @Size(max = 200, message = "存放地点长度不能超过200个字符")
    private String location;

    private LocalDate warrantyExpiryDate;
}
