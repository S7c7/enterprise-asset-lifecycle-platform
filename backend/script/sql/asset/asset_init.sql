-- 企业资产全生命周期管理平台（第一阶段）
-- 适用数据库：MySQL 8.x
-- 说明：本脚本可重复执行；不会删除现有业务数据。

CREATE TABLE IF NOT EXISTS asset_category (
    category_id         BIGINT(20)    NOT NULL                    COMMENT '资产分类ID',
    tenant_id           VARCHAR(20)   DEFAULT '000000'            COMMENT '租户编号',
    parent_id           BIGINT(20)    DEFAULT 0                   COMMENT '上级分类ID，0表示根分类',
    category_code       VARCHAR(32)   NOT NULL                    COMMENT '分类编码',
    category_name       VARCHAR(100)  NOT NULL                    COMMENT '分类名称',
    depreciation_years  INT(4)        DEFAULT 0                   COMMENT '默认折旧年限（年），0表示不设置',
    order_num           INT(4)        DEFAULT 0                   COMMENT '显示顺序',
    status              CHAR(1)       DEFAULT '0'                 COMMENT '状态（0正常 1停用）',
    create_dept         BIGINT(20)    DEFAULT NULL                COMMENT '创建部门',
    create_by           BIGINT(20)    DEFAULT NULL                COMMENT '创建者',
    create_time         DATETIME      DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    update_by           BIGINT(20)    DEFAULT NULL                COMMENT '更新者',
    update_time         DATETIME      DEFAULT NULL                COMMENT '更新时间',
    remark              VARCHAR(500)  DEFAULT NULL                COMMENT '备注',
    del_flag            BIGINT(20)    DEFAULT 0                   COMMENT '删除标志',
    PRIMARY KEY (category_id),
    KEY idx_asset_category_parent (tenant_id, parent_id),
    KEY idx_asset_category_code (tenant_id, category_code, del_flag)
) ENGINE=InnoDB COMMENT='资产分类';

CREATE TABLE IF NOT EXISTS asset_info (
    asset_id             BIGINT(20)     NOT NULL                   COMMENT '资产ID',
    tenant_id            VARCHAR(20)    DEFAULT '000000'           COMMENT '租户编号',
    asset_code           VARCHAR(64)    NOT NULL                   COMMENT '资产编码',
    asset_name           VARCHAR(200)   NOT NULL                   COMMENT '资产名称',
    category_id          BIGINT(20)     NOT NULL                   COMMENT '资产分类ID',
    specification        VARCHAR(200)   DEFAULT NULL               COMMENT '规格型号',
    brand                VARCHAR(100)   DEFAULT NULL               COMMENT '品牌',
    unit                 VARCHAR(20)    DEFAULT '台'               COMMENT '计量单位',
    purchase_date        DATE           DEFAULT NULL               COMMENT '购置日期',
    original_value       DECIMAL(18,2)  DEFAULT 0.00               COMMENT '资产原值',
    asset_status         CHAR(1)        DEFAULT '0'                COMMENT '资产状态（0库存 1使用中 2维修中 3调拨中 4已报废）',
    dept_id              BIGINT(20)     DEFAULT NULL               COMMENT '使用部门ID',
    keeper_id            BIGINT(20)     DEFAULT NULL               COMMENT '保管人ID',
    location             VARCHAR(200)   DEFAULT NULL               COMMENT '存放地点',
    warranty_expiry_date DATE           DEFAULT NULL               COMMENT '保修到期日',
    create_dept          BIGINT(20)     DEFAULT NULL               COMMENT '创建部门',
    create_by            BIGINT(20)     DEFAULT NULL               COMMENT '创建者',
    create_time          DATETIME       DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_by            BIGINT(20)     DEFAULT NULL               COMMENT '更新者',
    update_time          DATETIME       DEFAULT NULL               COMMENT '更新时间',
    remark               VARCHAR(500)   DEFAULT NULL               COMMENT '备注',
    del_flag             BIGINT(20)     DEFAULT 0                  COMMENT '删除标志',
    PRIMARY KEY (asset_id),
    KEY idx_asset_info_code (tenant_id, asset_code, del_flag),
    KEY idx_asset_info_category (tenant_id, category_id),
    KEY idx_asset_info_dept (tenant_id, dept_id),
    KEY idx_asset_info_keeper (tenant_id, keeper_id),
    KEY idx_asset_info_status (tenant_id, asset_status)
) ENGINE=InnoDB COMMENT='资产台账';

