package org.dromara.asset.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.asset.domain.AssetApplication;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.time.LocalDate;
import java.util.List;

/** 资产申请业务对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AssetApplication.class, reverseConvertGenerate = false)
public class AssetApplicationBo extends BaseEntity {

    @NotNull(message = "申请主键不能为空", groups = EditGroup.class)
    private Long applicationId;

    private String applicationNo;

    @NotBlank(message = "申请类型不能为空", groups = {AddGroup.class, EditGroup.class})
    @Pattern(regexp = "^(purchase|use|transfer|return|scrap)$", message = "申请类型不正确", groups = {AddGroup.class, EditGroup.class})
    private String applicationType;

    @NotBlank(message = "申请标题不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 200, message = "申请标题不能超过200个字符", groups = {AddGroup.class, EditGroup.class})
    private String title;

    private LocalDate expectedDate;

    @NotBlank(message = "申请原因不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 1000, message = "申请原因不能超过1000个字符", groups = {AddGroup.class, EditGroup.class})
    private String reason;

    private String status;
    private String flowCode;

    @Valid
    @NotEmpty(message = "至少需要一条申请明细", groups = {AddGroup.class, EditGroup.class})
    private List<AssetApplicationItemBo> items;
}
