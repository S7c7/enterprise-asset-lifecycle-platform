package org.dromara.asset.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.asset.domain.AssetIssueOrder;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/** 领用执行单视图。 */
@Data
@AutoMapper(target = AssetIssueOrder.class)
public class AssetIssueOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long issueId;
    private String issueNo;
    private Long applicationId;
    private String applicationNo;
    private String applicationTitle;
    private Long recipientId;

    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "recipientId")
    private String recipientName;

    private Long recipientDeptId;

    @Translation(type = TransConstant.DEPT_ID_TO_NAME, mapper = "recipientDeptId")
    private String recipientDeptName;

    private String status;
    private Long issuedBy;

    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "issuedBy")
    private String issuedByName;

    private LocalDateTime issuedTime;
    private Long version;
    private String remark;
    private Date createTime;
    private List<AssetApplicationItemVo> items;
}
