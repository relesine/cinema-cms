-- =============================================================================
-- Cinema Management System 电影院管理系统（星光国际影城）建库建表 + 初始数据脚本
-- MySQL 8.0 （phpStudy Pro 内置 8.0.12 已验证） / 存储引擎 InnoDB / 字符集 utf8mb4
-- 通用字段规范：create_time 创建时间、update_time 更新时间、is_deleted 逻辑删除(0正常1删除)
-- 导入方式：Navicat 打开本文件执行，或命令行：
--   mysql -uroot -p123456 < cms.sql
-- 初始账号密码列以 PLAIN:明文 存储，后端首次启动时由 DataInitializer 自动转成 BCrypt 密文
-- =============================================================================
DROP DATABASE IF EXISTS cms;
CREATE DATABASE cms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE cms;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- 一、权限相关：角色表 / 用户表 / 用户角色关联表 / 菜单权限表
-- =============================================================================
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  role_code    VARCHAR(50)  NOT NULL COMMENT '角色编码：ROLE_USER普通用户 ROLE_STAFF员工 ROLE_ADMIN管理员',
  role_name    VARCHAR(50)  NOT NULL COMMENT '角色名称',
  description  VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  username     VARCHAR(50)  NOT NULL COMMENT '登录账号',
  password     VARCHAR(100) NOT NULL COMMENT '密码（BCrypt密文；PLAIN:开头为待加密明文）',
  nickname     VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
  real_name    VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
  phone        VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  email        VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  avatar       VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  gender       TINYINT      NOT NULL DEFAULT 0 COMMENT '性别：0未知 1男 2女',
  status       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
  remark       VARCHAR(200) DEFAULT NULL COMMENT '备注',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表（统一存放用户/员工/管理员）';

DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
  id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id),
  KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表（多对多）';

DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父菜单ID，0为顶级',
  menu_name   VARCHAR(50)  NOT NULL COMMENT '菜单/权限名称',
  menu_type   CHAR(1)      NOT NULL DEFAULT 'C' COMMENT '类型：M目录 C菜单 F按钮',
  path        VARCHAR(100) DEFAULT NULL COMMENT '前端路由路径',
  perms       VARCHAR(100) DEFAULT NULL COMMENT '权限标识，如 movie:add',
  icon        VARCHAR(50)  DEFAULT NULL COMMENT '菜单图标',
  sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1显示 0隐藏',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单/权限表（按钮级权限底座）';

