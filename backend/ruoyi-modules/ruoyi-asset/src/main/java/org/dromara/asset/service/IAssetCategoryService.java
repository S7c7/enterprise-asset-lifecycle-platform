package org.dromara.asset.service;

import org.dromara.asset.domain.bo.AssetCategoryBo;
import org.dromara.asset.domain.vo.AssetCategoryVo;

import java.util.Collection;
import java.util.List;

/**
 * 资产分类服务。
 */
public interface IAssetCategoryService {

    AssetCategoryVo queryById(Long categoryId);

    List<AssetCategoryVo> queryList(AssetCategoryBo bo);

    Boolean insertByBo(AssetCategoryBo bo);

    Boolean updateByBo(AssetCategoryBo bo);

    Boolean deleteWithValidByIds(Collection<Long> categoryIds);
}
