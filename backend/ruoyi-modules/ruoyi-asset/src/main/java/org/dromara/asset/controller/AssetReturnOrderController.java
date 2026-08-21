package org.dromara.asset.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.asset.domain.bo.AssetReturnOrderBo;
import org.dromara.asset.domain.vo.AssetReturnOrderVo;
import org.dromara.asset.service.IAssetReturnOrderService;
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

/** 归还执行单接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/asset/return")
public class AssetReturnOrderController extends BaseController {

    private final IAssetReturnOrderService returnOrderService;

    @SaCheckPermission("asset:return:list")
    @GetMapping("/list")
    public TableDataInfo<AssetReturnOrderVo> list(AssetReturnOrderBo bo, PageQuery pageQuery) {
        return returnOrderService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("asset:return:query")
    @GetMapping("/{returnId}")
    public R<AssetReturnOrderVo> getInfo(
        @NotNull(message = "归还执行单主键不能为空") @PathVariable Long returnId) {
        return R.ok(returnOrderService.queryById(returnId));
    }

    @SaCheckPermission("asset:return:execute")
    @Log(title = "资产归还入库", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/{returnId}/execute")
    public R<Void> execute(
        @NotNull(message = "归还执行单主键不能为空") @PathVariable Long returnId) {
        return toAjax(returnOrderService.execute(returnId));
    }
}