CREATE TABLE IF NOT EXISTS asset_application (
    application_id      BIGINT(20)     NOT NULL                   COMMENT '申请ID',
    tenant_id           VARCHAR(20)    DEFAULT '000000'           COMMENT '租户编号',
    application_no      VARCHAR(40)    NOT NULL                   COMMENT '申请单号',
    application_type    VARCHAR(20)    NOT NULL                   COMMENT '申请类型（purchase采购 use领用 transfer调拨 return归还 scrap报废）',
    title               VARCHAR(200)   NOT NULL                   COMMENT '申请标题',
    applicant_id        BIGINT(20)     NOT NULL                   COMMENT '申请人ID',
    apply_dept_id       BIGINT(20)     NOT NULL                   COMMENT '申请部门ID',
    total_amount        DECIMAL(18,2)  DEFAULT 0.00               COMMENT '预估总金额',
    expected_date       DATE           DEFAULT NULL               COMMENT '期望完成日期',
    reason              VARCHAR(1000)  NOT NULL                   COMMENT '申请原因',
    status              VARCHAR(20)    DEFAULT 'draft'            COMMENT '业务状态（draft/waiting/finish/back/cancel等）',
    flow_code           VARCHAR(40)    DEFAULT 'asset_apply'      COMMENT '流程编码',
    create_dept         BIGINT(20)     DEFAULT NULL               COMMENT '创建部门',
    create_by           BIGINT(20)     DEFAULT NULL               COMMENT '创建者',
    create_time         DATETIME       DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_by           BIGINT(20)     DEFAULT NULL               COMMENT '更新者',
    update_time         DATETIME       DEFAULT NULL               COMMENT '更新时间',
    remark              VARCHAR(500)   DEFAULT NULL               COMMENT '备注',
    del_flag            BIGINT(20)     DEFAULT 0                  COMMENT '删除标志',
    PRIMARY KEY (application_id),
    KEY idx_asset_application_no (tenant_id, application_no, del_flag),
    KEY idx_asset_application_applicant (tenant_id, applicant_id, status),
    KEY idx_asset_application_dept (tenant_id, apply_dept_id)
) ENGINE=InnoDB COMMENT='资产申请';

CREATE TABLE IF NOT EXISTS asset_application_item (
    item_id              BIGINT(20)     NOT NULL                   COMMENT '申请明细ID',
    tenant_id            VARCHAR(20)    DEFAULT '000000'           COMMENT '租户编号',
    application_id       BIGINT(20)     NOT NULL                   COMMENT '申请ID',
    asset_id             BIGINT(20)     DEFAULT NULL               COMMENT '已有资产ID，采购申请可为空',
    category_id          BIGINT(20)     NOT NULL                   COMMENT '资产分类ID',
    item_name            VARCHAR(200)   NOT NULL                   COMMENT '资产或物品名称',
    specification        VARCHAR(200)   DEFAULT NULL               COMMENT '规格型号',
    quantity             INT(11)        NOT NULL DEFAULT 1         COMMENT '数量',
    unit                 VARCHAR(20)    DEFAULT '台'               COMMENT '计量单位',
    estimated_unit_price DECIMAL(18,2)  DEFAULT 0.00               COMMENT '预估单价',
    target_dept_id       BIGINT(20)     DEFAULT NULL               COMMENT '目标部门ID',
    target_location      VARCHAR(200)   DEFAULT NULL               COMMENT '目标地点',
    create_dept          BIGINT(20)     DEFAULT NULL               COMMENT '创建部门',
    create_by            BIGINT(20)     DEFAULT NULL               COMMENT '创建者',
    create_time          DATETIME       DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_by            BIGINT(20)     DEFAULT NULL               COMMENT '更新者',
    update_time          DATETIME       DEFAULT NULL               COMMENT '更新时间',
    remark               VARCHAR(500)   DEFAULT NULL               COMMENT '备注',
    del_flag             BIGINT(20)     DEFAULT 0                  COMMENT '删除标志',
    PRIMARY KEY (item_id),
    KEY idx_asset_application_item_main (tenant_id, application_id),
    KEY idx_asset_application_item_asset (tenant_id, asset_id),
    KEY idx_asset_application_item_category (tenant_id, category_id)
) ENGINE=InnoDB COMMENT='资产申请明细';

-- 初始分类（应用层同时执行租户内编码唯一性校验）
INSERT INTO asset_category
    (category_id, tenant_id, parent_id, category_code, category_name, depreciation_years, order_num, status, create_dept, create_by, create_time, remark, del_flag)
