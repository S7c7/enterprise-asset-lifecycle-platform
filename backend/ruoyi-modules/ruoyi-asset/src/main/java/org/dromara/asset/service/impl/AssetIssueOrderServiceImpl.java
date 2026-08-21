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
import org.dromara.asset.domain.AssetIssueOrder;
import org.dromara.asset.domain.bo.AssetIssueOrderBo;
import org.dromara.asset.domain.vo.AssetApplicationItemVo;
import org.dromara.asset.domain.vo.AssetIssueOrderVo;
import org.dromara.asset.mapper.AssetApplicationItemMapper;
import org.dromara.asset.mapper.AssetApplicationMapper;
import org.dromara.asset.mapper.AssetHistoryMapper;
import org.dromara.asset.mapper.AssetInfoMapper;
import org.dromara.asset.mapper.AssetIssueOrderMapper;
import org.dromara.asset.service.IAssetIssueOrderService;
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

/** 领用执行单服务实现。 */
@RequiredArgsConstructor
@Service
public class AssetIssueOrderServiceImpl implements IAssetIssueOrderService {

    static final String STATUS_PENDING = "pending";
    static final String STATUS_COMPLETED = "completed";
    static final String ASSET_STATUS_INVENTORY = "0";
    static final String ASSET_STATUS_IN_USE = "1";

    private final AssetIssueOrderMapper baseMapper;
    private final AssetApplicationMapper applicationMapper;
    private final AssetApplicationItemMapper applicationItemMapper;
    private final AssetInfoMapper assetInfoMapper;
    private final AssetHistoryMapper historyMapper;

    @Override
    public AssetIssueOrderVo queryById(Long issueId) {
        AssetIssueOrderVo vo = baseMapper.selectVoById(issueId);
        if (vo == null) {
            throw new ServiceException("领用执行单不存在或无权查看");
        }
        enrichApplications(List.of(vo), true);
        return vo;
    }

    @Override
    public TableDataInfo<AssetIssueOrderVo> queryPageList(AssetIssueOrderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AssetIssueOrder> wrapper = Wrappers.<AssetIssueOrder>lambdaQuery()
            .like(StringUtils.isNotBlank(bo.getIssueNo()), AssetIssueOrder::getIssueNo, bo.getIssueNo())
            .eq(StringUtils.isNotBlank(bo.getStatus()), AssetIssueOrder::getStatus, bo.getStatus())
            .eq(bo.getRecipientId() != null, AssetIssueOrder::getRecipientId, bo.getRecipientId())
            .eq(bo.getRecipientDeptId() != null, AssetIssueOrder::getRecipientDeptId, bo.getRecipientDeptId())
            .orderByAsc(AssetIssueOrder::getStatus)
            .orderByDesc(AssetIssueOrder::getCreateTime);
        Page<AssetIssueOrderVo> page = baseMapper.selectVoPage(pageQuery.build(), wrapper);
        enrichApplications(page.getRecords(), false);
        return TableDataInfo.build(page);
    }

