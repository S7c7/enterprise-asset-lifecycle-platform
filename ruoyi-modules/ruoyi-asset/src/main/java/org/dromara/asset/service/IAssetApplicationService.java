package org.dromara.asset.service;

import org.dromara.asset.domain.bo.AssetApplicationBo;
import org.dromara.asset.domain.vo.AssetApplicationVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.Collection;

/** 资产申请服务。 */
public interface IAssetApplicationService {
    AssetApplicationVo queryById(Long applicationId);
    TableDataInfo<AssetApplicationVo> queryMyPageList(AssetApplicationBo bo, PageQuery pageQuery);
    AssetApplicationVo insertByBo(AssetApplicationBo bo);
    AssetApplicationVo updateByBo(AssetApplicationBo bo);
    Boolean submit(Long applicationId);
    Boolean deleteWithValidByIds(Collection<Long> applicationIds);
}