SELECT 1900000000000000001, '000000', 0, 'IT_DEVICE', 'IT设备', 5, 1, '0', 103, 1, NOW(), '系统初始化分类', 0
WHERE NOT EXISTS (
    SELECT 1 FROM asset_category WHERE tenant_id = '000000' AND category_code = 'IT_DEVICE' AND del_flag = 0
);

INSERT INTO asset_category
    (category_id, tenant_id, parent_id, category_code, category_name, depreciation_years, order_num, status, create_dept, create_by, create_time, remark, del_flag)
SELECT 1900000000000000002, '000000', 0, 'OFFICE_EQUIPMENT', '办公设备', 5, 2, '0', 103, 1, NOW(), '系统初始化分类', 0
WHERE NOT EXISTS (
    SELECT 1 FROM asset_category WHERE tenant_id = '000000' AND category_code = 'OFFICE_EQUIPMENT' AND del_flag = 0
);

INSERT INTO asset_category
    (category_id, tenant_id, parent_id, category_code, category_name, depreciation_years, order_num, status, create_dept, create_by, create_time, remark, del_flag)
SELECT 1900000000000000003, '000000', 0, 'PRODUCTION_EQUIPMENT', '生产设备', 10, 3, '0', 103, 1, NOW(), '系统初始化分类', 0
WHERE NOT EXISTS (
    SELECT 1 FROM asset_category WHERE tenant_id = '000000' AND category_code = 'PRODUCTION_EQUIPMENT' AND del_flag = 0
);

-- 菜单与按钮权限。使用独立 ID 段，避免覆盖框架内置菜单。
INSERT INTO sys_menu VALUES
    (2000, '企业资产管理', 0, 1, 'asset', NULL, '', 1, 0, 'M', '0', '0', '', 'chart', 103, 1, NOW(), NULL, NULL, '企业资产全生命周期管理目录')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), update_time = NOW();

