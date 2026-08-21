package org.dromara.asset.service.impl;

import org.dromara.asset.domain.AssetApplication;
import org.dromara.asset.domain.bo.AssetApplicationBo;
import org.dromara.asset.domain.bo.AssetApplicationItemBo;
import org.dromara.asset.mapper.AssetApplicationItemMapper;
import org.dromara.asset.mapper.AssetApplicationMapper;
import org.dromara.asset.mapper.AssetCategoryMapper;
import org.dromara.asset.mapper.AssetInfoMapper;
import org.dromara.asset.service.IAssetIssueOrderService;
import org.dromara.common.core.domain.event.ProcessEvent;
import org.dromara.common.core.enums.BusinessStatusEnum;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.WorkflowService;
import org.dromara.common.satoken.utils.LoginHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("dev")
@DisplayName("资产申请权限与核心规则")
class AssetApplicationServiceImplTest {

    @Mock
    private AssetApplicationMapper applicationMapper;
    @Mock
    private AssetApplicationItemMapper itemMapper;
    @Mock
    private AssetCategoryMapper categoryMapper;
    @Mock
    private AssetInfoMapper assetInfoMapper;
    @Mock
    private WorkflowService workflowService;
    @Mock
    private IAssetIssueOrderService issueOrderService;

    private AssetApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AssetApplicationServiceImpl(
            applicationMapper, itemMapper, categoryMapper, assetInfoMapper, workflowService, issueOrderService);
    }

    @Test
    @DisplayName("申请人可以读取自己的申请")
    void ownerCanReadApplication() {
        AssetApplication application = application(10L);
        when(applicationMapper.selectById(1L)).thenReturn(application);
        try (MockedStatic<LoginHelper> login = loginAs(10L, false)) {
            service.queryById(1L, null);
        }
    }

    @Test
    @DisplayName("普通用户不能读取他人的申请")
    void otherUserCannotReadApplication() {
        when(applicationMapper.selectById(1L)).thenReturn(application(10L));
        try (MockedStatic<LoginHelper> login = loginAs(20L, false)) {
            ServiceException error = assertThrows(ServiceException.class, () -> service.queryById(1L, null));
            assertEquals("资产申请不存在或无权查看", error.getMessage());
        }
    }

    @Test
    @DisplayName("当前审批人携带匹配任务可以读取申请")
    void workflowParticipantCanReadApplication() {
        when(applicationMapper.selectById(1L)).thenReturn(application(10L));
        when(workflowService.canViewBusinessByTask(99L, "1", 20L)).thenReturn(true);
        try (MockedStatic<LoginHelper> login = loginAs(20L, false)) {
            service.queryById(1L, 99L);
        }
    }

    @Test
    @DisplayName("伪造或不匹配的流程任务不能绕过所有权")
    void unrelatedTaskCannotBypassOwnership() {
        when(applicationMapper.selectById(1L)).thenReturn(application(10L));
        when(workflowService.canViewBusinessByTask(99L, "1", 20L)).thenReturn(false);
        try (MockedStatic<LoginHelper> login = loginAs(20L, false)) {
            assertThrows(ServiceException.class, () -> service.queryById(1L, 99L));
        }
    }

    @Test
    @DisplayName("超级管理员可以执行审计读取")
    void superAdminCanReadApplication() {
        when(applicationMapper.selectById(1L)).thenReturn(application(10L));
        try (MockedStatic<LoginHelper> login = loginAs(1L, true)) {
            service.queryById(1L, null);
        }
    }

    @Test
    @DisplayName("申请总金额由服务端按数量和单价重算")
    void totalAmountIsCalculatedOnServer() {
        AssetApplicationItemBo first = item(2, "1999.50");
        AssetApplicationItemBo second = item(3, "100.00");
        assertEquals(new BigDecimal("4299.00"), service.calculateTotal(List.of(first, second)));
    }

    @Test
    @DisplayName("同一资产不能在一张申请中重复选择")
    void duplicateAssetIsRejected() {
        AssetApplicationItemBo first = item(1, "10");
        first.setAssetId(8L);
        AssetApplicationItemBo second = item(1, "10");
        second.setAssetId(8L);
        AssetApplicationBo bo = new AssetApplicationBo();
        bo.setApplicationType("use");
        bo.setItems(List.of(first, second));
        ServiceException error = assertThrows(ServiceException.class, () -> service.validateItems(bo));
        assertTrue(error.getMessage().contains("同一资产"));
    }

    @Test
    @DisplayName("审批中的申请不可再次编辑")
    void waitingApplicationIsNotEditable() {
        AssetApplication application = application(10L);
        application.setStatus(BusinessStatusEnum.WAITING.getStatus());
        assertThrows(ServiceException.class, () -> service.requireEditable(application));
    }

    @Test
    @DisplayName("流程结束后更新申请状态并触发领用执行单生成")
    void finishedWorkflowTriggersIssueOrderCreation() {
        AssetApplication application = application(10L);
        application.setApplicationType("use");
        when(applicationMapper.selectById(1L)).thenReturn(application);
        ProcessEvent event = new ProcessEvent();
        event.setBusinessId("1");
        event.setStatus(BusinessStatusEnum.FINISH.getStatus());
        event.setSubmit(false);

        service.processHandler(event);

        assertEquals(BusinessStatusEnum.FINISH.getStatus(), application.getStatus());
        verify(applicationMapper).updateById(application);
        verify(issueOrderService).createFromApprovedApplication(application);
    }

    private MockedStatic<LoginHelper> loginAs(Long userId, boolean superAdmin) {
        MockedStatic<LoginHelper> login = mockStatic(LoginHelper.class);
        login.when(LoginHelper::getUserId).thenReturn(userId);
        login.when(() -> LoginHelper.isSuperAdmin(userId)).thenReturn(superAdmin);
        login.when(LoginHelper::isTenantAdmin).thenReturn(false);
        return login;
    }

    private AssetApplication application(Long applicantId) {
        AssetApplication application = new AssetApplication();
        application.setApplicationId(1L);
        application.setApplicantId(applicantId);
        application.setStatus(BusinessStatusEnum.DRAFT.getStatus());
        return application;
    }

    private AssetApplicationItemBo item(int quantity, String unitPrice) {
        AssetApplicationItemBo item = new AssetApplicationItemBo();
        item.setQuantity(quantity);
        item.setEstimatedUnitPrice(new BigDecimal(unitPrice));
        return item;
    }
}
