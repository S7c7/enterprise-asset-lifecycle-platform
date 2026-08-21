package org.dromara.asset.service.impl;

import org.dromara.asset.domain.AssetApplication;
import org.dromara.asset.domain.AssetReturnOrder;
import org.dromara.asset.mapper.AssetApplicationItemMapper;
import org.dromara.asset.mapper.AssetApplicationMapper;
import org.dromara.asset.mapper.AssetHistoryMapper;
import org.dromara.asset.mapper.AssetInfoMapper;
import org.dromara.asset.mapper.AssetReturnOrderMapper;
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
@DisplayName("归还执行单核心规则")
class AssetReturnOrderServiceImplTest {

    @Mock
    private AssetReturnOrderMapper returnOrderMapper;
    @Mock
    private AssetApplicationMapper applicationMapper;
    @Mock
    private AssetApplicationItemMapper applicationItemMapper;
    @Mock
    private AssetInfoMapper assetInfoMapper;
    @Mock
    private AssetHistoryMapper historyMapper;

    private AssetReturnOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AssetReturnOrderServiceImpl(
            returnOrderMapper, applicationMapper, applicationItemMapper, assetInfoMapper, historyMapper);
    }

    @Test
    @DisplayName("审批完成的归还申请自动生成待执行单")
    void approvedReturnApplicationCreatesPendingOrder() {
        AssetApplication application = approvedApplication("return");

        service.createFromApprovedApplication(application);

        ArgumentCaptor<AssetReturnOrder> captor = ArgumentCaptor.forClass(AssetReturnOrder.class);
        verify(returnOrderMapper).insert(captor.capture());
        AssetReturnOrder order = captor.getValue();
        assertEquals(application.getApplicationId(), order.getApplicationId());
        assertEquals(application.getApplicantId(), order.getReturnerId());
        assertEquals(AssetReturnOrderServiceImpl.STATUS_PENDING, order.getStatus());
        assertTrue(order.getReturnNo().startsWith("ZCGH"));
    }

    @Test
    @DisplayName("非归还或未完成审批的申请不生成执行单")
    void unsupportedApplicationDoesNotCreateOrder() {
        service.createFromApprovedApplication(approvedApplication("use"));
        AssetApplication waiting = approvedApplication("return");
        waiting.setStatus(BusinessStatusEnum.WAITING.getStatus());
        service.createFromApprovedApplication(waiting);

        verify(returnOrderMapper, never()).insert(any(AssetReturnOrder.class));
    }

    @Test
    @DisplayName("同一申请只能生成一张归还执行单")
    void existingOrderMakesCreationIdempotent() {
        when(returnOrderMapper.exists(any())).thenReturn(true);

        service.createFromApprovedApplication(approvedApplication("return"));

        verify(returnOrderMapper, never()).insert(any(AssetReturnOrder.class));
    }

    @Test
    @DisplayName("已完成的归还执行单不能重复执行")
    void completedOrderCannotExecuteAgain() {
        AssetReturnOrder order = new AssetReturnOrder();
        order.setStatus(AssetReturnOrderServiceImpl.STATUS_COMPLETED);

        ServiceException error = assertThrows(ServiceException.class, () -> service.requirePending(order));

        assertTrue(error.getMessage().contains("不能重复执行"));
    }

    @Test
    @DisplayName("来源申请未审批完成时拒绝改变资产状态")
    void unapprovedSourceCannotBeExecuted() {
        AssetReturnOrder order = new AssetReturnOrder();
        order.setReturnId(3L);
        order.setApplicationId(8L);
        order.setStatus(AssetReturnOrderServiceImpl.STATUS_PENDING);
        when(returnOrderMapper.selectById(3L)).thenReturn(order);
        AssetApplication waiting = approvedApplication("return");
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
