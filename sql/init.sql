-- ============================================================
-- PostgreSQL 建库建表语句
-- 适用数据库：field-mapper 项目
-- ============================================================

-- 创建数据库（如未创建，在 psql 中执行）
-- CREATE DATABASE field_mapper;
-- \c field_mapper;

-- ============================================================
-- 1. 用户表
-- ============================================================
DROP TABLE IF EXISTS APP_USERS CASCADE;

CREATE TABLE APP_USERS (
    ID          BIGSERIAL PRIMARY KEY,
    USERNAME    VARCHAR(50) UNIQUE NOT NULL,
    PASSWORD    VARCHAR(200) NOT NULL,
    ROLE        VARCHAR(20) NOT NULL CHECK (ROLE IN ('ADMIN', 'USER')),
    CREATED_AT  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATED_BY  VARCHAR(50)
);

COMMENT ON TABLE APP_USERS IS '系统用户表';
COMMENT ON COLUMN APP_USERS.USERNAME IS '用户名';
COMMENT ON COLUMN APP_USERS.PASSWORD IS '密码（BCrypt加密）';
COMMENT ON COLUMN APP_USERS.ROLE IS '角色: ADMIN=管理员, USER=普通用户';

-- ============================================================
-- 2. 预算执行情况表
-- ============================================================
DROP TABLE IF EXISTS BUDGET_ITEMS CASCADE;

CREATE TABLE BUDGET_ITEMS (
    ID                 BIGSERIAL PRIMARY KEY,
    DIRECTION          VARCHAR(200),
    PROJECT            VARCHAR(200),
    AREA               VARCHAR(200),
    SPECIFIC_COST      VARCHAR(200),
    MONTH              VARCHAR(20),
    ACTUAL_CUMULATIVE  NUMERIC(14,2) DEFAULT 0,
    BOOK_CUMULATIVE    NUMERIC(14,2) DEFAULT 0,
    DIFFERENCE         NUMERIC(14,2) DEFAULT 0,
    REMARK             VARCHAR(500),
    VERIFIER           VARCHAR(50),
    CREATED_BY         VARCHAR(50),
    CREATED_AT         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE BUDGET_ITEMS IS '预算执行情况表';
COMMENT ON COLUMN BUDGET_ITEMS.DIRECTION IS '去向';
COMMENT ON COLUMN BUDGET_ITEMS.PROJECT IS '项目';
COMMENT ON COLUMN BUDGET_ITEMS.AREA IS '片区名';
COMMENT ON COLUMN BUDGET_ITEMS.SPECIFIC_COST IS '具体费用';
COMMENT ON COLUMN BUDGET_ITEMS.MONTH IS '月份';
COMMENT ON COLUMN BUDGET_ITEMS.ACTUAL_CUMULATIVE IS '实际累计金额';
COMMENT ON COLUMN BUDGET_ITEMS.BOOK_CUMULATIVE IS '当月账面累计金额';
COMMENT ON COLUMN BUDGET_ITEMS.DIFFERENCE IS '当月差异';
COMMENT ON COLUMN BUDGET_ITEMS.REMARK IS '备注/服务期限';
COMMENT ON COLUMN BUDGET_ITEMS.VERIFIER IS '数据核对人';

-- ============================================================
-- 3. 审计日志表
-- ============================================================
DROP TABLE IF EXISTS AUDIT_LOGS CASCADE;

CREATE TABLE AUDIT_LOGS (
    ID           BIGSERIAL PRIMARY KEY,
    TABLE_NAME   VARCHAR(100),
    RECORD_ID    BIGINT,
    ACTION_TYPE  VARCHAR(20),
    FIELD_NAME   VARCHAR(100),
    OLD_VALUE    TEXT,
    NEW_VALUE    TEXT,
    MODIFIED_BY  VARCHAR(50),
    MODIFIED_AT  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE AUDIT_LOGS IS '审计日志表';
COMMENT ON COLUMN AUDIT_LOGS.TABLE_NAME IS '操作的表名';
COMMENT ON COLUMN AUDIT_LOGS.RECORD_ID IS '操作记录ID';
COMMENT ON COLUMN AUDIT_LOGS.ACTION_TYPE IS '操作类型: INSERT/UPDATE/DELETE';
COMMENT ON COLUMN AUDIT_LOGS.FIELD_NAME IS '字段名（ROW_DATA=整行记录）';
COMMENT ON COLUMN AUDIT_LOGS.OLD_VALUE IS '修改前数据（JSON）';
COMMENT ON COLUMN AUDIT_LOGS.NEW_VALUE IS '修改后数据（JSON）';
COMMENT ON COLUMN AUDIT_LOGS.MODIFIED_BY IS '操作人';

-- ============================================================
-- 4. 默认用户（密码均为 123456）
-- BCrypt hash: $2a$10$LuqZzVYF0bzsV1OPJmqyOuZCzr7DKCVws87ZxSf.zmkHk/NXY3Z6q
-- ============================================================
INSERT INTO APP_USERS (USERNAME, PASSWORD, ROLE, CREATED_BY) VALUES
('admin', '$2a$10$LuqZzVYF0bzsV1OPJmqyOuZCzr7DKCVws87ZxSf.zmkHk/NXY3Z6q', 'ADMIN', 'system');

INSERT INTO APP_USERS (USERNAME, PASSWORD, ROLE, CREATED_BY) VALUES
('user1', '$2a$10$LuqZzVYF0bzsV1OPJmqyOuZCzr7DKCVws87ZxSf.zmkHk/NXY3Z6q', 'USER', 'admin');

-- ============================================================
-- 5. 测试表（原 demo 表）
-- ============================================================
DROP TABLE IF EXISTS TEST CASCADE;

CREATE TABLE TEST (
    ID   BIGSERIAL PRIMARY KEY,
    RQ   DATE NOT NULL,
    JE   NUMERIC(12,2) NOT NULL,
    SP   VARCHAR(200) NOT NULL,
    SL   NUMERIC(10,2) NOT NULL,
    BZ   VARCHAR(500)
);

INSERT INTO TEST (RQ, JE, SP, SL, BZ) VALUES
('2026-06-01', 128.50, '蓝牙耳机',   3,  '黑色款'),
('2026-06-01', 2599.00, '机械键盘', 1,  '87键青轴'),
('2026-06-02', 89.90,  '鼠标垫',    10, '大号800×300'),
('2026-06-02', 459.00, '移动硬盘',  2,  '2TB USB-C'),
('2026-06-03', 35.50,  'U盘',       5,  '64GB'),
('2026-06-03', 1899.00, '显示器',   1,  '27寸 4K'),
('2026-06-04', 69.00,  '网线',      30, '六类 3米'),
('2026-06-04', 329.00, '无线路由器', 2, 'WiFi6'),
('2026-06-05', 15.00,  '理线器',    20, '魔术贴'),
('2026-06-05', 799.00, '摄像头',    1,  '1080P 云台版');