INSERT INTO sys_menu VALUES
    (2001, '资产台账', 2000, 1, 'info', 'asset/info/index', '', 1, 0, 'C', '0', '0', 'asset:info:list', 'list', 103, 1, NOW(), NULL, NULL, '资产台账菜单'),
    (2002, '资产分类', 2000, 2, 'category', 'asset/category/index', '', 1, 0, 'C', '0', '0', 'asset:category:list', 'tree-table', 103, 1, NOW(), NULL, NULL, '资产分类菜单'),
    (2010, '台账查询', 2001, 1, '', '', '', 1, 0, 'F', '0', '0', 'asset:info:query', '#', 103, 1, NOW(), NULL, NULL, ''),
    (2011, '台账新增', 2001, 2, '', '', '', 1, 0, 'F', '0', '0', 'asset:info:add', '#', 103, 1, NOW(), NULL, NULL, ''),
    (2012, '台账修改', 2001, 3, '', '', '', 1, 0, 'F', '0', '0', 'asset:info:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
    (2013, '台账删除', 2001, 4, '', '', '', 1, 0, 'F', '0', '0', 'asset:info:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
    (2020, '分类查询', 2002, 1, '', '', '', 1, 0, 'F', '0', '0', 'asset:category:query', '#', 103, 1, NOW(), NULL, NULL, ''),
    (2021, '分类新增', 2002, 2, '', '', '', 1, 0, 'F', '0', '0', 'asset:category:add', '#', 103, 1, NOW(), NULL, NULL, ''),
    (2022, '分类修改', 2002, 3, '', '', '', 1, 0, 'F', '0', '0', 'asset:category:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
    (2023, '分类删除', 2002, 4, '', '', '', 1, 0, 'F', '0', '0', 'asset:category:remove', '#', 103, 1, NOW(), NULL, NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), perms = VALUES(perms), update_time = NOW();

INSERT INTO sys_menu VALUES
    (2030, '我的申请', 2000, 3, 'application', 'asset/application/index', '', 1, 0, 'C', '0', '0', 'asset:application:list', 'form', 103, 1, NOW(), NULL, NULL, '资产申请菜单'),
    (2031, '申请查询', 2030, 1, '', '', '', 1, 0, 'F', '0', '0', 'asset:application:query', '#', 103, 1, NOW(), NULL, NULL, ''),
    (2032, '申请新增', 2030, 2, '', '', '', 1, 0, 'F', '0', '0', 'asset:application:add', '#', 103, 1, NOW(), NULL, NULL, ''),
    (2033, '申请修改', 2030, 3, '', '', '', 1, 0, 'F', '0', '0', 'asset:application:edit', '#', 103, 1, NOW(), NULL, NULL, ''),
    (2034, '申请删除', 2030, 4, '', '', '', 1, 0, 'F', '0', '0', 'asset:application:remove', '#', 103, 1, NOW(), NULL, NULL, ''),
    (2035, '提交审批', 2030, 5, '', '', '', 1, 0, 'F', '0', '0', 'asset:application:submit', '#', 103, 1, NOW(), NULL, NULL, ''),
    (2036, '资产申请详情', 2000, 99, 'application/detail/index', 'asset/application/detail/index', '', 1, 0, 'C', '1', '0', 'asset:application:query', '#', 103, 1, NOW(), NULL, NULL, '审批与查看共用的隐藏详情路由')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), perms = VALUES(perms), update_time = NOW();

-- 资产审批分类与流程。默认审批人使用管理员角色 role:1，正式使用时应在流程设计器中改为企业实际角色。
INSERT INTO flow_category
    (category_id, tenant_id, parent_id, ancestors, category_name, order_num, del_flag, create_dept, create_by, create_time)
VALUES (110, '000000', 100, '0,100', '资产审批', 2, '0', 103, 1, NOW())
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name), update_time = NOW();

INSERT INTO flow_definition
    (id, flow_code, flow_name, model_value, category, version, is_publish, form_custom, form_path, activity_status, create_time, create_by, del_flag, tenant_id)
VALUES
    (2100, 'asset_apply', '资产申请审批', 'CLASSICS', '110', '1', 1, 'N', '/asset/application/detail/index', 1, NOW(), '1', '0', '000000')
ON DUPLICATE KEY UPDATE flow_name = VALUES(flow_name), is_publish = 1, form_path = VALUES(form_path), activity_status = 1, update_time = NOW();

INSERT INTO flow_node
    (id, node_type, definition_id, node_code, node_name, permission_flag, node_ratio, coordinate, form_custom, version, create_time, create_by, ext, del_flag, tenant_id)
VALUES
    (2101, 0, 2100, 'asset_apply_start', '开始', NULL, '0.000', '200,200|200,200', 'N', '1', NOW(), '1', '[]', '0', '000000'),
    (2102, 1, 2100, 'asset_apply_submit', '申请人提交', '', '0.000', '360,200|360,200', 'N', '1', NOW(), '1', '[{"code":"ButtonPermissionEnum","value":"back,termination,file,copy"}]', '0', '000000'),
    (2103, 1, 2100, 'asset_apply_dept', '部门负责人审批', 'role:1', '0.000', '540,200|540,200', 'N', '1', NOW(), '1', '[{"code":"ButtonPermissionEnum","value":"back,termination,copy,transfer,trust,file"}]', '0', '000000'),
    (2104, 1, 2100, 'asset_apply_manager', '资产管理员审批', 'role:1', '0.000', '720,200|720,200', 'N', '1', NOW(), '1', '[{"code":"ButtonPermissionEnum","value":"back,termination,copy,transfer,trust,file"}]', '0', '000000'),
    (2105, 2, 2100, 'asset_apply_end', '结束', NULL, '0.000', '900,200|900,200', 'N', '1', NOW(), '1', '[]', '0', '000000')
ON DUPLICATE KEY UPDATE node_name = VALUES(node_name), permission_flag = VALUES(permission_flag), ext = VALUES(ext), update_time = NOW();

INSERT INTO flow_skip
    (id, definition_id, now_node_code, now_node_type, next_node_code, next_node_type, skip_type, coordinate, create_time, create_by, del_flag, tenant_id)
VALUES
    (2111, 2100, 'asset_apply_start', 0, 'asset_apply_submit', 1, 'PASS', '220,200;310,200', NOW(), '1', '0', '000000'),
    (2112, 2100, 'asset_apply_submit', 1, 'asset_apply_dept', 1, 'PASS', '410,200;490,200', NOW(), '1', '0', '000000'),
    (2113, 2100, 'asset_apply_dept', 1, 'asset_apply_manager', 1, 'PASS', '590,200;670,200', NOW(), '1', '0', '000000'),
    (2114, 2100, 'asset_apply_manager', 1, 'asset_apply_end', 2, 'PASS', '770,200;880,200', NOW(), '1', '0', '000000')
ON DUPLICATE KEY UPDATE next_node_code = VALUES(next_node_code), skip_type = VALUES(skip_type), update_time = NOW();
