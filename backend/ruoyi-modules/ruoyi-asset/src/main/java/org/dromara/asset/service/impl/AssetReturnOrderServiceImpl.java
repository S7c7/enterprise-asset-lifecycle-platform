package org.dromara.asset.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.asset.domain.AssetApplication;
import org.dromara.asset.domain.AssetApplicationItem;
import org.dromara.asset.domain.AssetHistory;
import org.dromara.asset.domain.AssetInfo;
import org.dromara.asset.domain.AssetReturnOrder;
import org.dromara.asset.domain.bo.AssetReturnOrderBo;
import org.dromara.asset.domain.vo.AssetApplicationItemVo;
import org.dromara.asset.domain.vo.AssetReturnOrderVo;
import org.dromara.asset.mapper.AssetApplicationItemMapper;
import org.dromara.asset.mapper.AssetApplicationMapper;
import org.dromara.asset.mapper.AssetHistoryMapper;
import org.dromara.asset.mapper.AssetInfoMapper;
import org.dromara.asset.mapper.AssetReturnOrderMapper;
import org.dromara.asset.service.IAssetReturnOrderService;
import org.dromara.common.core.enums.BusinessStatusEnum;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** 归还执行单服务实现。 */
@RequiredArgsConstructor
@Service
public class AssetReturnOrderServiceImpl implements IAssetReturnOrderService {

    static final String STATUS_PENDING = "pending";
    static final String STATUS_COMPLETED = "completed";
    static final String ASSET_STATUS_INVENTORY = "0";
    static final String ASSET_STATUS_IN_USE = "1";

    private final AssetReturnOrderMapper baseMapper;
    private final AssetApplicationMapper applicationMapper;
    private final AssetApplicationItemMapper applicationItemMapper;
    private final AssetInfoMapper assetInfoMapper;
    private final AssetHistoryMapper historyMapper;

    @Override
    public AssetReturnOrderVo queryById(Long returnId) {
        AssetReturnOrderVo vo = baseMapper.selectVoById(returnId);
        if (vo == null) {
            throw new ServiceException("归还执行单不存在或无权查看");
        }
        enrichApplications(List.of(vo), true);
        return vo;
    }

    @Override
    public TableDataInfo<AssetReturnOrderVo> queryPageList(AssetReturnOrderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AssetReturnOrder> wrapper = Wrappers.<AssetReturnOrder>lambdaQuery()
            .like(StringUtils.isNotBlank(bo.getReturnNo()), AssetReturnOrder::getReturnNo, bo.getReturnNo())
            .eq(StringUtils.isNotBlank(bo.getStatus()), AssetReturnOrder::getStatus, bo.getStatus())
            .eq(bo.getReturnerId() != null, AssetReturnOrder::getReturnerId, bo.getReturnerId())
            .eq(bo.getReturnDeptId() != null, AssetReturnOrder::getReturnDeptId, bo.getReturnDeptId())
            .orderByAsc(AssetReturnOrder::getStatus)
            .orderByDesc(AssetReturnOrder::getCreateTime);
        Page<AssetReturnOrderVo> page = baseMapper.selectVoPage(pageQuery.build(), wrapper);
        enrichApplications(page.getRecords(), false);
        return TableDataInfo.build(page);
    }

