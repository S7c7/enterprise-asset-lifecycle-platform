package org.dromara.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.dromara.asset.domain.AssetCategory;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
@DisplayName("资产分类层级与删除规则")
class AssetCategoryServiceImplTest {

    @Mock
    private AssetCategoryMapper categoryMapper;
    @Mock
    private AssetInfoMapper assetInfoMapper;

    private AssetCategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AssetCategoryServiceImpl(categoryMapper, assetInfoMapper);
    }

    @Test
    @DisplayName("分类编码不可重复")
    void duplicateCodeIsRejected() {
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(true);
        assertThrows(ServiceException.class, () -> service.validEntityBeforeSave(category(1L, 0L)));
    }

    @Test
    @DisplayName("上级分类不能选择自身")
    void categoryCannotBeItsOwnParent() {
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(false);
        assertThrows(ServiceException.class, () -> service.validEntityBeforeSave(category(1L, 1L)));
    }

    @Test
    @DisplayName("上级分类必须存在")
    void parentMustExist() {
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(false);
        when(categoryMapper.selectById(2L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> service.validEntityBeforeSave(category(1L, 2L)));
    }

    @Test
    @DisplayName("分类层级不能形成循环")
    void categoryHierarchyCannotCycle() {
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(false);
        when(categoryMapper.selectById(2L)).thenReturn(category(2L, 3L));
        when(categoryMapper.selectById(3L)).thenReturn(category(3L, 2L));
        assertThrows(ServiceException.class, () -> service.validEntityBeforeSave(category(1L, 2L)));
    }

    @Test
    @DisplayName("存在下级分类时不能删除")
    void categoryWithChildrenCannotBeDeleted() {
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(true);
        assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(1L)));
    }

    @Test
    @DisplayName("分类下存在资产台账时不能删除")
    void categoryInUseCannotBeDeleted() {
        when(categoryMapper.exists(any(Wrapper.class))).thenReturn(false);
        when(assetInfoMapper.exists(any(Wrapper.class))).thenReturn(true);
        assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(1L)));
    }

    private AssetCategory category(Long id, Long parentId) {
        AssetCategory category = new AssetCategory();
        category.setCategoryId(id);
        category.setParentId(parentId);
        category.setCategoryCode("CAT-" + id);
        return category;
    }
}
