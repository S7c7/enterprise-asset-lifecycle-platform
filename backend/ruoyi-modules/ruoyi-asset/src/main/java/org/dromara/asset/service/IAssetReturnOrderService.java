package org.dromara.asset.service;

import org.dromara.asset.domain.AssetApplication;
import org.dromara.asset.domain.bo.AssetReturnOrderBo;
import org.dromara.asset.domain.vo.AssetReturnOrderVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/** 归还执行单服务。 */
public interface IAssetReturnOrderService {
    AssetReturnOrderVo queryById(Long returnId);
    TableDataInfo<AssetReturnOrderVo> queryPageList(AssetReturnOrderBo bo, PageQuery pageQuery);
    void createFromApprovedApplication(AssetApplication application);
    Boolean execute(Long returnId);
}
