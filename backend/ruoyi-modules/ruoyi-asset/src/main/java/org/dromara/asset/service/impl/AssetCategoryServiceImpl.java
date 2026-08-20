package org.dromara.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.asset.domain.AssetCategory;
import org.dromara.asset.domain.AssetInfo;
import org.dromara.asset.domain.bo.AssetCategoryBo;
import org.dromara.asset.domain.vo.AssetCategoryVo;
import org.dromara.asset.mapper.AssetCategoryMapper;
import org.dromara.asset.mapper.AssetInfoMapper;
import org.dromara.asset.service.IAssetCategoryService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 资产分类服务实现。
 */
@RequiredArgsConstructor
@Service
public class AssetCategoryServiceImpl implements IAssetCategoryService {

    private final AssetCategoryMapper baseMapper;
    private final AssetInfoMapper assetInfoMapper;

    @Override
    public AssetCategoryVo queryById(Long categoryId) {
        AssetCategoryVo vo = baseMapper.selectVoById(categoryId);
        if (vo != null && vo.getParentId() != null && vo.getParentId() != 0L) {
            AssetCategory parent = baseMapper.selectById(vo.getParentId());
            vo.setParentName(parent == null ? null : parent.getCategoryName());
        }
        return vo;
    }

    @Override
    public List<AssetCategoryVo> queryList(AssetCategoryBo bo) {
        List<AssetCategoryVo> list = baseMapper.selectVoList(buildQueryWrapper(bo));
        enrichParentNames(list);
        return list;
    }

    private LambdaQueryWrapper<AssetCategory> buildQueryWrapper(AssetCategoryBo bo) {
        return Wrappers.<AssetCategory>lambdaQuery()
            .eq(bo.getParentId() != null, AssetCategory::getParentId, bo.getParentId())
            .like(StringUtils.isNotBlank(bo.getCategoryCode()), AssetCategory::getCategoryCode, bo.getCategoryCode())
            .like(StringUtils.isNotBlank(bo.getCategoryName()), AssetCategory::getCategoryName, bo.getCategoryName())
            .eq(StringUtils.isNotBlank(bo.getStatus()), AssetCategory::getStatus, bo.getStatus())
            .orderByAsc(AssetCategory::getOrderNum)
            .orderByAsc(AssetCategory::getCategoryId);
    }

    private void enrichParentNames(List<AssetCategoryVo> list) {
        Set<Long> parentIds = new HashSet<>();
        for (AssetCategoryVo item : list) {
            if (item.getParentId() != null && item.getParentId() != 0L) {
                parentIds.add(item.getParentId());
            }
        }
        if (parentIds.isEmpty()) {
            return;
        }
        Map<Long, String> names = new HashMap<>();
        for (AssetCategory parent : baseMapper.selectByIds(parentIds)) {
            names.put(parent.getCategoryId(), parent.getCategoryName());
        }
        list.forEach(item -> item.setParentName(names.get(item.getParentId())));
    }

    @Override
    public Boolean insertByBo(AssetCategoryBo bo) {
        AssetCategory entity = MapstructUtils.convert(bo, AssetCategory.class);
        normalize(entity);
        validEntityBeforeSave(entity);
        boolean success = baseMapper.insert(entity) > 0;
        if (success) {
            bo.setCategoryId(entity.getCategoryId());
        }
        return success;
    }

    @Override
    public Boolean updateByBo(AssetCategoryBo bo) {
        if (baseMapper.selectById(bo.getCategoryId()) == null) {
            throw new ServiceException("资产分类不存在或已被删除");
        }
        AssetCategory entity = MapstructUtils.convert(bo, AssetCategory.class);
        normalize(entity);
        validEntityBeforeSave(entity);
        return baseMapper.updateById(entity) > 0;
    }

    private void normalize(AssetCategory entity) {
        entity.setParentId(entity.getParentId() == null ? 0L : entity.getParentId());
        entity.setOrderNum(entity.getOrderNum() == null ? 0 : entity.getOrderNum());
        entity.setDepreciationYears(entity.getDepreciationYears() == null ? 0 : entity.getDepreciationYears());
        entity.setStatus(StringUtils.isBlank(entity.getStatus()) ? "0" : entity.getStatus());
    }

    void validEntityBeforeSave(AssetCategory entity) {
        boolean duplicateCode = baseMapper.exists(Wrappers.<AssetCategory>lambdaQuery()
            .eq(AssetCategory::getCategoryCode, entity.getCategoryCode())
            .ne(entity.getCategoryId() != null, AssetCategory::getCategoryId, entity.getCategoryId()));
        if (duplicateCode) {
            throw new ServiceException("资产分类编码已存在");
        }
        if (entity.getParentId() == 0L) {
            return;
        }
        if (entity.getParentId().equals(entity.getCategoryId())) {
            throw new ServiceException("上级分类不能选择自身");
        }
        AssetCategory parent = baseMapper.selectById(entity.getParentId());
        if (parent == null) {
            throw new ServiceException("上级分类不存在");
        }
        Set<Long> visited = new HashSet<>(Collections.singleton(entity.getCategoryId()));
        while (parent.getParentId() != null && parent.getParentId() != 0L) {
            if (!visited.add(parent.getCategoryId())) {
                throw new ServiceException("资产分类层级不能形成循环");
            }
            parent = baseMapper.selectById(parent.getParentId());
            if (parent == null) {
                break;
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> categoryIds) {
        if (baseMapper.exists(Wrappers.<AssetCategory>lambdaQuery()
            .in(AssetCategory::getParentId, categoryIds))) {
            throw new ServiceException("存在下级资产分类，不能删除");
        }
        if (assetInfoMapper.exists(Wrappers.<AssetInfo>lambdaQuery()
            .in(AssetInfo::getCategoryId, categoryIds))) {
            throw new ServiceException("分类下存在资产台账，不能删除");
        }
        return baseMapper.deleteByIds(categoryIds) > 0;
    }
}
