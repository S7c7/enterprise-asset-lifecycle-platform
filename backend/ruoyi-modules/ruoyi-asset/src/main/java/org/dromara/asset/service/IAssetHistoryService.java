package org.dromara.asset.service;

import org.dromara.asset.domain.bo.AssetHistoryBo;
import org.dromara.asset.domain.vo.AssetHistoryVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/** 资产履历查询服务。 */
public interface IAssetHistoryService {

    TableDataInfo<AssetHistoryVo> queryPageList(AssetHistoryBo bo, PageQuery pageQuery);
}
