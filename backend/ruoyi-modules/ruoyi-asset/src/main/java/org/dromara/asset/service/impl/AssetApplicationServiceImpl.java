package org.dromara.asset.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.asset.domain.AssetApplication;
import org.dromara.asset.domain.AssetApplicationItem;
import org.dromara.asset.domain.AssetCategory;
import org.dromara.asset.domain.AssetInfo;
import org.dromara.asset.domain.bo.AssetApplicationBo;
import org.dromara.asset.domain.bo.AssetApplicationItemBo;
import org.dromara.asset.domain.vo.AssetApplicationItemVo;
import org.dromara.asset.domain.vo.AssetApplicationVo;
import org.dromara.asset.mapper.AssetApplicationItemMapper;
import org.dromara.asset.mapper.AssetApplicationMapper;
import org.dromara.asset.mapper.AssetCategoryMapper;
import org.dromara.asset.mapper.AssetInfoMapper;
import org.dromara.asset.service.IAssetApplicationService;
import org.dromara.asset.service.IAssetIssueOrderService;
import org.dromara.asset.service.IAssetReturnOrderService;
import org.dromara.common.core.domain.dto.FlowInstanceBizExtDTO;
import org.dromara.common.core.domain.dto.StartProcessDTO;
import org.dromara.common.core.domain.event.ProcessEvent;
import org.dromara.common.core.enums.BusinessStatusEnum;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.WorkflowService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 资产申请服务实现。 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AssetApplicationServiceImpl implements IAssetApplicationService {

    private static final String FLOW_CODE = "asset_apply";
    private static final Set<String> EDITABLE_STATUSES = Set.of(
        BusinessStatusEnum.DRAFT.getStatus(),
        BusinessStatusEnum.BACK.getStatus(),
        BusinessStatusEnum.CANCEL.getStatus()
    );

    private final AssetApplicationMapper baseMapper;
    private final AssetApplicationItemMapper itemMapper;
    private final AssetCategoryMapper categoryMapper;
    private final AssetInfoMapper assetInfoMapper;
    private final WorkflowService workflowService;
    private final IAssetIssueOrderService issueOrderService;
    private final IAssetReturnOrderService returnOrderService;

    @Override
    public AssetApplicationVo queryById(Long applicationId, Long taskId) {
        requireReadable(applicationId, taskId);
        AssetApplicationVo vo = baseMapper.selectVoById(applicationId);
        if (vo != null) {
            fillItems(vo);
        }
        return vo;
    }

    @Override
    public TableDataInfo<AssetApplicationVo> queryMyPageList(AssetApplicationBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AssetApplication> wrapper = Wrappers.<AssetApplication>lambdaQuery()
            .eq(AssetApplication::getApplicantId, LoginHelper.getUserId())
            .like(StringUtils.isNotBlank(bo.getApplicationNo()), AssetApplication::getApplicationNo, bo.getApplicationNo())
            .like(StringUtils.isNotBlank(bo.getTitle()), AssetApplication::getTitle, bo.getTitle())
            .eq(StringUtils.isNotBlank(bo.getApplicationType()), AssetApplication::getApplicationType, bo.getApplicationType())
            .eq(StringUtils.isNotBlank(bo.getStatus()), AssetApplication::getStatus, bo.getStatus())
            .orderByDesc(AssetApplication::getCreateTime)
            .orderByDesc(AssetApplication::getApplicationId);
        Page<AssetApplicationVo> page = baseMapper.selectVoPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetApplicationVo insertByBo(AssetApplicationBo bo) {
        validateItems(bo);
        AssetApplication entity = MapstructUtils.convert(bo, AssetApplication.class);
        entity.setApplicationNo(generateApplicationNo());
        entity.setApplicantId(LoginHelper.getUserId());
        entity.setApplyDeptId(LoginHelper.getDeptId());
        entity.setTotalAmount(calculateTotal(bo.getItems()));
        entity.setStatus(BusinessStatusEnum.DRAFT.getStatus());
        entity.setFlowCode(FLOW_CODE);
        baseMapper.insert(entity);
        saveItems(entity.getApplicationId(), bo.getItems());
        return queryById(entity.getApplicationId(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetApplicationVo updateByBo(AssetApplicationBo bo) {
        AssetApplication existing = requireOwned(bo.getApplicationId());
        requireEditable(existing);
        validateItems(bo);
        AssetApplication entity = MapstructUtils.convert(bo, AssetApplication.class);
        entity.setApplicationNo(existing.getApplicationNo());
        entity.setApplicantId(existing.getApplicantId());
        entity.setApplyDeptId(existing.getApplyDeptId());
        entity.setTotalAmount(calculateTotal(bo.getItems()));
        entity.setStatus(existing.getStatus());
        entity.setFlowCode(FLOW_CODE);
        baseMapper.updateById(entity);
        itemMapper.delete(Wrappers.<AssetApplicationItem>lambdaQuery()
            .eq(AssetApplicationItem::getApplicationId, entity.getApplicationId()));
        saveItems(entity.getApplicationId(), bo.getItems());
        return queryById(entity.getApplicationId(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(Long applicationId) {
        AssetApplication application = requireOwned(applicationId);
        requireEditable(application);
        if (itemMapper.selectCount(Wrappers.<AssetApplicationItem>lambdaQuery()
            .eq(AssetApplicationItem::getApplicationId, applicationId)) == 0) {
            throw new ServiceException("申请明细不能为空");
        }

        StartProcessDTO startProcess = new StartProcessDTO();
        startProcess.setBusinessId(applicationId.toString());
        startProcess.setFlowCode(FLOW_CODE);
        startProcess.getVariables().put("applicationType", application.getApplicationType());
        startProcess.getVariables().put("totalAmount", application.getTotalAmount());
        FlowInstanceBizExtDTO bizExt = new FlowInstanceBizExtDTO();
        bizExt.setBusinessCode(application.getApplicationNo());
        bizExt.setBusinessTitle(application.getTitle());
        startProcess.setBizExt(bizExt);
        if (!workflowService.startCompleteTask(startProcess)) {
            throw new ServiceException("资产申请流程发起失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> applicationIds) {
        for (Long id : applicationIds) {
            AssetApplication entity = requireOwned(id);
            requireEditable(entity);
        }
        itemMapper.delete(Wrappers.<AssetApplicationItem>lambdaQuery()
            .in(AssetApplicationItem::getApplicationId, applicationIds));
        return baseMapper.deleteByIds(applicationIds) > 0;
    }

    @EventListener(condition = "#processEvent.flowCode == 'asset_apply'")
    public void processHandler(ProcessEvent processEvent) {
        AssetApplication application = baseMapper.selectById(Convert.toLong(processEvent.getBusinessId()));
        if (application == null) {
            log.warn("未找到流程对应的资产申请，businessId={}", processEvent.getBusinessId());
            return;
        }
        application.setStatus(Boolean.TRUE.equals(processEvent.getSubmit())
            ? BusinessStatusEnum.WAITING.getStatus() : processEvent.getStatus());
        baseMapper.updateById(application);
        issueOrderService.createFromApprovedApplication(application);
        returnOrderService.createFromApprovedApplication(application);
    }

    private AssetApplication requireOwned(Long applicationId) {
        AssetApplication entity = baseMapper.selectOne(Wrappers.<AssetApplication>lambdaQuery()
            .eq(AssetApplication::getApplicationId, applicationId)
            .eq(AssetApplication::getApplicantId, LoginHelper.getUserId()));
        if (entity == null) {
            throw new ServiceException("资产申请不存在或无权操作");
        }
        return entity;
    }

    private AssetApplication requireReadable(Long applicationId, Long taskId) {
        AssetApplication entity = baseMapper.selectById(applicationId);
        Long userId = LoginHelper.getUserId();
        boolean administrator = LoginHelper.isSuperAdmin(userId) || LoginHelper.isTenantAdmin();
        boolean owner = entity != null && userId != null && userId.equals(entity.getApplicantId());
        boolean workflowParticipant = entity != null && taskId != null
            && workflowService.canViewBusinessByTask(taskId, applicationId.toString(), userId);
        if (entity == null || (!administrator && !owner && !workflowParticipant)) {
            throw new ServiceException("资产申请不存在或无权查看");
        }
        return entity;
    }

    void requireEditable(AssetApplication entity) {
        if (!EDITABLE_STATUSES.contains(entity.getStatus())) {
            throw new ServiceException("当前申请状态不允许修改、删除或重新提交");
        }
    }

    void validateItems(AssetApplicationBo bo) {
        boolean assetRequired = !"purchase".equals(bo.getApplicationType());
        Map<Long, AssetInfo> assets = new HashMap<>();
        if (assetRequired) {
            Set<Long> assetIds = new HashSet<>();
            for (AssetApplicationItemBo item : bo.getItems()) {
                if (item.getAssetId() == null) {
                    throw new ServiceException("领用、调拨、归还和报废申请必须选择已有资产");
                }
                if (!assetIds.add(item.getAssetId())) {
                    throw new ServiceException("同一资产不能在一张申请中重复选择");
                }
            }
            assetInfoMapper.selectByIds(assetIds).forEach(asset -> assets.put(asset.getAssetId(), asset));
            if (assets.size() != assetIds.size()) {
                throw new ServiceException("申请明细包含不存在或无权访问的资产");
            }
            if ("use".equals(bo.getApplicationType())
                && assets.values().stream().anyMatch(asset -> !"0".equals(asset.getAssetStatus()))) {
                throw new ServiceException("领用申请只能选择库存状态的资产");
            }
            if ("return".equals(bo.getApplicationType())) {
                Long applicantId = LoginHelper.getUserId();
                if (assets.values().stream().anyMatch(asset -> !"1".equals(asset.getAssetStatus()))) {
                    throw new ServiceException("归还申请只能选择使用中状态的资产");
                }
                if (assets.values().stream().anyMatch(asset -> !applicantId.equals(asset.getKeeperId()))) {
                    throw new ServiceException("只能归还当前申请人保管的资产");
                }
            }
        }
        Set<Long> categoryIds = new HashSet<>();
        for (AssetApplicationItemBo item : bo.getItems()) {
            if (assetRequired) {
                AssetInfo asset = assets.get(item.getAssetId());
                item.setCategoryId(asset.getCategoryId());
                item.setItemName(asset.getAssetName());
                item.setSpecification(asset.getSpecification());
                item.setQuantity(1);
                item.setUnit(asset.getUnit());
                item.setEstimatedUnitPrice(asset.getOriginalValue());
            } else {
                item.setAssetId(null);
            }
            categoryIds.add(item.getCategoryId());
        }
        Map<Long, AssetCategory> categories = new HashMap<>();
        categoryMapper.selectByIds(categoryIds).forEach(category -> categories.put(category.getCategoryId(), category));
        if (categories.size() != categoryIds.size()) {
            throw new ServiceException("申请明细包含不存在的资产分类");
        }
        if (categories.values().stream().anyMatch(category -> !"0".equals(category.getStatus()))) {
            throw new ServiceException("申请明细包含已停用的资产分类");
        }
    }

    private void saveItems(Long applicationId, List<AssetApplicationItemBo> items) {
        for (AssetApplicationItemBo itemBo : items) {
            AssetApplicationItem item = MapstructUtils.convert(itemBo, AssetApplicationItem.class);
            item.setItemId(null);
            item.setApplicationId(applicationId);
            item.setUnit(StringUtils.isBlank(item.getUnit()) ? "台" : item.getUnit());
            item.setEstimatedUnitPrice(item.getEstimatedUnitPrice() == null ? BigDecimal.ZERO : item.getEstimatedUnitPrice());
            itemMapper.insert(item);
        }
    }

    BigDecimal calculateTotal(List<AssetApplicationItemBo> items) {
        return items.stream()
            .map(item -> (item.getEstimatedUnitPrice() == null ? BigDecimal.ZERO : item.getEstimatedUnitPrice())
                .multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void fillItems(AssetApplicationVo vo) {
        List<AssetApplicationItemVo> items = itemMapper.selectVoList(Wrappers.<AssetApplicationItem>lambdaQuery()
            .eq(AssetApplicationItem::getApplicationId, vo.getApplicationId())
            .orderByAsc(AssetApplicationItem::getCreateTime)
            .orderByAsc(AssetApplicationItem::getItemId));
        Set<Long> categoryIds = new HashSet<>();
        items.forEach(item -> categoryIds.add(item.getCategoryId()));
        Map<Long, String> names = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            categoryMapper.selectByIds(categoryIds).forEach(category -> names.put(category.getCategoryId(), category.getCategoryName()));
        }
        items.forEach(item -> item.setCategoryName(names.get(item.getCategoryId())));
        vo.setItems(items);
    }

    private String generateApplicationNo() {
        String suffix = IdUtil.getSnowflakeNextIdStr();
        return "ZCSQ" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + suffix.substring(suffix.length() - 4);
    }
}