-- =============================================================================
-- 二、影院基础数据：影厅表 / 座位表
-- =============================================================================
DROP TABLE IF EXISTS biz_hall;
CREATE TABLE biz_hall (
  id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  name        VARCHAR(50) NOT NULL COMMENT '影厅名称，如1号厅',
  hall_type   TINYINT     NOT NULL DEFAULT 1 COMMENT '影厅类型：1普通厅 2巨幕厅 3VIP厅 4儿童厅',
  row_count   INT         NOT NULL COMMENT '座位总行数',
  col_count   INT         NOT NULL COMMENT '每行座位列数',
  seat_count  INT         NOT NULL COMMENT '总座位数',
  status      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted  TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='影厅表';

DROP TABLE IF EXISTS biz_seat;
CREATE TABLE biz_seat (
  id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  hall_id     BIGINT      NOT NULL COMMENT '所属影厅ID',
  row_no      INT         NOT NULL COMMENT '排号（从1开始）',
  col_no      INT         NOT NULL COMMENT '列号（从1开始）',
  seat_name   VARCHAR(20) NOT NULL COMMENT '座位名，如5排8座',
  seat_type   TINYINT     NOT NULL DEFAULT 0 COMMENT '座位类型：0普通 1优选 2VIP 3亲子座',
  status      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1正常 0维修不可售',
  create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted  TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_hall_row_col (hall_id, row_no, col_no),
  KEY idx_hall (hall_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位表';

-- =============================================================================
-- 三、电影表 / 排片场次表
-- =============================================================================
DROP TABLE IF EXISTS biz_movie;
CREATE TABLE biz_movie (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  name         VARCHAR(100) NOT NULL COMMENT '电影中文名',
  name_en      VARCHAR(150) DEFAULT NULL COMMENT '英文/又名',
  cover        VARCHAR(255) DEFAULT NULL COMMENT '海报URL',
  director     VARCHAR(100) DEFAULT NULL COMMENT '导演',
  actors       VARCHAR(255) DEFAULT NULL COMMENT '主演（逗号分隔）',
  genre        VARCHAR(100) DEFAULT NULL COMMENT '类型（剧情/科幻等，逗号分隔）',
  duration     INT          NOT NULL DEFAULT 120 COMMENT '片长（分钟）',
  release_date DATE         DEFAULT NULL COMMENT '上映日期',
  region       VARCHAR(50)  DEFAULT NULL COMMENT '制片地区',
  language     VARCHAR(50)  DEFAULT NULL COMMENT '语言',
  synopsis     TEXT         COMMENT '剧情简介',
  score        DECIMAL(3,1) NOT NULL DEFAULT 0.0 COMMENT '评分，满分10',
  movie_status TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1热映 0待映 2下架',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_status (movie_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电影表';

DROP TABLE IF EXISTS biz_schedule;
CREATE TABLE biz_schedule (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  movie_id    BIGINT        NOT NULL COMMENT '电影ID',
  hall_id     BIGINT        NOT NULL COMMENT '影厅ID',
  start_time  DATETIME      NOT NULL COMMENT '开场时间',
  end_time    DATETIME      NOT NULL COMMENT '散场时间=开场+片长',
  price       DECIMAL(10,2) NOT NULL COMMENT '基础票价（元）',
  version_lang VARCHAR(20)  NOT NULL DEFAULT '国语2D' COMMENT '放映版本：国语2D/IMAX3D等',
  status      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1正常 0取消场次',
  version     INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号（防并发改排片）',
  create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted  TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  KEY idx_movie (movie_id),
  KEY idx_hall_time (hall_id, start_time),
  KEY idx_start (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排片场次表';

-- =============================================================================
-- 四、会员：会员等级表 / 会员表
-- =============================================================================
DROP TABLE IF EXISTS biz_member_level;
CREATE TABLE biz_member_level (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  level_name  VARCHAR(50)   NOT NULL COMMENT '等级名称',
  min_points  INT           NOT NULL DEFAULT 0 COMMENT '升级所需积分下限',
  discount    DECIMAL(3,2)  NOT NULL DEFAULT 1.00 COMMENT '购票折扣：1.00原价 0.90九折',
  description VARCHAR(200)  DEFAULT NULL COMMENT '等级权益说明',
  create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';

DROP TABLE IF EXISTS biz_member;
CREATE TABLE biz_member (
  id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id       BIGINT        NOT NULL COMMENT '所属用户ID',
  level_id      BIGINT        NOT NULL DEFAULT 1 COMMENT '会员等级ID',
  points        INT           NOT NULL DEFAULT 0 COMMENT '当前积分（消费1元=1积分）',
  balance       DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '会员卡余额（模拟，元）',
  total_consume DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费金额',
  open_time     DATETIME      DEFAULT NULL COMMENT '开卡时间',
  status        TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1有效 0冻结',
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- =============================================================================
-- 五、交易：订单表 / 订单座位明细表
-- =============================================================================
DROP TABLE IF EXISTS biz_order;
CREATE TABLE biz_order (
  id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  order_no        VARCHAR(32)   NOT NULL COMMENT '订单编号（业务唯一）',
  user_id         BIGINT        NOT NULL COMMENT '下单用户ID',
  schedule_id     BIGINT        NOT NULL COMMENT '场次ID',
  movie_id        BIGINT        NOT NULL COMMENT '电影ID（冗余便于统计）',
  hall_id         BIGINT        NOT NULL COMMENT '影厅ID（冗余）',
  total_amount    DECIMAL(10,2) NOT NULL COMMENT '订单原价合计',
  discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '会员优惠金额',
  pay_amount      DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  seat_count      INT           NOT NULL DEFAULT 1 COMMENT '购票数量',
  order_status    TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0待支付 1已支付 2已退票 3已取消(超时) 4已完成(已检票)',
  pay_type        TINYINT       DEFAULT NULL COMMENT '支付方式：1模拟微信 2模拟支付宝 3会员余额',
  pay_time        DATETIME      DEFAULT NULL COMMENT '支付时间',
  ticket_time     DATETIME      DEFAULT NULL COMMENT '检票时间',
  cancel_time     DATETIME      DEFAULT NULL COMMENT '取消/退票时间',
  expire_time     DATETIME      DEFAULT NULL COMMENT '订单支付截止时间（超时自动取消释放座位）',
  remark          VARCHAR(200)  DEFAULT NULL COMMENT '备注',
  create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted      TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_user (user_id),
  KEY idx_schedule (schedule_id),
  KEY idx_status (order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

DROP TABLE IF EXISTS biz_order_seat;
CREATE TABLE biz_order_seat (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  order_id    BIGINT        NOT NULL COMMENT '订单ID',
  schedule_id BIGINT        NOT NULL COMMENT '场次ID',
  seat_id     BIGINT        NOT NULL COMMENT '座位ID',
  row_no      INT           NOT NULL COMMENT '排号',
  col_no      INT           NOT NULL COMMENT '列号',
  seat_name   VARCHAR(20)   NOT NULL COMMENT '座位名快照',
  price       DECIMAL(10,2) NOT NULL COMMENT '该座位成交单价快照',
  create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_schedule_seat (schedule_id, seat_id),
  KEY idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单-座位明细表（一个订单多张票=多行）';

-- =============================================================================
-- 六、检票记录表 / 公告表 / 票房日统计表
-- =============================================================================
DROP TABLE IF EXISTS biz_ticket_check;
CREATE TABLE biz_ticket_check (
  id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  order_id    BIGINT      NOT NULL COMMENT '订单ID',
  user_id     BIGINT      NOT NULL COMMENT '观影用户ID',
  schedule_id BIGINT      NOT NULL COMMENT '场次ID',
  staff_id    BIGINT      DEFAULT NULL COMMENT '检票员工ID',
  result      TINYINT     NOT NULL DEFAULT 1 COMMENT '核销结果：1成功 0失败',
  note        VARCHAR(200) DEFAULT NULL COMMENT '备注（失败原因）',
  check_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '检票时间',
  PRIMARY KEY (id),
  KEY idx_order (order_id),
  KEY idx_schedule (schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检票核销记录表';

DROP TABLE IF EXISTS biz_notice;
CREATE TABLE biz_notice (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  title        VARCHAR(150) NOT NULL COMMENT '公告标题',
  content      TEXT         COMMENT '公告正文',
  notice_type  TINYINT      NOT NULL DEFAULT 1 COMMENT '类型：1系统通知 2活动优惠',
  status       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1已发布 0草稿',
  publish_time DATETIME     DEFAULT NULL COMMENT '发布时间',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

DROP TABLE IF EXISTS biz_stat_daily;
CREATE TABLE biz_stat_daily (
  id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  stat_date    DATE          NOT NULL COMMENT '统计日期',
  movie_id     BIGINT       NOT NULL COMMENT '电影ID',
  order_count  INT           NOT NULL DEFAULT 0 COMMENT '订单数',
  ticket_count INT           NOT NULL DEFAULT 0 COMMENT '出票数',
  amount       DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '票房金额',
  PRIMARY KEY (id),
  UNIQUE KEY uk_date_movie (stat_date, movie_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='票房日统计表（定时任务落地，实时大盘以订单聚合为准）';

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 七、初始化基础数据
-- =============================================================================
-- 7.1 三个角色
INSERT INTO sys_role (id, role_code, role_name, description) VALUES
(1,'ROLE_USER','普通用户','用户购票端：浏览电影、选座购票、退票、会员中心'),
(2,'ROLE_STAFF','影院员工','员工端：检票核销、场次查询、会员办理、订单查询'),
(3,'ROLE_ADMIN','超级管理员','管理后台：电影/影厅/排片/订单/用户/权限/公告/票房大盘');

-- 7.2 账号（密码 PLAIN: 明文，后端首启自动 BCrypt 加密）
INSERT INTO sys_user (id, username, password, nickname, real_name, phone, gender, status, remark) VALUES
(1,'admin','PLAIN:admin123','超级管理员','系统管理员','13800000001',1,1,'内置超管，不可删除'),
(2,'staff01','PLAIN:123456','检票员小王','王检票','13800000002',1,1,'前台检票员工'),
(3,'staff02','PLAIN:123456','值班李经理','李经理','13800000003',2,1,'值班经理员工'),
(4,'user01','PLAIN:123456','影迷阿星','周星','13900000001',1,1,'金卡会员测试号'),
(5,'user02','PLAIN:123456','小美','林美','13900000002',2,1,'银卡会员测试号'),
(6,'user03','PLAIN:123456','路人甲','张三','13900000003',1,1,'普通非会员测试号');

-- 7.3 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1,3),(2,2),(3,2),(4,1),(5,1),(6,1);

-- 7.4 管理后台菜单（按钮级权限标识底座）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, perms, icon, sort) VALUES
(1,0,'数据大盘','C','/admin/dashboard','dashboard:view','DataLine',1),
(2,0,'电影管理','C','/admin/movie','movie:list','Film',2),
(3,0,'排片管理','C','/admin/schedule','schedule:list','Calendar',3),
(4,0,'影厅管理','C','/admin/hall','hall:list','Grid',4),
(5,0,'订单管理','C','/admin/order','order:list','Tickets',5),
(6,0,'用户管理','C','/admin/user','user:list','User',6),
(7,0,'会员管理','C','/admin/member','member:list','GoldMedal',7),
(8,0,'公告管理','C','/admin/notice','notice:list','Bell',9);

-- 7.5 五个影厅：2普通厅 + 1巨幕厅 + 1VIP厅 + 1儿童厅
INSERT INTO biz_hall (id, name, hall_type, row_count, col_count, seat_count, status) VALUES
(1,'1号普通厅',1,8,12,96,1),
(2,'2号普通厅',1,8,12,96,1),
(3,'3号IMAX巨幕厅',2,10,16,160,1),
(4,'4号VIP厅',3,5,8,40,1),
(5,'5号儿童厅',4,6,10,60,1);

-- 7.6 用递归CTE按影厅行列自动生成全部座位（共452个）
--     普通厅：0普通座；巨幕厅：前2排为优选座(1)；VIP厅：全部VIP座(2)；儿童厅：前2排亲子座(3)
INSERT INTO biz_seat (hall_id, row_no, col_no, seat_name, seat_type, status)
WITH RECURSIVE nr(n) AS (SELECT 1 UNION ALL SELECT n+1 FROM nr WHERE n < 16),
hc AS (
  SELECT h.id hall_id, h.row_count rc, h.col_count cc, h.hall_type ht FROM biz_hall h
)
SELECT hc.hall_id, r.n, c.n,
       CONCAT(r.n,'排',c.n,'座'),
       CASE hc.ht WHEN 2 THEN CASE WHEN r.n<=2 THEN 1 ELSE 0 END
                  WHEN 3 THEN 2
                  WHEN 4 THEN CASE WHEN r.n<=2 THEN 3 ELSE 0 END
                  ELSE 0 END,
       1
FROM hc
JOIN nr r ON r.n <= hc.rc
JOIN nr c ON c.n <= hc.cc;

-- 7.7 八部电影：5部热映 + 3部待映
INSERT INTO biz_movie (id,name,name_en,director,actors,genre,duration,release_date,region,language,synopsis,score,movie_status) VALUES
(1,'星际迷途','Lost in Stars','陈启航','陆远,苏晴,周野','科幻,冒险',132,'2026-07-01','中国大陆','汉语普通话','一支深空救援队在虫洞另一端失联，年轻领航员穿越星门寻找归途与真相的硬核科幻故事。',9.1,1),
(2,'长安少年行','Chang''an Boys','李慕白','顾清,唐风,柳依依','古装,喜剧',108,'2026-07-18','中国大陆','汉语普通话','盛唐长安城中，一群市井少年阴差阳错卷入离奇案件，笑料百出又热血温暖。',8.4,1),
(3,'深海奇遇记','Deep Sea Tale','海蓝蓝','泡泡,珊瑚,老船长','动画,奇幻',96,'2026-08-02','中国大陆','汉语普通话','小女孩潜入五彩深海，与神秘鲸灵一起守护海洋之心的治愈系动画冒险。',8.8,1),
(4,'极速狂飙','Full Throttle','赵烈','张驰,马赛,雷霆','动作,运动',115,'2026-08-10','中国大陆','汉语普通话','退役赛车手重返赛道，在霓虹山道上与宿敌展开终极对决的燃情动作片。',8.0,1),
(5,'午夜档案馆','Midnight Archive','沈默','白夜,林岚','悬疑,惊悚',121,'2026-08-20','中国大陆','汉语普通话','午夜开放的神秘档案馆里，每一份档案都对应一宗未解悬案，真相层层反转。',8.6,1),
(6,'山河故人归','Mountains and Rivers','顾远山','江雪,陈默','剧情,文艺',128,'2026-10-01','中国大陆','汉语普通话','漂泊半生的老人回到阔别已久的故乡，在山水之间寻回亲情与初心。',0.0,0),
(7,'萌宠大冒险2','Pets Adventure 2','豆豆','橘宝,奶糖','动画,喜剧',94,'2026-10-05','中国大陆','汉语普通话','一群家养萌宠趁主人离家踏上城市冒险，爆笑又温馨的合家欢续作。',0.0,0),
(8,'量子危机','Quantum Crisis','艾瑞克','李昂,艾玛','科幻,动作',138,'2026-11-11','美国','英语','量子实验失控导致平行世界重叠，特工必须在72小时内拯救两个时空。',0.0,0);

-- 7.8 会员等级：普通/银卡/金卡 三级
INSERT INTO biz_member_level (id, level_name, min_points, discount, description) VALUES
(1,'普通会员',0,1.00,'注册即得，积分累计'),
(2,'银卡会员',500,0.90,'累计500积分升级，购票9折'),
(3,'金卡会员',2000,0.85,'累计2000积分升级，购票85折，优先选座');

-- 7.9 会员开卡（user01金卡 / user02银卡；user03未开卡）
INSERT INTO biz_member (user_id, level_id, points, balance, total_consume, open_time, status) VALUES
(4,3,2380,128.50,2380.00,'2026-07-05 10:00:00',1),
(5,2,660,0.00,660.00,'2026-08-01 14:30:00',1);

-- 7.10 系统公告
INSERT INTO biz_notice (title, content, notice_type, status, publish_time) VALUES
('欢迎光临星光国际影城','影城共设5个特色影厅，会员购票最低85折，祝您观影愉快！',1,1,NOW()),
('金秋观影季 充值送好礼','活动期间会员充值满300元送爆米花套餐一份，数量有限先到先得。',2,1,NOW()),
('儿童厅亲子专场温馨提示','周末上午10点儿童厅设亲子专场，陪同家长享半价优惠。',1,1,NOW());

-- 7.11 一周固定排片：从前2天到未来6天共9天，每天每厅4个固定时段，热映电影(id1~5)轮播
--      票价按影厅类型定价：普通厅35 / 巨幕厅55 / VIP厅78 / 儿童厅40
INSERT INTO biz_schedule (movie_id,hall_id,start_time,end_time,price,version_lang,status)
WITH RECURSIVE d(di) AS (SELECT 0 UNION ALL SELECT di+1 FROM d WHERE di<8),
slot(si,st) AS (
  SELECT 1,'10:00:00' UNION ALL SELECT 2,'13:30:00'
  UNION ALL SELECT 3,'17:00:00' UNION ALL SELECT 4,'20:30:00'
)
SELECT
  1 + MOD(d.di*4 + s.si + h.id, 5)                 AS movie_id,
  h.id                                              AS hall_id,
  TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL d.di-2 DAY), s.st) AS start_time,
  TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL d.di-2 DAY), s.st) + INTERVAL m.duration MINUTE AS end_time,
  CASE h.hall_type WHEN 1 THEN 35 WHEN 2 THEN 55 WHEN 3 THEN 78 ELSE 40 END AS price,
  CASE h.hall_type WHEN 2 THEN 'IMAX3D' WHEN 3 THEN 'VIP厅' ELSE '国语2D' END AS version_lang,
  1
FROM d CROSS JOIN slot s CROSS JOIN biz_hall h
JOIN biz_movie m ON m.id = 1 + MOD(d.di*4 + s.si + h.id, 5);

-- 7.12 少量历史已支付订单（挂在前2天的场次上，用于票房大盘首屏有数据）
--     说明：订单座位占用的是历史场次，不影响未来场次购票
INSERT INTO biz_order
(order_no,user_id,schedule_id,movie_id,hall_id,total_amount,discount_amount,pay_amount,seat_count,order_status,pay_type,pay_time,ticket_time,expire_time)
SELECT CONCAT('CG',DATE_FORMAT(s.start_time,'%Y%m%d'),'0',u.uid,'00',s.id), u.uid, s.id, s.movie_id, s.hall_id,
       s.price*2, ROUND(s.price*2*0.1,2), ROUND(s.price*2*0.9,2), 2,
       CASE WHEN u.uid=4 THEN 4 ELSE 1 END, 1, s.start_time,
       CASE WHEN u.uid=4 THEN s.start_time ELSE NULL END,
       s.start_time
FROM biz_schedule s
JOIN (SELECT 4 uid UNION ALL SELECT 5 UNION ALL SELECT 6) u
WHERE s.start_time < NOW()
LIMIT 12;

-- 为历史订单补两张座位票（取该场次前两个座位，仅用于金额演示）
INSERT INTO biz_order_seat (order_id, schedule_id, seat_id, row_no, col_no, seat_name, price)
SELECT o.id, o.schedule_id,
       (SELECT se.id FROM biz_seat se WHERE se.hall_id=o.hall_id ORDER BY se.row_no,se.col_no LIMIT 1 OFFSET 0),
       1,1,'1排1座', ROUND(o.pay_amount/o.seat_count,2)
FROM biz_order o;
INSERT INTO biz_order_seat (order_id, schedule_id, seat_id, row_no, col_no, seat_name, price)
SELECT o.id, o.schedule_id,
       (SELECT se.id FROM biz_seat se WHERE se.hall_id=o.hall_id ORDER BY se.row_no,se.col_no LIMIT 1 OFFSET 1),
       1,2,'1排2座', ROUND(o.pay_amount/o.seat_count,2)
FROM biz_order o;

-- 为已完成(order_status=4)的订单写检票记录
INSERT INTO biz_ticket_check (order_id,user_id,schedule_id,staff_id,result,check_time)
SELECT o.id,o.user_id,o.schedule_id,2,1,o.ticket_time FROM biz_order o WHERE o.order_status=4;

-- =============================================================================
-- 数据校验（执行后应看到：6用户 / 5影厅 / 452座位 / 8电影 / 180场次）
-- =============================================================================
SELECT 'sys_user' t, COUNT(*) c FROM sys_user
UNION ALL SELECT 'biz_hall', COUNT(*) FROM biz_hall
UNION ALL SELECT 'biz_seat', COUNT(*) FROM biz_seat
UNION ALL SELECT 'biz_movie', COUNT(*) FROM biz_movie
UNION ALL SELECT 'biz_schedule', COUNT(*) FROM biz_schedule
UNION ALL SELECT 'biz_order', COUNT(*) FROM biz_order;
