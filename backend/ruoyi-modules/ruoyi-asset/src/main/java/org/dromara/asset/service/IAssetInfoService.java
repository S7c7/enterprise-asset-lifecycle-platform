package org.dromara.asset.service;

import org.dromara.asset.domain.bo.AssetInfoBo;
import org.dromara.asset.domain.vo.AssetInfoVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.Collection;

/**
 * 资产台账服务。
 */
public interface IAssetInfoService {

    AssetInfoVo queryById(Long assetId);

    TableDataInfo<AssetInfoVo> queryPageList(AssetInfoBo bo, PageQuery pageQuery);

    Boolean insertByBo(AssetInfoBo bo);

    Boolean updateByBo(AssetInfoBo bo);

    Boolean deleteWithValidByIds(Collection<Long> assetIds);
}
