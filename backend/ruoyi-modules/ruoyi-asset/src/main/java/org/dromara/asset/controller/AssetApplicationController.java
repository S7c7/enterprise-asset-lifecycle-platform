package org.dromara.asset.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.asset.domain.bo.AssetApplicationBo;
import org.dromara.asset.domain.vo.AssetApplicationVo;
import org.dromara.asset.service.IAssetApplicationService;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/** 资产申请接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/asset/application")
public class AssetApplicationController extends BaseController {

    private final IAssetApplicationService applicationService;

    @SaCheckPermission("asset:application:list")
    @GetMapping("/list")
    public TableDataInfo<AssetApplicationVo> list(AssetApplicationBo bo, PageQuery pageQuery) {
        return applicationService.queryMyPageList(bo, pageQuery);
    }

    @SaCheckPermission("asset:application:query")
    @GetMapping("/{applicationId}")
    public R<AssetApplicationVo> getInfo(
        @NotNull(message = "申请主键不能为空") @PathVariable Long applicationId,
        @RequestParam(required = false) Long taskId) {
        return R.ok(applicationService.queryById(applicationId, taskId));
    }

    @SaCheckPermission("asset:application:add")
    @Log(title = "资产申请", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<AssetApplicationVo> add(@Validated(AddGroup.class) @RequestBody AssetApplicationBo bo) {
        return R.ok(applicationService.insertByBo(bo));
    }

    @SaCheckPermission("asset:application:edit")
    @Log(title = "资产申请", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public R<AssetApplicationVo> edit(@Validated(EditGroup.class) @RequestBody AssetApplicationBo bo) {
        return R.ok(applicationService.updateByBo(bo));
    }

    @SaCheckPermission("asset:application:submit")
    @Log(title = "资产申请提交审批", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/{applicationId}/submit")
    public R<Void> submit(@NotNull(message = "申请主键不能为空") @PathVariable Long applicationId) {
        return toAjax(applicationService.submit(applicationId));
    }

    @SaCheckPermission("asset:application:remove")
    @Log(title = "资产申请", businessType = BusinessType.DELETE)
    @DeleteMapping("/{applicationIds}")
    public R<Void> remove(@NotEmpty(message = "申请主键不能为空") @PathVariable Long[] applicationIds) {
        return toAjax(applicationService.deleteWithValidByIds(Arrays.asList(applicationIds)));
    }
}