    @Override
    public void createFromApprovedApplication(AssetApplication application) {
        if (!"use".equals(application.getApplicationType())
            || !BusinessStatusEnum.FINISH.getStatus().equals(application.getStatus())) {
            return;
        }
        boolean exists = baseMapper.exists(Wrappers.<AssetIssueOrder>lambdaQuery()
            .eq(AssetIssueOrder::getApplicationId, application.getApplicationId()));
        if (exists) {
            return;
        }
        AssetIssueOrder order = new AssetIssueOrder();
        order.setIssueNo(generateIssueNo());
        order.setApplicationId(application.getApplicationId());
        order.setRecipientId(application.getApplicantId());
        order.setRecipientDeptId(application.getApplyDeptId());
        order.setStatus(STATUS_PENDING);
        order.setVersion(0L);
        baseMapper.insert(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(Long issueId) {
        AssetIssueOrder order = baseMapper.selectById(issueId);
        requirePending(order);
        AssetApplication application = applicationMapper.selectById(order.getApplicationId());
        if (application == null || !"use".equals(application.getApplicationType())
            || !BusinessStatusEnum.FINISH.getStatus().equals(application.getStatus())) {
            throw new ServiceException("来源领用申请未审批完成，不能执行");
        }
        List<AssetApplicationItem> items = applicationItemMapper.selectList(Wrappers.<AssetApplicationItem>lambdaQuery()
            .eq(AssetApplicationItem::getApplicationId, application.getApplicationId())
            .orderByAsc(AssetApplicationItem::getItemId));
        if (items.isEmpty()) {
            throw new ServiceException("领用执行单没有可执行的资产明细");
        }
        Set<Long> assetIds = items.stream().map(AssetApplicationItem::getAssetId)
            .filter(Objects::nonNull).collect(Collectors.toSet());
        if (assetIds.size() != items.size()) {
            throw new ServiceException("领用执行单包含无效或重复的资产明细");
        }
        Map<Long, AssetInfo> assets = new HashMap<>();
        assetInfoMapper.selectByIds(assetIds).forEach(asset -> assets.put(asset.getAssetId(), asset));
        if (assets.size() != assetIds.size()) {
            throw new ServiceException("领用执行单包含不存在或无权访问的资产");
        }

        Long operatorId = LoginHelper.getUserId();
        LocalDateTime operationTime = LocalDateTime.now();
        for (AssetApplicationItem item : items) {
            AssetInfo before = assets.get(item.getAssetId());
            if (!ASSET_STATUS_INVENTORY.equals(before.getAssetStatus())) {
                throw new ServiceException("资产“" + before.getAssetName() + "”已不是库存状态，不能重复领用");
            }
            Long targetDeptId = item.getTargetDeptId() == null ? order.getRecipientDeptId() : item.getTargetDeptId();
            String targetLocation = StringUtils.isBlank(item.getTargetLocation()) ? before.getLocation() : item.getTargetLocation();
            AssetInfo update = new AssetInfo();
            update.setAssetId(before.getAssetId());
            update.setAssetStatus(ASSET_STATUS_IN_USE);
            update.setDeptId(targetDeptId);
            update.setKeeperId(order.getRecipientId());
            update.setLocation(targetLocation);
            int updated = assetInfoMapper.updateLifecycleState(update, Wrappers.<AssetInfo>lambdaUpdate()
                .eq(AssetInfo::getAssetId, before.getAssetId())
                .eq(AssetInfo::getAssetStatus, ASSET_STATUS_INVENTORY));
            if (updated != 1) {
                throw new ServiceException("资产“" + before.getAssetName() + "”状态已变化，请刷新后重试");
            }
            historyMapper.insert(buildHistory(before, order, targetDeptId, targetLocation, operatorId, operationTime));
        }

        order.setStatus(STATUS_COMPLETED);
        order.setIssuedBy(operatorId);
        order.setIssuedTime(operationTime);
        if (baseMapper.updateById(order) != 1) {
            throw new ServiceException("领用执行单已被其他人处理，请刷新后重试");
        }
        return true;
    }

    void requirePending(AssetIssueOrder order) {
        if (order == null) {
            throw new ServiceException("领用执行单不存在或无权操作");
        }
        if (!STATUS_PENDING.equals(order.getStatus())) {
            throw new ServiceException("领用执行单已完成，不能重复执行");
        }
    }

    private AssetHistory buildHistory(AssetInfo before, AssetIssueOrder order, Long targetDeptId,
                                      String targetLocation, Long operatorId, LocalDateTime operationTime) {
        AssetHistory history = new AssetHistory();
        history.setAssetId(before.getAssetId());
        history.setAssetCode(before.getAssetCode());
        history.setAssetName(before.getAssetName());
        history.setBusinessType("issue");
        history.setBusinessId(order.getIssueId());
        history.setBeforeStatus(before.getAssetStatus());
        history.setAfterStatus(ASSET_STATUS_IN_USE);
        history.setBeforeDeptId(before.getDeptId());
        history.setAfterDeptId(targetDeptId);
        history.setBeforeKeeperId(before.getKeeperId());
        history.setAfterKeeperId(order.getRecipientId());
        history.setBeforeLocation(before.getLocation());
        history.setAfterLocation(targetLocation);
        history.setOperatorId(operatorId);
        history.setOperationTime(operationTime);
        history.setDescription("执行领用单" + order.getIssueNo() + "，资产由库存转为使用中");
        return history;
    }

    private void enrichApplications(List<AssetIssueOrderVo> orders, boolean includeItems) {
        if (orders.isEmpty()) {
            return;
        }
        Set<Long> applicationIds = orders.stream().map(AssetIssueOrderVo::getApplicationId).collect(Collectors.toSet());
        Map<Long, AssetApplication> applications = applicationMapper.selectByIds(applicationIds).stream()
            .collect(Collectors.toMap(AssetApplication::getApplicationId, application -> application));
        for (AssetIssueOrderVo order : orders) {
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

    private String generateIssueNo() {
        String suffix = IdUtil.getSnowflakeNextIdStr();
        return "ZCLY" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + suffix.substring(suffix.length() - 4);
    }
}
