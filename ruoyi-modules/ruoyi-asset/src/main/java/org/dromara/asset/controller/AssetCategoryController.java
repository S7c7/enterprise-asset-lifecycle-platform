package org.dromara.asset.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.asset.domain.bo.AssetCategoryBo;
import org.dromara.asset.domain.vo.AssetCategoryVo;
import org.dromara.asset.service.IAssetCategoryService;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 资产分类接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/asset/category")
public class AssetCategoryController extends BaseController {

    private final IAssetCategoryService assetCategoryService;

    @SaCheckPermission("asset:category:list")
    @GetMapping("/list")
    public R<List<AssetCategoryVo>> list(@Validated(QueryGroup.class) AssetCategoryBo bo) {
        return R.ok(assetCategoryService.queryList(bo));
    }

    @SaCheckPermission("asset:info:list")
    @GetMapping("/options")
    public R<List<AssetCategoryVo>> options() {
        AssetCategoryBo bo = new AssetCategoryBo();
        bo.setStatus("0");
        return R.ok(assetCategoryService.queryList(bo));
    }

    @SaCheckPermission("asset:category:query")
    @GetMapping("/{categoryId}")
    public R<AssetCategoryVo> getInfo(@NotNull(message = "分类主键不能为空")
                                     @PathVariable Long categoryId) {
        return R.ok(assetCategoryService.queryById(categoryId));
    }

    @SaCheckPermission("asset:category:add")
    @Log(title = "资产分类", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody AssetCategoryBo bo) {
        return toAjax(assetCategoryService.insertByBo(bo));
    }

    @SaCheckPermission("asset:category:edit")
    @Log(title = "资产分类", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody AssetCategoryBo bo) {
        return toAjax(assetCategoryService.updateByBo(bo));
    }

    @SaCheckPermission("asset:category:remove")
    @Log(title = "资产分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public R<Void> remove(@NotEmpty(message = "分类主键不能为空")
                          @PathVariable Long[] categoryIds) {
        return toAjax(assetCategoryService.deleteWithValidByIds(Arrays.asList(categoryIds)));
    }
}
