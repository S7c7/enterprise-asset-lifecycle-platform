package org.dromara.asset.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.asset.domain.AssetCategory;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 资产分类视图对象。
 */
@Data
@AutoMapper(target = AssetCategory.class)
public class AssetCategoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long categoryId;
    private Long parentId;
    private String parentName;
    private String categoryCode;
    private String categoryName;
    private Integer depreciationYears;
    private Integer orderNum;
    private String status;
    private String remark;
    private Date createTime;
}
