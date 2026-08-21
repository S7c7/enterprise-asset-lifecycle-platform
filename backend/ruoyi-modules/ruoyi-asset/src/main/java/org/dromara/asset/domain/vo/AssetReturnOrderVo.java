package org.dromara.asset.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.asset.domain.AssetReturnOrder;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/** 归还执行单视图。 */
@Data
@AutoMapper(target = AssetReturnOrder.class)
public class AssetReturnOrderVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long returnId;
    private String returnNo;
    private Long applicationId;
    private String applicationNo;
    private String applicationTitle;
    private Long returnerId;
    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "returnerId")
    private String returnerName;
    private Long returnDeptId;
    @Translation(type = TransConstant.DEPT_ID_TO_NAME, mapper = "returnDeptId")
    private String returnDeptName;
    private String status;
    private Long returnedBy;
    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "returnedBy")
    private String returnedByName;
    private LocalDateTime returnedTime;
    private Long version;
    private String remark;
    private Date createTime;
    private List<AssetApplicationItemVo> items;
}
