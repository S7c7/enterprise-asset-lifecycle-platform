package org.dromara.asset.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.asset.domain.bo.AssetInfoBo;
import org.dromara.asset.domain.vo.AssetInfoVo;
import org.dromara.asset.service.IAssetInfoService;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.core.validate.QueryGroup;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
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

/**
 * 资产台账接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/asset/info")
public class AssetInfoController extends BaseController {

    private final IAssetInfoService assetInfoService;

    @SaCheckPermission("asset:info:list")
    @GetMapping("/list")
    public TableDataInfo<AssetInfoVo> list(@Validated(QueryGroup.class) AssetInfoBo bo, PageQuery pageQuery) {
        return assetInfoService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("asset:info:query")
    @GetMapping("/{assetId}")
    public R<AssetInfoVo> getInfo(@NotNull(message = "资产主键不能为空")
                                  @PathVariable Long assetId) {
        return R.ok(assetInfoService.queryById(assetId));
    }

    @SaCheckPermission("asset:info:add")
    @Log(title = "资产台账", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody AssetInfoBo bo) {
        return toAjax(assetInfoService.insertByBo(bo));
    }

    @SaCheckPermission("asset:info:edit")
    @Log(title = "资产台账", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody AssetInfoBo bo) {
        return toAjax(assetInfoService.updateByBo(bo));
    }

    @SaCheckPermission("asset:info:remove")
    @Log(title = "资产台账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{assetIds}")
    public R<Void> remove(@NotEmpty(message = "资产主键不能为空")
                          @PathVariable Long[] assetIds) {
        return toAjax(assetInfoService.deleteWithValidByIds(Arrays.asList(assetIds)));
    }
}
