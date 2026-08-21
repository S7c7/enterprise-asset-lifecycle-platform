package org.dromara.asset.service.impl;

import org.dromara.asset.domain.AssetApplication;
import org.dromara.asset.domain.AssetIssueOrder;
import org.dromara.asset.mapper.AssetApplicationItemMapper;
import org.dromara.asset.mapper.AssetApplicationMapper;
import org.dromara.asset.mapper.AssetHistoryMapper;
import org.dromara.asset.mapper.AssetInfoMapper;
import org.dromara.asset.mapper.AssetIssueOrderMapper;
import org.dromara.common.core.enums.BusinessStatusEnum;
import org.dromara.common.core.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
@DisplayName("领用执行单核心规则")
class AssetIssueOrderServiceImplTest {

    @Mock
    private AssetIssueOrderMapper issueOrderMapper;
    @Mock
    private AssetApplicationMapper applicationMapper;
    @Mock
    private AssetApplicationItemMapper applicationItemMapper;
    @Mock
    private AssetInfoMapper assetInfoMapper;
    @Mock
    private AssetHistoryMapper historyMapper;

    private AssetIssueOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AssetIssueOrderServiceImpl(
            issueOrderMapper, applicationMapper, applicationItemMapper, assetInfoMapper, historyMapper);
    }

    @Test
    @DisplayName("审批完成的领用申请自动生成待执行单")
    void approvedUseApplicationCreatesPendingOrder() {
        AssetApplication application = approvedApplication("use");

        service.createFromApprovedApplication(application);

        ArgumentCaptor<AssetIssueOrder> captor = ArgumentCaptor.forClass(AssetIssueOrder.class);
        verify(issueOrderMapper).insert(captor.capture());
        AssetIssueOrder order = captor.getValue();
        assertEquals(application.getApplicationId(), order.getApplicationId());
        assertEquals(application.getApplicantId(), order.getRecipientId());
        assertEquals(AssetIssueOrderServiceImpl.STATUS_PENDING, order.getStatus());
        assertTrue(order.getIssueNo().startsWith("ZCLY"));
    }

    @Test
    @DisplayName("非领用或未完成审批的申请不生成执行单")
    void unsupportedApplicationDoesNotCreateOrder() {
        AssetApplication purchase = approvedApplication("purchase");
        service.createFromApprovedApplication(purchase);
        AssetApplication waiting = approvedApplication("use");
        waiting.setStatus(BusinessStatusEnum.WAITING.getStatus());
        service.createFromApprovedApplication(waiting);

        verify(issueOrderMapper, never()).insert(any(AssetIssueOrder.class));
    }

    @Test
    @DisplayName("同一申请只能生成一张执行单")
    void existingOrderMakesCreationIdempotent() {
        when(issueOrderMapper.exists(any())).thenReturn(true);

        service.createFromApprovedApplication(approvedApplication("use"));

        verify(issueOrderMapper, never()).insert(any(AssetIssueOrder.class));
    }

    @Test
    @DisplayName("已完成的执行单不能重复执行")
    void completedOrderCannotExecuteAgain() {
        AssetIssueOrder order = new AssetIssueOrder();
        order.setStatus(AssetIssueOrderServiceImpl.STATUS_COMPLETED);

        ServiceException error = assertThrows(ServiceException.class, () -> service.requirePending(order));

        assertTrue(error.getMessage().contains("不能重复执行"));
    }

    @Test
    @DisplayName("来源申请未审批完成时拒绝改变资产状态")
    void unapprovedSourceCannotBeExecuted() {
        AssetIssueOrder order = new AssetIssueOrder();
        order.setIssueId(3L);
        order.setApplicationId(8L);
        order.setStatus(AssetIssueOrderServiceImpl.STATUS_PENDING);
        when(issueOrderMapper.selectById(3L)).thenReturn(order);
        AssetApplication waiting = approvedApplication("use");
        waiting.setStatus(BusinessStatusEnum.WAITING.getStatus());
        when(applicationMapper.selectById(8L)).thenReturn(waiting);

        ServiceException error = assertThrows(ServiceException.class, () -> service.execute(3L));

        assertTrue(error.getMessage().contains("未审批完成"));
        verify(assetInfoMapper, never()).updateLifecycleState(any(), any());
    }

    private AssetApplication approvedApplication(String type) {
        AssetApplication application = new AssetApplication();
        application.setApplicationId(8L);
        application.setApplicationType(type);
        application.setApplicantId(10L);
        application.setApplyDeptId(20L);
        application.setStatus(BusinessStatusEnum.FINISH.getStatus());
        return application;
    }
}