    @Override
    public void createFromApprovedApplication(AssetApplication application) {
        if (!"return".equals(application.getApplicationType())
            || !BusinessStatusEnum.FINISH.getStatus().equals(application.getStatus())) {
            return;
        }
        boolean exists = baseMapper.exists(Wrappers.<AssetReturnOrder>lambdaQuery()
            .eq(AssetReturnOrder::getApplicationId, application.getApplicationId()));
        if (exists) {
            return;
        }
        AssetReturnOrder order = new AssetReturnOrder();
        order.setReturnNo(generateReturnNo());
        order.setApplicationId(application.getApplicationId());
        order.setReturnerId(application.getApplicantId());
        order.setReturnDeptId(application.getApplyDeptId());
        order.setStatus(STATUS_PENDING);
        order.setVersion(0L);
        baseMapper.insert(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(Long returnId) {
        AssetReturnOrder order = baseMapper.selectById(returnId);
        requirePending(order);
        AssetApplication application = applicationMapper.selectById(order.getApplicationId());
        if (application == null || !"return".equals(application.getApplicationType())
            || !BusinessStatusEnum.FINISH.getStatus().equals(application.getStatus())) {
            throw new ServiceException("来源归还申请未审批完成，不能执行");
        }
        List<AssetApplicationItem> items = applicationItemMapper.selectList(Wrappers.<AssetApplicationItem>lambdaQuery()
            .eq(AssetApplicationItem::getApplicationId, application.getApplicationId())
            .orderByAsc(AssetApplicationItem::getItemId));
        if (items.isEmpty()) {
            throw new ServiceException("归还执行单没有可执行的资产明细");
        }
        Set<Long> assetIds = items.stream().map(AssetApplicationItem::getAssetId)
            .filter(Objects::nonNull).collect(Collectors.toSet());
        if (assetIds.size() != items.size()) {
            throw new ServiceException("归还执行单包含无效或重复的资产明细");
        }
        Map<Long, AssetInfo> assets = new HashMap<>();
        assetInfoMapper.selectByIds(assetIds).forEach(asset -> assets.put(asset.getAssetId(), asset));
        if (assets.size() != assetIds.size()) {
            throw new ServiceException("归还执行单包含不存在或无权访问的资产");
        }

        Long operatorId = LoginHelper.getUserId();
        LocalDateTime operationTime = LocalDateTime.now();
        for (AssetApplicationItem item : items) {
            AssetInfo before = assets.get(item.getAssetId());
            if (!ASSET_STATUS_IN_USE.equals(before.getAssetStatus())) {
                throw new ServiceException("资产“" + before.getAssetName() + "”已不是使用中状态，不能归还");
            }
            if (!Objects.equals(before.getKeeperId(), order.getReturnerId())) {
                throw new ServiceException("资产“" + before.getAssetName() + "”当前保管人与归还申请人不一致");
            }
            String targetLocation = StringUtils.isBlank(item.getTargetLocation())
                ? before.getLocation() : item.getTargetLocation();
            AssetInfo update = new AssetInfo();
            update.setAssetId(before.getAssetId());
            update.setAssetStatus(ASSET_STATUS_INVENTORY);
            update.setLocation(targetLocation);
            int updated = assetInfoMapper.updateLifecycleState(update, Wrappers.<AssetInfo>lambdaUpdate()
                .set(AssetInfo::getDeptId, null)
                .set(AssetInfo::getKeeperId, null)
                .eq(AssetInfo::getAssetId, before.getAssetId())
                .eq(AssetInfo::getAssetStatus, ASSET_STATUS_IN_USE)
                .eq(AssetInfo::getKeeperId, order.getReturnerId()));
            if (updated != 1) {
                throw new ServiceException("资产“" + before.getAssetName() + "”状态或保管人已变化，请刷新后重试");
            }
            historyMapper.insert(buildHistory(before, order, targetLocation, operatorId, operationTime));
        }

        order.setStatus(STATUS_COMPLETED);
        order.setReturnedBy(operatorId);
        order.setReturnedTime(operationTime);
        if (baseMapper.updateById(order) != 1) {
            throw new ServiceException("归还执行单已被其他人处理，请刷新后重试");
        }
        return true;
    }

    void requirePending(AssetReturnOrder order) {
        if (order == null) {
            throw new ServiceException("归还执行单不存在或无权操作");
        }
        if (!STATUS_PENDING.equals(order.getStatus())) {
            throw new ServiceException("归还执行单已完成，不能重复执行");
        }
    }

    private AssetHistory buildHistory(AssetInfo before, AssetReturnOrder order, String targetLocation,
                                      Long operatorId, LocalDateTime operationTime) {
        AssetHistory history = new AssetHistory();
        history.setAssetId(before.getAssetId());
        history.setAssetCode(before.getAssetCode());
        history.setAssetName(before.getAssetName());
        history.setBusinessType("return");
        history.setBusinessId(order.getReturnId());
        history.setBeforeStatus(before.getAssetStatus());
        history.setAfterStatus(ASSET_STATUS_INVENTORY);
        history.setBeforeDeptId(before.getDeptId());
        history.setAfterDeptId(null);
        history.setBeforeKeeperId(before.getKeeperId());
        history.setAfterKeeperId(null);
        history.setBeforeLocation(before.getLocation());
        history.setAfterLocation(targetLocation);
        history.setOperatorId(operatorId);
        history.setOperationTime(operationTime);
        history.setDescription("执行归还单" + order.getReturnNo() + "，资产由使用中转为库存");
        return history;
    }

    private void enrichApplications(List<AssetReturnOrderVo> orders, boolean includeItems) {
        if (orders.isEmpty()) {
            return;
        }
        Set<Long> applicationIds = orders.stream().map(AssetReturnOrderVo::getApplicationId).collect(Collectors.toSet());
        Map<Long, AssetApplication> applications = applicationMapper.selectByIds(applicationIds).stream()
            .collect(Collectors.toMap(AssetApplication::getApplicationId, application -> application));
        for (AssetReturnOrderVo order : orders) {
            AssetApplication application = applications.get(order.getApplicationId());
            if (application != null) {
                order.setApplicationNo(application.getApplicationNo());
                order.setApplicationTitle(application.getTitle());
            }
            if (includeItems) {
                List<AssetApplicationItemVo> items = applicationItemMapper.selectVoList(
                    Wrappers.<AssetApplicationItem>lambdaQuery()
                        .eq(AssetApplicationItem::getApplicationId, order.getApplicationId())
                        .orderByAsc(AssetApplicationItem::getItemId));
                order.setItems(items);
            }
        }
    }

    private String generateReturnNo() {
        String suffix = IdUtil.getSnowflakeNextIdStr();
        return "ZCGH" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + suffix.substring(suffix.length() - 4);
    }
}
