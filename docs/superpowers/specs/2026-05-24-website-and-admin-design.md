# 交易智慧网站与管理后台设计文档

**日期**: 2026-05-24
**项目**: TradeYourPlan (交易智慧) 网站配套项目

---

## 1. 项目概述

为"交易智慧" Android App 开发配套网站，包含公开站点和管理后台。

### 1.1 目标

- 提供公开的语录浏览页面
- 提供管理后台用于语录内容管理
- 提供 API 供 App 拉取语录数据（网站作为主数据源）

### 1.2 域名与部署

- **域名**: trading.tangping.me
- **服务器**: 已有，SSH 可访问
- **Nginx**: 已安装，需配置反向代理
- **SSL**: 使用 acme.sh 申请证书
- **MySQL**: 已安装

---

## 2. 技术栈

| 组件 | 技术 | 说明 |
|------|------|------|
| 前端 | Next.js (静态导出) | Material 3 风格，匹配 App 设计 |
| 后端 | Go + 标准库 | 单一二进制，高性能 |
| 数据库 | MySQL | 已安装 |
| 部署 | Nginx 反向代理 | + systemd 服务管理 |

---

## 3. 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                      Nginx 反向代理                       │
│  trading.tangping.me (443, acme.sh 证书)                │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────┐         ┌─────────────┐              │
│  │ Next.js     │         │ Go API      │              │
│  │ 静态站点    │         │ 后端服务    │              │
│  │ :3000       │         │ :8080       │              │
│  └─────────────┘         └─────────────┘              │
│         │                       │                      │
│         └───────────┬───────────┘                      │
│                     ▼                                  │
│              ┌─────────────┐                          │
│              │   MySQL     │                          │
│              │  (已有)     │                          │
│              └─────────────┘                          │
└─────────────────────────────────────────────────────────┘
```

---

## 4. 数据库设计

### 4.1 语录表 (quotes)

```sql
CREATE TABLE quotes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content VARCHAR(500) NOT NULL COMMENT '语录内容',
    category ENUM('RISK_MGMT', 'MINDSET', 'DISCIPLINE', 'TECHNICAL') NOT NULL COMMENT '分类',
    market_type ENUM('STOCK', 'FUTURES', 'GENERAL') NOT NULL DEFAULT 'GENERAL' COMMENT '市场类型',
    source ENUM('SYSTEM', 'USER') NOT NULL DEFAULT 'SYSTEM' COMMENT '来源',
    is_favorite BOOLEAN DEFAULT FALSE COMMENT '是否收藏',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    created_at BIGINT NOT NULL COMMENT '创建时间戳(毫秒)',
    updated_at BIGINT NOT NULL COMMENT '更新时间戳(毫秒)',
    INDEX idx_category (category),
    INDEX idx_market_type (market_type),
    INDEX idx_source (source),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易语录表';
```

### 4.2 管理配置表 (admin_config)

```sql
CREATE TABLE admin_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    admin_path VARCHAR(100) NOT NULL UNIQUE DEFAULT 'xyz-admin' COMMENT '管理后台路径',
    admin_password_hash VARCHAR(255) NOT NULL COMMENT '管理员密码哈希',
    updated_at BIGINT NOT NULL COMMENT '更新时间戳'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理配置表';

-- 初始化默认密码 (需要后续修改)
INSERT INTO admin_config (admin_password_hash, updated_at)
VALUES ('', UNIX_TIMESTAMP() * 1000);
```

---

## 5. API 接口设计

### 5.1 公共 API (App 使用)

#### 获取语录列表
```
GET /api/quotes
```

**查询参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| category | string | 否 | RISK_MGMT, MINDSET, DISCIPLINE, TECHNICAL |
| marketType | string | 否 | STOCK, FUTURES, GENERAL |
| source | string | 否 | SYSTEM, USER |
| limit | int | 否 | 默认 20 |
| offset | int | 否 | 默认 0 |

**响应:**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "quotes": [
      {
        "id": 1,
        "content": "止损是交易的第一课",
        "category": "RISK_MGMT",
        "categoryDisplay": "风险管理",
        "marketType": "GENERAL",
        "marketTypeDisplay": "通用",
        "source": "SYSTEM",
        "isFavorite": false,
        "viewCount": 0,
        "createdAt": 1234567890000
      }
    ],
    "total": 100,
    "limit": 20,
    "offset": 0
  }
}
```

