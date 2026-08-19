package org.dromara.asset.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.asset.domain.AssetInfo;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * 资产台账视图对象。
 */
@Data
@AutoMapper(target = AssetInfo.class)
public class AssetInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long assetId;
    private String assetCode;
    private String assetName;
    private Long categoryId;
    private String categoryName;
    private String specification;
    private String brand;
    private String unit;
    private LocalDate purchaseDate;
    private BigDecimal originalValue;
    private String assetStatus;
    private Long deptId;

    @Translation(type = TransConstant.DEPT_ID_TO_NAME, mapper = "deptId")
    private String deptName;

    private Long keeperId;

    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "keeperId")
    private String keeperName;

    private String location;
    private LocalDate warrantyExpiryDate;
    private String remark;
    private Date createTime;
    private Date updateTime;
}
