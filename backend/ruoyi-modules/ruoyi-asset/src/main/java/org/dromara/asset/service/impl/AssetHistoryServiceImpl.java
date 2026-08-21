package org.dromara.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.asset.domain.AssetHistory;
import org.dromara.asset.domain.bo.AssetHistoryBo;
import org.dromara.asset.domain.vo.AssetHistoryVo;
import org.dromara.asset.mapper.AssetHistoryMapper;
import org.dromara.asset.service.IAssetHistoryService;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

/** 资产履历只允许查询，不提供修改和删除入口。 */
@RequiredArgsConstructor
@Service
public class AssetHistoryServiceImpl implements IAssetHistoryService {

    private final AssetHistoryMapper baseMapper;

    @Override
    public TableDataInfo<AssetHistoryVo> queryPageList(AssetHistoryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AssetHistory> wrapper = Wrappers.<AssetHistory>lambdaQuery()
            .eq(bo.getAssetId() != null, AssetHistory::getAssetId, bo.getAssetId())
            .like(StringUtils.isNotBlank(bo.getAssetCode()), AssetHistory::getAssetCode, bo.getAssetCode())
            .like(StringUtils.isNotBlank(bo.getAssetName()), AssetHistory::getAssetName, bo.getAssetName())
            .eq(StringUtils.isNotBlank(bo.getBusinessType()), AssetHistory::getBusinessType, bo.getBusinessType())
            .eq(bo.getBusinessId() != null, AssetHistory::getBusinessId, bo.getBusinessId())
            .orderByDesc(AssetHistory::getOperationTime)
            .orderByDesc(AssetHistory::getHistoryId);
        Page<AssetHistoryVo> page = baseMapper.selectVoPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }
}
