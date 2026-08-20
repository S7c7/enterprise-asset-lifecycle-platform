package org.dromara.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.asset.domain.AssetCategory;
import org.dromara.asset.domain.AssetInfo;
import org.dromara.asset.domain.bo.AssetInfoBo;
import org.dromara.asset.domain.vo.AssetInfoVo;
import org.dromara.asset.mapper.AssetCategoryMapper;
import org.dromara.asset.mapper.AssetInfoMapper;
import org.dromara.asset.service.IAssetInfoService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 资产台账服务实现。
 */
@RequiredArgsConstructor
@Service
public class AssetInfoServiceImpl implements IAssetInfoService {

    private final AssetInfoMapper baseMapper;
    private final AssetCategoryMapper categoryMapper;

    @Override
    public AssetInfoVo queryById(Long assetId) {
        AssetInfoVo vo = baseMapper.selectVoList(Wrappers.<AssetInfo>lambdaQuery()
                .eq(AssetInfo::getAssetId, assetId))
            .stream().findFirst().orElse(null);
        enrichCategoryNames(vo == null ? List.of() : List.of(vo));
        return vo;
    }

    @Override
    public TableDataInfo<AssetInfoVo> queryPageList(AssetInfoBo bo, PageQuery pageQuery) {
        Page<AssetInfoVo> page = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        enrichCategoryNames(page.getRecords());
        return TableDataInfo.build(page);
    }

    private LambdaQueryWrapper<AssetInfo> buildQueryWrapper(AssetInfoBo bo) {
        Map<String, Object> params = bo.getParams();
        return Wrappers.<AssetInfo>lambdaQuery()
            .like(StringUtils.isNotBlank(bo.getAssetCode()), AssetInfo::getAssetCode, bo.getAssetCode())
            .like(StringUtils.isNotBlank(bo.getAssetName()), AssetInfo::getAssetName, bo.getAssetName())
            .eq(bo.getCategoryId() != null, AssetInfo::getCategoryId, bo.getCategoryId())
            .eq(StringUtils.isNotBlank(bo.getAssetStatus()), AssetInfo::getAssetStatus, bo.getAssetStatus())
            .eq(bo.getDeptId() != null, AssetInfo::getDeptId, bo.getDeptId())
            .eq(bo.getKeeperId() != null, AssetInfo::getKeeperId, bo.getKeeperId())
            .between(params.get("beginPurchaseDate") != null && params.get("endPurchaseDate") != null,
                AssetInfo::getPurchaseDate, params.get("beginPurchaseDate"), params.get("endPurchaseDate"))
            .orderByDesc(AssetInfo::getCreateTime)
            .orderByDesc(AssetInfo::getAssetId);
    }

    private void enrichCategoryNames(List<AssetInfoVo> list) {
        Set<Long> categoryIds = new HashSet<>();
        list.forEach(item -> categoryIds.add(item.getCategoryId()));
        if (categoryIds.isEmpty()) {
            return;
        }
        Map<Long, String> names = new HashMap<>();
        for (AssetCategory category : categoryMapper.selectByIds(categoryIds)) {
            names.put(category.getCategoryId(), category.getCategoryName());
        }
        list.forEach(item -> item.setCategoryName(names.get(item.getCategoryId())));
    }

    @Override
    public Boolean insertByBo(AssetInfoBo bo) {
        AssetInfo entity = MapstructUtils.convert(bo, AssetInfo.class);
        normalize(entity);
        validEntityBeforeSave(entity);
        boolean success = baseMapper.insert(entity) > 0;
        if (success) {
            bo.setAssetId(entity.getAssetId());
        }
        return success;
    }

    @Override
    public Boolean updateByBo(AssetInfoBo bo) {
        if (baseMapper.selectById(bo.getAssetId()) == null) {
            throw new ServiceException("资产台账不存在或已被删除");
        }
        AssetInfo entity = MapstructUtils.convert(bo, AssetInfo.class);
        normalize(entity);
        validEntityBeforeSave(entity);
        return baseMapper.updateById(entity) > 0;
    }

    private void normalize(AssetInfo entity) {
        entity.setAssetStatus(StringUtils.isBlank(entity.getAssetStatus()) ? "0" : entity.getAssetStatus());
    }

    void validEntityBeforeSave(AssetInfo entity) {
        boolean duplicateCode = baseMapper.exists(Wrappers.<AssetInfo>lambdaQuery()
            .eq(AssetInfo::getAssetCode, entity.getAssetCode())
            .ne(entity.getAssetId() != null, AssetInfo::getAssetId, entity.getAssetId()));
        if (duplicateCode) {
            throw new ServiceException("资产编码已存在");
        }
        AssetCategory category = categoryMapper.selectById(entity.getCategoryId());
        if (category == null) {
            throw new ServiceException("资产分类不存在");
        }
        if (!"0".equals(category.getStatus())) {
            throw new ServiceException("资产分类已停用");
        }
        if (entity.getPurchaseDate() != null && entity.getWarrantyExpiryDate() != null
            && entity.getWarrantyExpiryDate().isBefore(entity.getPurchaseDate())) {
            throw new ServiceException("保修到期日不能早于购置日期");
        }
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> assetIds) {
        List<AssetInfo> assets = baseMapper.selectByIds(assetIds);
        if (assets.size() != assetIds.size()) {
            throw new ServiceException("资产台账不存在或无权删除");
        }
        boolean hasNonInventoryAsset = assets.stream().anyMatch(asset -> !"0".equals(asset.getAssetStatus()));
        if (hasNonInventoryAsset) {
            throw new ServiceException("只有库存状态的资产允许删除");
        }
        return baseMapper.deleteByIds(assetIds) > 0;
    }
}
