package org.dromara.asset.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.asset.domain.AssetApplication;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/** 资产申请视图。 */
@Data
@AutoMapper(target = AssetApplication.class)
public class AssetApplicationVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long applicationId;
    private String applicationNo;
    private String applicationType;
    private String title;
    private Long applicantId;

    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "applicantId")
    private String applicantName;

    private Long applyDeptId;

    @Translation(type = TransConstant.DEPT_ID_TO_NAME, mapper = "applyDeptId")
    private String applyDeptName;

    private BigDecimal totalAmount;
    private LocalDate expectedDate;
    private String reason;
    private String status;
    private String flowCode;
    private String remark;
    private Date createTime;
    private Date updateTime;
    private List<AssetApplicationItemVo> items;
}
