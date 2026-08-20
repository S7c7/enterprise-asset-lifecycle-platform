package org.dromara.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.dromara.asset.domain.AssetCategory;
import org.dromara.asset.domain.AssetInfo;
import org.dromara.asset.mapper.AssetCategoryMapper;
import org.dromara.asset.mapper.AssetInfoMapper;
import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
@DisplayName("资产台账状态与基础数据规则")
class AssetInfoServiceImplTest {

    @Mock
    private AssetInfoMapper assetInfoMapper;
    @Mock
    private AssetCategoryMapper categoryMapper;

    private AssetInfoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AssetInfoServiceImpl(assetInfoMapper, categoryMapper);
    }

    @Test
    @DisplayName("资产编码不可重复")
    void duplicateAssetCodeIsRejected() {
        when(assetInfoMapper.exists(any(Wrapper.class))).thenReturn(true);
        assertThrows(ServiceException.class, () -> service.validEntityBeforeSave(asset("0")));
    }

    @Test
    @DisplayName("资产分类必须存在")
    void categoryMustExist() {
        when(assetInfoMapper.exists(any(Wrapper.class))).thenReturn(false);
        when(categoryMapper.selectById(1L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> service.validEntityBeforeSave(asset("0")));
    }

    @Test
    @DisplayName("停用分类不能新增资产")
    void disabledCategoryIsRejected() {
        when(assetInfoMapper.exists(any(Wrapper.class))).thenReturn(false);
        when(categoryMapper.selectById(1L)).thenReturn(category("1"));
        assertThrows(ServiceException.class, () -> service.validEntityBeforeSave(asset("0")));
    }

    @Test
    @DisplayName("保修到期日不能早于购置日期")
    void warrantyCannotExpireBeforePurchase() {
        when(assetInfoMapper.exists(any(Wrapper.class))).thenReturn(false);
        when(categoryMapper.selectById(1L)).thenReturn(category("0"));
        AssetInfo asset = asset("0");
        asset.setPurchaseDate(LocalDate.of(2026, 1, 2));
        asset.setWarrantyExpiryDate(LocalDate.of(2026, 1, 1));
        assertThrows(ServiceException.class, () -> service.validEntityBeforeSave(asset));
    }

    @Test
    @DisplayName("非库存状态资产不能删除")
    void nonInventoryAssetCannotBeDeleted() {
        when(assetInfoMapper.selectByIds(List.of(1L))).thenReturn(List.of(asset("1")));
        assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(1L)));
    }

    @Test
    @DisplayName("删除时发现资产不存在应拒绝")
    void missingAssetCannotBeDeleted() {
        when(assetInfoMapper.selectByIds(List.of(1L, 2L))).thenReturn(List.of(asset("0")));
        assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(1L, 2L)));
    }

    @Test
    @DisplayName("库存资产可以删除")
    void inventoryAssetCanBeDeleted() {
        when(assetInfoMapper.selectByIds(List.of(1L))).thenReturn(List.of(asset("0")));
        when(assetInfoMapper.deleteByIds(List.of(1L))).thenReturn(1);
        assertTrue(service.deleteWithValidByIds(List.of(1L)));
    }

    private AssetInfo asset(String status) {
        AssetInfo asset = new AssetInfo();
        asset.setAssetId(1L);
        asset.setAssetCode("ASSET-001");
        asset.setCategoryId(1L);
        asset.setAssetStatus(status);
        return asset;
    }

    private AssetCategory category(String status) {
        AssetCategory category = new AssetCategory();
        category.setCategoryId(1L);
        category.setStatus(status);
        return category;
    }
}
