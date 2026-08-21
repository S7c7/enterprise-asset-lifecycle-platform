package org.dromara.asset.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.asset.domain.bo.AssetHistoryBo;
import org.dromara.asset.domain.vo.AssetHistoryVo;
import org.dromara.asset.service.IAssetHistoryService;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 资产履历只读接口。 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/asset/history")
public class AssetHistoryController extends BaseController {

    private final IAssetHistoryService historyService;

    @SaCheckPermission("asset:history:list")
    @GetMapping("/list")
    public TableDataInfo<AssetHistoryVo> list(AssetHistoryBo bo, PageQuery pageQuery) {
        return historyService.queryPageList(bo, pageQuery);
    }
}
