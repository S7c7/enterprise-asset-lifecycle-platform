package org.dromara.asset.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.asset.domain.bo.AssetIssueOrderBo;
import org.dromara.asset.domain.vo.AssetIssueOrderVo;
import org.dromara.asset.service.IAssetIssueOrderService;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 领用执行单接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/asset/issue")
public class AssetIssueOrderController extends BaseController {

    private final IAssetIssueOrderService issueOrderService;

    @SaCheckPermission("asset:issue:list")
    @GetMapping("/list")
    public TableDataInfo<AssetIssueOrderVo> list(AssetIssueOrderBo bo, PageQuery pageQuery) {
        return issueOrderService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("asset:issue:query")
    @GetMapping("/{issueId}")
    public R<AssetIssueOrderVo> getInfo(
        @NotNull(message = "领用执行单主键不能为空") @PathVariable Long issueId) {
        return R.ok(issueOrderService.queryById(issueId));
    }

    @SaCheckPermission("asset:issue:execute")
    @Log(title = "领用执行", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/{issueId}/execute")
    public R<Void> execute(
        @NotNull(message = "领用执行单主键不能为空") @PathVariable Long issueId) {
        return toAjax(issueOrderService.execute(issueId));
    }
}