#### 获取随机语录
```
GET /api/quotes/random
```

**查询参数:**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| category | string | 否 | 指定分类 |
| marketType | string | 否 | 指定市场类型 |

**响应:**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "content": "止损是交易的第一课",
    "category": "RISK_MGMT",
    "categoryDisplay": "风险管理",
    "marketType": "GENERAL",
    "marketTypeDisplay": "通用",
    "source": "SYSTEM",
    "isFavorite": false,
    "createdAt": 1234567890000
  }
}
```

#### 获取单条语录
```
GET /api/quotes/:id
```

#### 获取分类列表
```
GET /api/quotes/categories
```

**响应:**
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {"value": "RISK_MGMT", "label": "风险管理"},
    {"value": "MINDSET", "label": "交易心态"},
    {"value": "DISCIPLINE", "label": "交易纪律"},
    {"value": "TECHNICAL", "label": "技术分析"}
  ]
}
```

#### 获取市场类型列表
```
GET /api/quotes/market-types
```

#### 获取系统预设语录
```
GET /api/system/quotes
```

用于 App 首次安装时初始化本地数据库。

### 5.2 管理 API (需要认证)

#### 管理员登录
```
POST /api/admin/login
```

**请求:**
```json
{
  "password": "admin_password"
}
```

**响应:**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "jwt_token_or_session_id"
  }
}
```

#### 获取所有语录 (管理用)
```
GET /api/admin/quotes
Authorization: Bearer {token}
```

#### 创建语录
```
POST /api/admin/quotes
Authorization: Bearer {token}
```

**请求:**
```json
{
  "content": "新的交易语录",
  "category": "RISK_MGMT",
  "marketType": "GENERAL",
  "source": "SYSTEM"
}
```

#### 更新语录
```
PUT /api/admin/quotes/:id
Authorization: Bearer {token}
```

#### 删除语录
```
DELETE /api/admin/quotes/:id
Authorization: Bearer {token}
```

#### 批量导入语录
```
POST /api/admin/quotes/batch
Authorization: Bearer {token}
```

**请求:**
```json
{
  "quotes": [
    {"content": "语录1", "category": "RISK_MGMT", "marketType": "GENERAL"},
    {"content": "语录2", "category": "MINDSET", "marketType": "STOCK"}
  ]
}
```

### 5.3 错误码

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 1001 | 参数错误 |
| 1002 | 未授权 |
| 1003 | 密码错误 |
| 1004 | 资源不存在 |
| 5000 | 服务器错误 |

---

## 6. 前端页面设计

### 6.1 页面路由

| 路由 | 页面 | 说明 |
|------|------|------|
| `/` | 首页 | 极简风格，展示品牌 + 下载链接 |
| `/quotes` | 语录浏览 | 单条聚焦 + 分类筛选 + 换一换按钮 |
| `/about` | 关于页面 | 项目介绍 |
| `/xyz-admin` | 管理登录 | 密码验证页 |
| `/xyz-admin/dashboard` | 管理后台 | 侧边栏 + 语录管理 |

### 6.2 视觉风格

**继承 App 专业深色主题:**

| 元素 | 颜色 | 说明 |
|------|------|------|
| 主色 | #3B82F6 | Primary Blue |
| 背景 | #0F172A | 深色背景 |
| 卡片/表面 | #1E293B | 次级深色 |
| 边框 | #334155 | Border |
| 错误 | #DC2626 | Destructive |
| 文本 | #F8FAFC | On Background |

**分类色标:**
- 风险管理 (RISK_MGMT): #0EA5E9 (天蓝)
- 交易心态 (MINDSET): #F59E0B (橙色)
- 交易纪律 (DISCIPLINE): #10B981 (绿色)
- 技术分析 (TECHNICAL): #8B5CF6 (紫色)

### 6.3 首页设计 (极简风格)

- 居中展示品牌名称 "交易智慧"
- 副标题："让交易智慧常伴左右"
- 下载 App 按钮
- 浏览语录链接
- Material 3 设计语言

### 6.4 语录浏览页 (单条聚焦)

- 顶部: 分类筛选标签
- 中央: 单条语录大字展示
- 底部: 分类标签 + 收藏数 + "换一换"按钮
- 类似 App 首页体验

### 6.5 管理后台 (侧边栏布局)

- 左侧: 固定侧边栏导航
  - 语录管理
  - 统计 (预留)
  - 设置 (预留)
- 右侧: 内容区域
  - 顶部: 标题 + 新增按钮
  - 列表: 语录卡片（内容 + 分类标签 + 操作按钮）

---

## 7. 部署配置

### 7.1 Nginx 配置

```nginx
# /etc/nginx/sites-available/trading.tangping.me
server {
    listen 80;
    server_name trading.tangping.me;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name trading.tangping.me;

    ssl_certificate /root/.acme.sh/trading.tangping.me_ecc/fullchain.cer;
    ssl_certificate_key /root/.acme.sh/trading.tangping.me_ecc/trading.tangping.me.key;

    # 静态站点
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    # API
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 7.2 SSL 证书申请 (acme.sh)

```bash
# 申请证书
acme.sh --issue -d trading.tangping.me --nginx /etc/nginx

# 安装证书
acme.sh --install-cert -d trading.tangping.me \
  --ecc \
  --key-file /etc/nginx/ssl/trading.tangping.me.key \
  --fullchain-file /etc/nginx/ssl/trading.tangping.me.cer \
  --reloadcmd "systemctl reload nginx"
```

### 7.3 服务管理 (systemd)

**Go API 服务:**
```ini
# /etc/systemd/system/trading-api.service
[Unit]
Description=Trading Wisdom API
After=network.target mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/trading-api
ExecStart=/opt/trading-api/trading-api
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

**Next.js 静态站点服务:**
```ini
# /etc/systemd/system/trading-web.service
[Unit]
Description=Trading Wisdom Web
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/trading-web
ExecStart=npm run start
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

---

## 8. 项目目录结构

```
/opt/
├── trading-api/          # Go 后端
│   ├── main.go
│   ├── api/
│   ├── models/
│   ├── database/
│   └── go.mod
├── trading-web/          # Next.js 前端
│   ├── app/
│   ├── components/
│   ├── public/
│   └── package.json
└── trading-wisdom/       # API 文档 (可选)
    └── openapi.yaml
```

---

## 9. 开发阶段划分

1. **阶段一**: 数据库 + Go API 核心
2. **阶段二**: Next.js 前端 + 公开页面
3. **阶段三**: 管理后台 + 部署配置
4. **阶段四**: App API 集成测试

---

## 10. 附录

### 10.1 分类枚举

```go
// Category
const (
    CategoryRiskMgmt  = "RISK_MGMT"   // 风险管理
    CategoryMindset   = "MINDSET"     // 交易心态
    CategoryDiscipline = "DISCIPLINE" // 交易纪律
    CategoryTechnical = "TECHNICAL"   // 技术分析
)
```

### 10.2 市场类型枚举

```go
// MarketType
const (
    MarketTypeStock   = "STOCK"   // 股票
    MarketTypeFutures = "FUTURES" // 期货
    MarketTypeGeneral = "GENERAL" // 通用
)
```

### 10.3 来源枚举

```go
// QuoteSource
const (
    SourceSystem = "SYSTEM" // 系统
    SourceUser   = "USER"   // 用户
)
```
