package org.dromara.asset.service;

import org.dromara.asset.domain.AssetApplication;
import org.dromara.asset.domain.bo.AssetIssueOrderBo;
import org.dromara.asset.domain.vo.AssetIssueOrderVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/** 领用执行单服务。 */
public interface IAssetIssueOrderService {

    AssetIssueOrderVo queryById(Long issueId);

    TableDataInfo<AssetIssueOrderVo> queryPageList(AssetIssueOrderBo bo, PageQuery pageQuery);

    void createFromApprovedApplication(AssetApplication application);

    Boolean execute(Long issueId);
}
