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
