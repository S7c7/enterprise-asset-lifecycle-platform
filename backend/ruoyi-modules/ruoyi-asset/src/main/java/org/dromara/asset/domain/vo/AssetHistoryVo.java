package org.dromara.asset.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.asset.domain.AssetHistory;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 资产履历视图。 */
@Data
@AutoMapper(target = AssetHistory.class)
public class AssetHistoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long historyId;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private String businessType;
    private Long businessId;
    private String beforeStatus;
    private String afterStatus;
    private Long beforeDeptId;

    @Translation(type = TransConstant.DEPT_ID_TO_NAME, mapper = "beforeDeptId")
    private String beforeDeptName;

    private Long afterDeptId;

    @Translation(type = TransConstant.DEPT_ID_TO_NAME, mapper = "afterDeptId")
    private String afterDeptName;

    private Long beforeKeeperId;

    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "beforeKeeperId")
    private String beforeKeeperName;

    private Long afterKeeperId;

    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "afterKeeperId")
    private String afterKeeperName;

    private String beforeLocation;
    private String afterLocation;
    private Long operatorId;

    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "operatorId")
    private String operatorName;

    private LocalDateTime operationTime;
    private String description;
}
