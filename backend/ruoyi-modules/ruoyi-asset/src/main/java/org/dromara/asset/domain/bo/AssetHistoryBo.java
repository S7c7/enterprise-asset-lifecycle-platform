package org.dromara.asset.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/** 资产履历查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssetHistoryBo extends BaseEntity {
    private Long assetId;
    private String assetCode;
    private String assetName;
    private String businessType;
    private Long businessId;
}
