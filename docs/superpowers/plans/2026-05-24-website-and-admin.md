# 交易智慧网站与管理后台实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建交易智慧 App 的配套网站，包含公开语录浏览页、管理后台、以及 App 使用的 API 服务

**Architecture:** Nginx 作为反向代理，Next.js 静态站点 + Go API 服务，MySQL 数据存储

**Tech Stack:** Next.js (静态导出), Go + 标准库, MySQL, Nginx, acme.sh

---

## 项目文件结构

```
/opt/
├── trading-api/                      # Go API 后端
│   ├── main.go                       # 入口文件
│   ├── go.mod                        # Go 模块定义
│   ├── go.sum                        # 依赖锁定
│   ├── config/
│   │   └── config.go                 # 配置管理
│   ├── models/
│   │   └── quote.go                  # 语录数据模型
│   ├── database/
│   │   └── db.go                     # 数据库连接
│   ├── handlers/
│   │   ├── public.go                 # 公共 API 处理器
│   │   ├── admin.go                  # 管理 API 处理器
│   │   └── middleware.go             # 中间件 (认证)
│   └── utils/
│       └── response.go               # 统一响应格式
├── trading-web/                      # Next.js 前端
│   ├── package.json
│   ├── next.config.js
│   ├── tailwind.config.ts
│   ├── app/
│   │   ├── layout.tsx                # 根布局
│   │   ├── page.tsx                  # 首页
│   │   ├── quotes/
│   │   │   └── page.tsx              # 语录浏览页
│   │   ├── about/
│   │   │   └── page.tsx              # 关于页面
│   │   ├── xyz-admin/
│   │   │   ├── page.tsx              # 管理登录
│   │   │   └── dashboard/
│   │   │       └── page.tsx          # 管理后台
│   │   └── api/                      # Next.js API routes (用于前端代理)
│   ├── components/
│   │   ├── Header.tsx                # 导航头部
│   │   ├── Footer.tsx                # 页脚
│   │   ├── QuoteCard.tsx             # 语录卡片
│   │   ├── CategoryFilter.tsx        # 分类筛选器
│   │   └── admin/
│   │       ├── Sidebar.tsx           # 管理后台侧边栏
│   │       ├── QuoteList.tsx         # 语录列表
│   │       └── QuoteEditor.tsx       # 语录编辑器
│   ├── lib/
│   │   ├── api.ts                    # API 客户端
│   │   └── theme.ts                  # 主题配置
│   └── styles/
│       └── globals.css               # 全局样式
└── trading-deploy/                   # 部署脚本
    ├── nginx.conf                    # Nginx 配置
    └── setup-ssl.sh                  # SSL 证书设置脚本
```

---

## 阶段一：数据库与 Go API 后端

### Task 1: 创建 MySQL 数据库表

**Files:**
- Create: `/opt/trading-api/database/schema.sql`

- [ ] **Step 1: 创建数据库初始化脚本**

```sql
-- /opt/trading-api/database/schema.sql
CREATE DATABASE IF NOT EXISTS trading_wisdom CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE trading_wisdom;

-- 语录表
DROP TABLE IF EXISTS quotes;
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

-- 管理配置表
DROP TABLE IF EXISTS admin_config;
CREATE TABLE admin_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    admin_path VARCHAR(100) NOT NULL UNIQUE DEFAULT 'xyz-admin' COMMENT '管理后台路径',
    admin_password_hash VARCHAR(255) NOT NULL COMMENT '管理员密码哈希(bcrypt)',
    updated_at BIGINT NOT NULL COMMENT '更新时间戳'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理配置表';

-- 初始化管理员配置 (默认密码: admin123, 需首次登录后修改)
-- bcrypt hash of "admin123"
INSERT INTO admin_config (admin_password_hash, updated_at)
VALUES ('$2a$10$YourBcryptHashHere', UNIX_TIMESTAMP() * 1000);

-- 插入示例语录
INSERT INTO quotes (content, category, market_type, source, created_at, updated_at) VALUES
('止损是交易的第一课，学会止损才能生存。', 'RISK_MGMT', 'GENERAL', 'SYSTEM', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('计划你的交易，交易你的计划。', 'DISCIPLINE', 'GENERAL', 'SYSTEM', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('不要让情绪左右你的交易决策。', 'MINDSET', 'STOCK', 'SYSTEM', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('趋势是你的朋友，永远不要逆势而为。', 'TECHNICAL', 'GENERAL', 'SYSTEM', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);
```

- [ ] **Step 2: 在服务器上执行数据库初始化**

```bash
# 在服务器上执行 (通过 SSH)
mysql -u root -p < /opt/trading-api/database/schema.sql
```

Expected: "Database created and tables initialized"

- [ ] **Step 3: 验证表创建成功**

```bash
mysql -u root -p -e "USE trading_wisdom; SHOW TABLES; DESCRIBE quotes;"
```

Expected: 显示 quotes 和 admin_config 表结构

- [ ] **Step 4: 提交**

```bash
git add database/schema.sql
git commit -m "feat: add MySQL database schema"
```

---

### Task 2: 初始化 Go 项目

**Files:**
- Create: `/opt/trading-api/go.mod`
- Create: `/opt/trading-api/main.go`

- [ ] **Step 1: 创建 go.mod**

```go
// /opt/trading-api/go.mod
module trading-api

go 1.21

require (
    github.com/go-sql-driver/mysql v1.7.1
    golang.org/x/crypto v0.18.0
    github.com/gorilla/mux v1.8.1
)
```

- [ ] **Step 2: 下载依赖**

```bash
cd /opt/trading-api && go mod tidy
```

Expected: 依赖下载完成

- [ ] **Step 3: 创建 main.go 入口文件**

```go
// /opt/trading-api/main.go
package main

import (
    "database/sql"
    "log"
    "net/http"
    "os"

    _ "github.com/go-sql-driver/mysql"
    "github.com/gorilla/mux"
)

func main() {
    // 数据库连接
    dsn := os.Getenv("DB_DSN")
    if dsn == "" {
        dsn = "root:@tcp(127.0.0.1:3306)/trading_wisdom?parseTime=true"
    }

    db, err := sql.Open("mysql", dsn)
    if err != nil {
        log.Fatal("Database connection failed:", err)
    }
    defer db.Close()

    if err := db.Ping(); err != nil {
        log.Fatal("Database ping failed:", err)
    }

    // 创建路由
    r := mux.NewRouter()

    // 公共 API
    r.HandleFunc("/api/quotes", handleGetQuotes(db)).Methods("GET")
    r.HandleFunc("/api/quotes/random", handleGetRandomQuote(db)).Methods("GET")
    r.HandleFunc("/api/quotes/{id}", handleGetQuoteByID(db)).Methods("GET")
    r.HandleFunc("/api/quotes/categories", handleGetCategories).Methods("GET")
    r.HandleFunc("/api/quotes/market-types", handleGetMarketTypes).Methods("GET")
    r.HandleFunc("/api/system/quotes", handleGetSystemQuotes(db)).Methods("GET")

    // 管理 API
    r.HandleFunc("/api/admin/login", handleAdminLogin(db)).Methods("POST")
    r.HandleFunc("/api/admin/quotes", authMiddleware(handleAdminGetQuotes(db), db)).Methods("GET")
    r.HandleFunc("/api/admin/quotes", authMiddleware(handleAdminCreateQuote(db), db)).Methods("POST")
    r.HandleFunc("/api/admin/quotes/{id}", authMiddleware(handleAdminUpdateQuote(db), db)).Methods("PUT")
    r.HandleFunc("/api/admin/quotes/{id}", authMiddleware(handleAdminDeleteQuote(db), db)).Methods("DELETE")
    r.HandleFunc("/api/admin/quotes/batch", authMiddleware(handleAdminBatchImport(db), db)).Methods("POST")

    // 启动服务器
    port := os.Getenv("PORT")
    if port == "" {
        port = "8080"
    }

    log.Printf("Server starting on port %s", port)
    log.Fatal(http.ListenAndServe(":"+port, r))
}
```

- [ ] **Step 4: 提交**

```bash
git add go.mod main.go
git commit -m "feat: initialize Go project with main entry point"
```

---

### Task 3: 创建数据模型

**Files:**
- Create: `/opt/trading-api/models/quote.go`
- Create: `/opt/trading-api/utils/response.go`

- [ ] **Step 1: 创建语录模型**

```go
// /opt/trading-api/models/quote.go
package models

import (
    "time"
)

// Category 语录分类
type Category string

const (
    CategoryRiskMgmt  Category = "RISK_MGMT"   // 风险管理
    CategoryMindset   Category = "MINDSET"     // 交易心态
    CategoryDiscipline Category = "DISCIPLINE" // 交易纪律
    CategoryTechnical Category = "TECHNICAL"   // 技术分析
)

func (c Category) DisplayName() string {
    switch c {
    case CategoryRiskMgmt:
        return "风险管理"
    case CategoryMindset:
        return "交易心态"
    case CategoryDiscipline:
        return "交易纪律"
    case CategoryTechnical:
        return "技术分析"
    default:
        return ""
    }
}

// MarketType 市场类型
type MarketType string

const (
    MarketTypeStock   MarketType = "STOCK"   // 股票
    MarketTypeFutures MarketType = "FUTURES" // 期货
    MarketTypeGeneral MarketType = "GENERAL" // 通用
)

func (m MarketType) DisplayName() string {
    switch m {
    case MarketTypeStock:
        return "股票"
    case MarketTypeFutures:
        return "期货"
    case MarketTypeGeneral:
        return "通用"
    default:
        return ""
    }
}

// QuoteSource 语录来源
type QuoteSource string

const (
    SourceSystem QuoteSource = "SYSTEM" // 系统
    SourceUser   QuoteSource = "USER"   // 用户
)

// Quote 语录数据模型
type Quote struct {
    ID          int64       `json:"id"`
    Content     string      `json:"content"`
    Category    Category    `json:"category"`
    MarketType  MarketType  `json:"marketType"`
    Source      QuoteSource `json:"source"`
    IsFavorite  bool        `json:"isFavorite"`
    ViewCount   int         `json:"viewCount"`
    CreatedAt   int64       `json:"createdAt"`
    UpdatedAt   int64       `json:"updatedAt"`
}

// QuoteWithDisplay 带显示名称的语录
type QuoteWithDisplay struct {
    ID              int64       `json:"id"`
    Content         string      `json:"content"`
    Category        Category    `json:"category"`
    CategoryDisplay string      `json:"categoryDisplay"`
    MarketType      MarketType  `json:"marketType"`
    MarketTypeDisplay string    `json:"marketTypeDisplay"`
    Source          QuoteSource `json:"source"`
    IsFavorite      bool        `json:"isFavorite"`
    ViewCount       int         `json:"viewCount"`
    CreatedAt       int64       `json:"createdAt"`
}

// ToWithDisplay 转换为带显示名称的格式
func (q *Quote) ToWithDisplay() QuoteWithDisplay {
    return QuoteWithDisplay{
        ID:               q.ID,
        Content:          q.Content,
        Category:         q.Category,
        CategoryDisplay:  q.Category.DisplayName(),
        MarketType:       q.MarketType,
        MarketTypeDisplay: q.MarketType.DisplayName(),
        Source:           q.Source,
        IsFavorite:       q.IsFavorite,
        ViewCount:        q.ViewCount,
        CreatedAt:        q.CreatedAt,
    }
}

// CreateQuoteRequest 创建语录请求
type CreateQuoteRequest struct {
    Content    string     `json:"content"`
    Category   Category   `json:"category"`
    MarketType MarketType `json:"marketType"`
    Source     QuoteSource `json:"source"`
}

// UpdateQuoteRequest 更新语录请求
type UpdateQuoteRequest struct {
    Content    *string     `json:"content,omitempty"`
    Category   *Category   `json:"category,omitempty"`
    MarketType *MarketType `json:"marketType,omitempty"`
}

// AdminLoginRequest 管理员登录请求
type AdminLoginRequest struct {
    Password string `json:"password"`
}

// BatchImportRequest 批量导入请求
type BatchImportRequest struct {
    Quotes []CreateQuoteRequest `json:"quotes"`
}

// CategoryLabel 分类标签
type CategoryLabel struct {
    Value string `json:"value"`
    Label string `json:"label"`
}

// MarketTypeLabel 市场类型标签
type MarketTypeLabel struct {
    Value string `json:"value"`
    Label string `json:"label"`
}
```

- [ ] **Step 2: 创建统一响应格式**

```go
// /opt/trading-api/utils/response.go
package utils

import (
    "encoding/json"
    "net/http"
)

// ErrorCode 错误码
const (
    CodeSuccess      = 0
    CodeParamError   = 1001
    CodeUnauthorized = 1002
    CodePasswordErr  = 1003
    CodeNotFound     = 1004
    CodeServerError  = 5000
)

// Response 统一响应格式
type Response struct {
    Code    int         `json:"code"`
    Message string      `json:"message"`
    Data    interface{} `json:"data,omitempty"`
}

// Success 成功响应
func Success(w http.ResponseWriter, data interface{}) {
    writeJSON(w, Response{
        Code:    CodeSuccess,
        Message: "success",
        Data:    data,
    })
}

// Error 错误响应
func Error(w http.ResponseWriter, code int, message string) {
    status := http.StatusOK
    if code >= 5000 {
        status = http.StatusInternalServerError
    } else if code >= 1002 {
        status = http.StatusUnauthorized
    } else if code >= 1001 {
        status = http.StatusBadRequest
    }

    writeJSON(w, Response{
        Code:    code,
        Message: message,
    })
}

func writeJSON(w http.ResponseWriter, data interface{}) {
    w.Header().Set("Content-Type", "application/json")
    json.NewEncoder(w).Encode(data)
}
```

- [ ] **Step 3: 提交**

```bash
git add models/quote.go utils/response.go
git commit -m "feat: add quote models and response utilities"
```

---

### Task 4: 创建数据库连接层

**Files:**
- Create: `/opt/trading-api/database/db.go`

- [ ] **Step 1: 创建数据库操作层**

```go
// /opt/trading-api/database/db.go
package database

import (
    "database/sql"
    "fmt"
    "time"

    "trading-api/models"
)

// QuoteDB 语录数据库操作
type QuoteDB struct {
    db *sql.DB
}

// NewQuoteDB 创建语录数据库实例
func NewQuoteDB(db *sql.DB) *QuoteDB {
    return &QuoteDB{db: db}
}

// GetAll 获取所有语录（带筛选和分页）
func (q *QuoteDB) GetAll(category models.Category, marketType models.MarketType, source models.QuoteSource, limit, offset int) ([]models.Quote, int, error) {
    query := "SELECT id, content, category, market_type, source, is_favorite, view_count, created_at, updated_at FROM quotes WHERE 1=1"
    args := []interface{}{}
    countQuery := "SELECT COUNT(*) FROM quotes WHERE 1=1"

    if category != "" {
        query += " AND category = ?"
        countQuery += " AND category = ?"
        args = append(args, string(category))
    }
    if marketType != "" {
        query += " AND market_type = ?"
        countQuery += " AND market_type = ?"
        args = append(args, string(marketType))
    }
    if source != "" {
        query += " AND source = ?"
        countQuery += " AND source = ?"
        args = append(args, string(source))
    }

    // 获取总数
    var total int
    countArgs := make([]interface{}, len(args))
    copy(countArgs, args)
    if err := q.db.QueryRow(countQuery, countArgs...).Scan(&total); err != nil {
        return nil, 0, err
    }

    // 获取数据
    query += " ORDER BY created_at DESC LIMIT ? OFFSET ?"
    args = append(args, limit, offset)

    rows, err := q.db.Query(query, args...)
    if err != nil {
        return nil, 0, err
    }
    defer rows.Close()

    var quotes []models.Quote
    for rows.Next() {
        var q models.Quote
        err := rows.Scan(&q.ID, &q.Content, &q.Category, &q.MarketType, &q.Source, &q.IsFavorite, &q.ViewCount, &q.CreatedAt, &q.UpdatedAt)
        if err != nil {
            return nil, 0, err
        }
        quotes = append(quotes, q)
    }

    return quotes, total, nil
}

// GetRandom 获取随机语录
func (q *QuoteDB) GetRandom(category models.Category, marketType models.MarketType) (*models.Quote, error) {
    query := `SELECT id, content, category, market_type, source, is_favorite, view_count, created_at, updated_at
              FROM quotes WHERE 1=1`
    args := []interface{}{}

    if category != "" {
        query += " AND category = ?"
        args = append(args, string(category))
    }
    if marketType != "" {
        query += " AND market_type = ?"
        args = append(args, string(marketType))
    }

    query += " ORDER BY RAND() LIMIT 1"

    var quote models.Quote
    err := q.db.QueryRow(query, args...).Scan(
        &quote.ID, &quote.Content, &quote.Category, &quote.MarketType,
        &quote.Source, &quote.IsFavorite, &quote.ViewCount,
        &quote.CreatedAt, &quote.UpdatedAt,
    )

    if err == sql.ErrNoRows {
        return nil, nil
    }
    if err != nil {
        return nil, err
    }

    // 增加浏览次数
    q.db.Exec("UPDATE quotes SET view_count = view_count + 1 WHERE id = ?", quote.ID)

    return &quote, nil
}

// GetByID 根据 ID 获取语录
func (q *QuoteDB) GetByID(id int64) (*models.Quote, error) {
    var quote models.Quote
    err := q.db.QueryRow(
        "SELECT id, content, category, market_type, source, is_favorite, view_count, created_at, updated_at FROM quotes WHERE id = ?",
        id,
    ).Scan(
        &quote.ID, &quote.Content, &quote.Category, &quote.MarketType,
        &quote.Source, &quote.IsFavorite, &quote.ViewCount,
        &quote.CreatedAt, &quote.UpdatedAt,
    )

    if err == sql.ErrNoRows {
        return nil, nil
    }
    if err != nil {
        return nil, err
    }

    return &quote, nil
}

// GetSystemQuotes 获取系统预设语录
func (q *QuoteDB) GetSystemQuotes() ([]models.Quote, error) {
    rows, err := q.db.Query(
        "SELECT id, content, category, market_type, source, is_favorite, view_count, created_at, updated_at FROM quotes WHERE source = 'SYSTEM'",
    )
    if err != nil {
        return nil, err
    }
    defer rows.Close()

    var quotes []models.Quote
    for rows.Next() {
        var q models.Quote
        err := rows.Scan(&q.ID, &q.Content, &q.Category, &q.MarketType, &q.Source, &q.IsFavorite, &q.ViewCount, &q.CreatedAt, &q.UpdatedAt)
        if err != nil {
            return nil, err
        }
        quotes = append(quotes, q)
    }

    return quotes, nil
}

// Create 创建语录
func (q *QuoteDB) Create(req models.CreateQuoteRequest) (*models.Quote, error) {
    now := time.Now().UnixMilli()
    result, err := q.db.Exec(
        "INSERT INTO quotes (content, category, market_type, source, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
        req.Content, string(req.Category), string(req.MarketType), string(req.Source), now, now,
    )
    if err != nil {
        return nil, err
    }

    id, err := result.LastInsertId()
    if err != nil {
        return nil, err
    }

    return q.GetByID(id)
}

// Update 更新语录
func (q *QuoteDB) Update(id int64, req models.UpdateQuoteRequest) error {
    query := "UPDATE quotes SET updated_at = ?"
    args := []interface{}{time.Now().UnixMilli()}

    if req.Content != nil {
        query += ", content = ?"
        args = append(args, *req.Content)
    }
    if req.Category != nil {
        query += ", category = ?"
        args = append(args, string(*req.Category))
    }
    if req.MarketType != nil {
        query += ", market_type = ?"
        args = append(args, string(*req.MarketType))
    }

    query += " WHERE id = ?"
    args = append(args, id)

    result, err := q.db.Exec(query, args...)
    if err != nil {
        return err
    }

    rows, _ := result.RowsAffected()
    if rows == 0 {
        return sql.ErrNoRows
    }

    return nil
}

// Delete 删除语录
func (q *QuoteDB) Delete(id int64) error {
    result, err := q.db.Exec("DELETE FROM quotes WHERE id = ?", id)
    if err != nil {
        return err
    }

    rows, _ := result.RowsAffected()
    if rows == 0 {
        return sql.ErrNoRows
    }

    return nil
}

// BatchCreate 批量创建语录
func (q *QuoteDB) BatchCreate(quotes []models.CreateQuoteRequest) ([]models.Quote, error) {
    now := time.Now().UnixMilli()
    created := []models.Quote{}

    for _, req := range quotes {
        result, err := q.db.Exec(
            "INSERT INTO quotes (content, category, market_type, source, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
            req.Content, string(req.Category), string(req.MarketType), string(req.Source), now, now,
        )
        if err != nil {
            return nil, fmt.Errorf("batch insert failed: %w", err)
        }

        id, err := result.LastInsertId()
        if err != nil {
            return nil, err
        }

        quote, err := q.GetByID(id)
        if err != nil {
            return nil, err
        }
        created = append(created, *quote)
    }

    return created, nil
}

// GetAdminPasswordHash 获取管理员密码哈希
func (q *QuoteDB) GetAdminPasswordHash() (string, error) {
    var hash string
    err := q.db.QueryRow("SELECT admin_password_hash FROM admin_config WHERE id = 1").Scan(&hash)
    return hash, err
}

// VerifyAdminPassword 验证管理员密码
func (q *QuoteDB) VerifyAdminPassword(password string) bool {
    hash, err := q.GetAdminPasswordHash()
    if err != nil {
        return false
    }

    // 使用 bcrypt 验证密码
    // 实现在 middleware 中
    return hash != "" // 简化版，实际需要 bcrypt 验证
}
```

- [ ] **Step 2: 提交**

```bash
git add database/db.go
git commit -m "feat: add database operations layer"
```

---

### Task 5: 创建 API 处理器（公共 API）

**Files:**
- Create: `/opt/trading-api/handlers/public.go`

- [ ] **Step 1: 创建公共 API 处理器**

```go
// /opt/trading-api/handlers/public.go
package handlers

import (
    "database/sql"
    "encoding/json"
    "net/http"
    "strconv"
    "strings"

    "trading-api/database"
    "trading-api/models"
    "trading-api/utils"
)

// QuoteListResponse 语录列表响应
type QuoteListResponse struct {
    Quotes []models.QuoteWithDisplay `json:"quotes"`
    Total  int                       `json:"total"`
    Limit  int                       `json:"limit"`
    Offset int                       `json:"offset"`
}

func handleGetQuotes(db *sql.DB) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        // 解析查询参数
        category := models.Category(r.URL.Query().Get("category"))
        marketType := models.MarketType(r.URL.Query().Get("marketType"))
        source := models.QuoteSource(r.URL.Query().Get("source"))

        limit := 20
        if l := r.URL.Query().Get("limit"); l != "" {
            if parsed, err := strconv.Atoi(l); err == nil && parsed > 0 && parsed <= 100 {
                limit = parsed
            }
        }

        offset := 0
        if o := r.URL.Query().Get("offset"); o != "" {
            if parsed, err := strconv.Atoi(o); err == nil && parsed >= 0 {
                offset = parsed
            }
        }

        quoteDB := database.NewQuoteDB(db)
        quotes, total, err := quoteDB.GetAll(category, marketType, source, limit, offset)
        if err != nil {
            utils.Error(w, utils.CodeServerError, "Database error")
            return
        }

        // 转换为带显示名称的格式
        displayQuotes := make([]models.QuoteWithDisplay, len(quotes))
        for i, q := range quotes {
            displayQuotes[i] = q.ToWithDisplay()
        }

        utils.Success(w, QuoteListResponse{
            Quotes: displayQuotes,
            Total:  total,
            Limit:  limit,
            Offset: offset,
        })
    }
}

func handleGetRandomQuote(db *sql.DB) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        category := models.Category(r.URL.Query().Get("category"))
        marketType := models.MarketType(r.URL.Query().Get("marketType"))

        quoteDB := database.NewQuoteDB(db)
        quote, err := quoteDB.GetRandom(category, marketType)
        if err != nil {
            utils.Error(w, utils.CodeServerError, "Database error")
            return
        }

        if quote == nil {
            utils.Error(w, utils.CodeNotFound, "No quotes found")
            return
        }

        utils.Success(w, quote.ToWithDisplay())
    }
}

func handleGetQuoteByID(db *sql.DB) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        // 从 mux.Vars 获取 ID
        vars := map[string]string{}
        // 简化处理，实际应从 mux 获取
        idStr := strings.TrimPrefix(r.URL.Path, "/api/quotes/")
        idStr = strings.Split(idStr, "/")[0]

        id, err := strconv.ParseInt(idStr, 10, 64)
        if err != nil {
            utils.Error(w, utils.CodeParamError, "Invalid quote ID")
            return
        }

        quoteDB := database.NewQuoteDB(db)
        quote, err := quoteDB.GetByID(id)
        if err != nil {
            utils.Error(w, utils.CodeServerError, "Database error")
            return
        }

        if quote == nil {
            utils.Error(w, utils.CodeNotFound, "Quote not found")
            return
        }

        utils.Success(w, quote.ToWithDisplay())
    }
}

func handleGetCategories(w http.ResponseWriter, r *http.Request) {
    categories := []models.CategoryLabel{
        {Value: "RISK_MGMT", Label: "风险管理"},
        {Value: "MINDSET", Label: "交易心态"},
        {Value: "DISCIPLINE", Label: "交易纪律"},
        {Value: "TECHNICAL", Label: "技术分析"},
    }
    utils.Success(w, categories)
}

func handleGetMarketTypes(w http.ResponseWriter, r *http.Request) {
    types := []models.MarketTypeLabel{
        {Value: "STOCK", Label: "股票"},
        {Value: "FUTURES", Label: "期货"},
        {Value: "GENERAL", Label: "通用"},
    }
    utils.Success(w, types)
}

func handleGetSystemQuotes(db *sql.DB) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        quoteDB := database.NewQuoteDB(db)
        quotes, err := quoteDB.GetSystemQuotes()
        if err != nil {
            utils.Error(w, utils.CodeServerError, "Database error")
            return
        }

        displayQuotes := make([]models.QuoteWithDisplay, len(quotes))
        for i, q := range quotes {
            displayQuotes[i] = q.ToWithDisplay()
        }

        utils.Success(w, displayQuotes)
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add handlers/public.go
git commit -m "feat: add public API handlers"
```

---

### Task 6: 创建管理 API 处理器和认证中间件

**Files:**
- Create: `/opt/trading-api/handlers/admin.go`
- Create: `/opt/trading-api/handlers/middleware.go`

- [ ] **Step 1: 创建认证中间件**

```go
// /opt/trading-api/handlers/middleware.go
package handlers

import (
    "database/sql"
    "net/http"
    "strings"

    "golang.org/x/crypto/bcrypt"
    "trading-api/database"
    "trading-api/utils"
)

// SessionToken 简单的会话令牌 (生产环境应使用 JWT)
var SessionToken = "trading-admin-session-2024"

func authMiddleware(next http.HandlerFunc, db *sql.DB) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        // 检查 Authorization header
        auth := r.Header.Get("Authorization")
        if auth == "" {
            utils.Error(w, utils.CodeUnauthorized, "Missing authorization header")
            return
        }

        // 检查 Bearer token
        if !strings.HasPrefix(auth, "Bearer ") {
            utils.Error(w, utils.CodeUnauthorized, "Invalid authorization format")
            return
        }

        token := strings.TrimPrefix(auth, "Bearer ")
        if token != SessionToken {
            utils.Error(w, utils.CodeUnauthorized, "Invalid token")
            return
        }

        next(w, r)
    }
}

func handleAdminLogin(db *sql.DB) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        var req models.AdminLoginRequest
        if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
            utils.Error(w, utils.CodeParamError, "Invalid request body")
            return
        }

        quoteDB := database.NewQuoteDB(db)
        hash, err := quoteDB.GetAdminPasswordHash()
        if err != nil {
            utils.Error(w, utils.CodeServerError, "Database error")
            return
        }

        // 验证密码 (bcrypt)
        if err := bcrypt.CompareHashAndPassword([]byte(hash), []byte(req.Password)); err != nil {
            utils.Error(w, utils.CodePasswordErr, "Invalid password")
            return
        }

        // 返回 session token
        utils.Success(w, map[string]string{
            "token": SessionToken,
        })
    }
}
```

- [ ] **Step 2: 创建管理 API 处理器**

```go
// /opt/trading-api/handlers/admin.go (续)
// ... 在 handleAdminLogin 后添加

func handleAdminGetQuotes(db *sql.DB) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        quoteDB := database.NewQuoteDB(db)
        quotes, total, err := quoteDB.GetAll("", "", "", 1000, 0)
        if err != nil {
            utils.Error(w, utils.CodeServerError, "Database error")
            return
        }

        displayQuotes := make([]models.QuoteWithDisplay, len(quotes))
        for i, q := range quotes {
            displayQuotes[i] = q.ToWithDisplay()
        }

        utils.Success(w, map[string]interface{}{
            "quotes": displayQuotes,
            "total":  total,
        })
    }
}

func handleAdminCreateQuote(db *sql.DB) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        var req models.CreateQuoteRequest
        if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
            utils.Error(w, utils.CodeParamError, "Invalid request body")
            return
        }

        // 验证
        if req.Content == "" || len(req.Content) > 500 {
            utils.Error(w, utils.CodeParamError, "Content must be 1-500 characters")
            return
        }

        if req.Category == "" {
            req.Category = models.CategoryGeneral
        }

        quoteDB := database.NewQuoteDB(db)
        quote, err := quoteDB.Create(req)
        if err != nil {
            utils.Error(w, utils.CodeServerError, "Failed to create quote")
            return
        }

        utils.Success(w, quote.ToWithDisplay())
    }
}

func handleAdminUpdateQuote(db *sql.DB) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        // 获取 ID (简化处理)
        idStr := strings.TrimPrefix(r.URL.Path, "/api/admin/quotes/")
        idStr = strings.Split(idStr, "/")[0]

        id, err := strconv.ParseInt(idStr, 10, 64)
        if err != nil {
            utils.Error(w, utils.CodeParamError, "Invalid quote ID")
            return
        }

        var req models.UpdateQuoteRequest
        if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
            utils.Error(w, utils.CodeParamError, "Invalid request body")
            return
        }

        quoteDB := database.NewQuoteDB(db)
        if err := quoteDB.Update(id, req); err != nil {
            if err == sql.ErrNoRows {
                utils.Error(w, utils.CodeNotFound, "Quote not found")
            } else {
                utils.Error(w, utils.CodeServerError, "Failed to update quote")
            }
            return
        }

        quote, _ := quoteDB.GetByID(id)
        utils.Success(w, quote.ToWithDisplay())
    }
}

func handleAdminDeleteQuote(db *sql.DB) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        // 获取 ID
        idStr := strings.TrimPrefix(r.URL.Path, "/api/admin/quotes/")
        idStr = strings.Split(idStr, "/")[0]

        id, err := strconv.ParseInt(idStr, 10, 64)
        if err != nil {
            utils.Error(w, utils.CodeParamError, "Invalid quote ID")
            return
        }

        quoteDB := database.NewQuoteDB(db)
        if err := quoteDB.Delete(id); err != nil {
            if err == sql.ErrNoRows {
                utils.Error(w, utils.CodeNotFound, "Quote not found")
            } else {
                utils.Error(w, utils.CodeServerError, "Failed to delete quote")
            }
            return
        }

        utils.Success(w, map[string]string{"message": "Deleted successfully"})
    }
}

func handleAdminBatchImport(db *sql.DB) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        var req models.BatchImportRequest
        if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
            utils.Error(w, utils.CodeParamError, "Invalid request body")
            return
        }

        if len(req.Quotes) == 0 || len(req.Quotes) > 100 {
            utils.Error(w, utils.CodeParamError, "Batch size must be 1-100")
            return
        }

        quoteDB := database.NewQuoteDB(db)
        created, err := quoteDB.BatchCreate(req.Quotes)
        if err != nil {
            utils.Error(w, utils.CodeServerError, "Failed to create quotes")
            return
        }

        displayQuotes := make([]models.QuoteWithDisplay, len(created))
        for i, q := range created {
            displayQuotes[i] = q.ToWithDisplay()
        }

        utils.Success(w, map[string]interface{}{
            "created": displayQuotes,
            "count":   len(created),
        })
    }
}
```

- [ ] **Step 3: 添加缺失的导入**

更新 handlers/admin.go 文件顶部：

```go
package handlers

import (
    "database/sql"
    "encoding/json"
    "net/http"
    "strconv"
    "strings"

    "trading-api/database"
    "trading-api/models"
    "trading-api/utils"
)
```

- [ ] **Step 4: 更新 main.go 使用正确的路由**

更新 main.go 中的路由配置，添加 gorilla/mux 的正确支持：

```go
// /opt/trading-api/main.go (更新路由部分)
func main() {
    // ... 数据库连接代码保持不变 ...

    // 创建路由
    r := mux.NewRouter()

    // 公共 API
    r.HandleFunc("/api/quotes", handlers.HandleGetQuotes(db)).Methods("GET")
    r.HandleFunc("/api/quotes/random", handlers.HandleGetRandomQuote(db)).Methods("GET")
    r.HandleFunc("/api/quotes/{id}", handlers.HandleGetQuoteByID(db)).Methods("GET")
    r.HandleFunc("/api/quotes/categories", handlers.HandleGetCategories).Methods("GET")
    r.HandleFunc("/api/quotes/market-types", handlers.HandleGetMarketTypes).Methods("GET")
    r.HandleFunc("/api/system/quotes", handlers.HandleGetSystemQuotes(db)).Methods("GET")

    // 管理 API
    r.HandleFunc("/api/admin/login", handlers.HandleAdminLogin(db)).Methods("POST")
    r.HandleFunc("/api/admin/quotes", handlers.AuthMiddleware(handlers.HandleAdminGetQuotes(db), db)).Methods("GET")
    r.HandleFunc("/api/admin/quotes", handlers.AuthMiddleware(handlers.HandleAdminCreateQuote(db), db)).Methods("POST")
    r.HandleFunc("/api/admin/quotes/{id}", handlers.AuthMiddleware(handlers.HandleAdminUpdateQuote(db), db)).Methods("PUT")
    r.HandleFunc("/api/admin/quotes/{id}", handlers.AuthMiddleware(handlers.HandleAdminDeleteQuote(db), db)).Methods("DELETE")
    r.HandleFunc("/api/admin/quotes/batch", handlers.AuthMiddleware(handlers.HandleAdminBatchImport(db), db)).Methods("POST")

    // ... 服务器启动代码保持不变 ...
}
```

同时更新 handlers 包中的函数名为导出（首字母大写）：

在 handlers/public.go 中：
```go
// 将所有函数名首字母大写
func HandleGetQuotes(db *sql.DB) http.HandlerFunc { ... }
func HandleGetRandomQuote(db *sql.DB) http.HandlerFunc { ... }
func HandleGetQuoteByID(db *sql.DB) http.HandlerFunc { ... }
func HandleGetCategories(w http.ResponseWriter, r *http.Request) { ... }
func HandleGetMarketTypes(w http.ResponseWriter, r *http.Request) { ... }
func HandleGetSystemQuotes(db *sql.DB) http.HandlerFunc { ... }
```

在 handlers/middleware.go 中：
```go
func AuthMiddleware(next http.HandlerFunc, db *sql.DB) http.HandlerFunc { ... }
func HandleAdminLogin(db *sql.DB) http.HandlerFunc { ... }
```

在 handlers/admin.go 中：
```go
func HandleAdminGetQuotes(db *sql.DB) http.HandlerFunc { ... }
func HandleAdminCreateQuote(db *sql.DB) http.HandlerFunc { ... }
func HandleAdminUpdateQuote(db *sql.DB) http.HandlerFunc { ... }
func HandleAdminDeleteQuote(db *sql.DB) http.HandlerFunc { ... }
func HandleAdminBatchImport(db *sql.DB) http.HandlerFunc { ... }
```

- [ ] **Step 5: 提交**

```bash
git add handlers/admin.go handlers/middleware.go
git commit -m "feat: add admin API handlers and authentication middleware"
git add main.go
git commit -m "fix: update routes to use mux path variables"
```

---

### Task 7: 生成管理员密码哈希并初始化

**Files:**
- Create: `/opt/trading-api/scripts/gen-password.go`

- [ ] **Step 1: 创建密码生成脚本**

```go
// /opt/trading-api/scripts/gen-password.go
package main

import (
    "fmt"
    "golang.org/x/crypto/bcrypt"
)

func main() {
    password := "admin123" // 默认密码

    hash, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
    if err != nil {
        panic(err)
    }

    fmt.Printf("Password: %s\n", password)
    fmt.Printf("Hash: %s\n", string(hash))
    fmt.Printf("\nSQL:\nUPDATE admin_config SET admin_password_hash = '%s', updated_at = UNIX_TIMESTAMP() * 1000 WHERE id = 1;\n", string(hash))
}
```

- [ ] **Step 2: 生成密码哈希**

```bash
cd /opt/trading-api/scripts && go run gen-password.go
```

Expected: 输出密码哈希值和 SQL 更新语句

- [ ] **Step 3: 更新数据库中的密码哈希**

将输出的 SQL 语句复制到 MySQL 中执行

- [ ] **Step 4: 提交**

```bash
git add scripts/gen-password.go
git commit -m "feat: add password hash generation script"
```

---

### Task 8: 测试 Go API

**Files:**
- None (测试)

- [ ] **Step 1: 构建 Go API**

```bash
cd /opt/trading-api && go build -o trading-api main.go
```

Expected: 生成 trading-api 二进制文件

- [ ] **Step 2: 运行 API 服务器**

```bash
./trading-api
```

Expected: "Server starting on port 8080"

- [ ] **Step 3: 测试公共 API**

```bash
# 测试获取语录列表
curl http://localhost:8080/api/quotes

# 测试获取随机语录
curl http://localhost:8080/api/quotes/random

# 测试获取分类
curl http://localhost:8080/api/quotes/categories

# 测试获取系统语录
curl http://localhost:8080/api/system/quotes
```

Expected: 返回 JSON 格式的语录数据

- [ ] **Step 4: 测试管理登录**

```bash
curl -X POST http://localhost:8080/api/admin/login \
  -H "Content-Type: application/json" \
  -d '{"password":"admin123"}'
```

Expected: 返回 token

- [ ] **Step 5: 测试管理 API (使用 token)**

```bash
TOKEN="<从上一步获取的token>"

curl http://localhost:8080/api/admin/quotes \
  -H "Authorization: Bearer $TOKEN"

curl -X POST http://localhost:8080/api/admin/quotes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"测试语录","category":"RISK_MGMT","marketType":"GENERAL","source":"SYSTEM"}'
```

Expected: 成功获取和创建语录

- [ ] **Step 6: 停止服务器**

按 Ctrl+C 停止测试服务器

---

## 阶段二：Next.js 前端

### Task 9: 初始化 Next.js 项目

**Files:**
- Create: `/opt/trading-web/`
- Create: `/opt/trading-web/package.json`

- [ ] **Step 1: 创建 Next.js 项目**

```bash
cd /opt && npx create-next-app@latest trading-web --typescript --tailwind --app --no-src-dir --import-alias "@/*"
```

创建时选择：
- TypeScript: Yes
- ESLint: Yes
- Tailwind CSS: Yes
- `app/` directory: Yes
- `src/` directory: No
- Import alias: `@/*`

- [ ] **Step 2: 安装额外依赖**

```bash
cd /opt/trading-web
npm install @mui/material @emotion/react @emotion/styled @mui/icons-material
npm install axios
npm install --save-dev @types/node
```

- [ ] **Step 3: 配置 Next.js 为静态导出**

更新 next.config.js：

```javascript
// /opt/trading-web/next.config.js
/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'export',
  images: {
    unoptimized: true,
  },
  trailingSlash: true,
}

module.exports = nextConfig
```

- [ ] **Step 4: 提交**

```bash
git add package.json next.config.js tsconfig.json tailwind.config.ts
git commit -m "feat: initialize Next.js project with static export"
```

---

### Task 10: 配置主题和全局样式

**Files:**
- Create: `/opt/trading-web/lib/theme.ts`
- Create: `/opt/trading-web/styles/globals.css`
- Modify: `/opt/trading-web/app/layout.tsx`

- [ ] **Step 1: 创建主题配置**

```typescript
// /opt/trading-web/lib/theme.ts
export const theme = {
  colors: {
    primary: '#3B82F6',
    primaryContainer: '#1E3A5F',
    onPrimary: '#FFFFFF',
    onPrimaryContainer: '#DBEAFE',
    secondary: '#2563EB',
    accent: '#059669',
    background: '#0F172A',
    surface: '#1E293B',
    onBackground: '#F8FAFC',
    onSurface: '#F8FAFC',
    muted: '#334155',
    border: '#334155',
    error: '#DC2626',
  },
  categoryColors: {
    RISK_MGMT: '#0EA5E9',   // 天蓝
    MINDSET: '#F59E0B',     // 橙色
    DISCIPLINE: '#10B981',  // 绿色
    TECHNICAL: '#8B5CF6',   // 紫色
  },
  categoryLabels: {
    RISK_MGMT: '风险管理',
    MINDSET: '交易心态',
    DISCIPLINE: '交易纪律',
    TECHNICAL: '技术分析',
  },
  marketTypeLabels: {
    STOCK: '股票',
    FUTURES: '期货',
    GENERAL: '通用',
  },
} as const;

export type Category = keyof typeof theme.categoryColors;
export type MarketType = keyof typeof theme.marketTypeLabels;
```

- [ ] **Step 2: 更新全局样式**

```css
/* /opt/trading-web/styles/globals.css */
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  :root {
    --color-background: #0F172A;
    --color-surface: #1E293B;
    --color-primary: #3B82F6;
    --color-border: #334155;
    --color-text: #F8FAFC;
    --color-text-muted: #94A3B8;
  }

  * {
    @apply border-border;
  }

  body {
    @apply bg-background text-text;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  }

  a {
    @apply text-primary hover:underline;
  }
}

@layer components {
  .btn {
    @apply px-4 py-2 rounded-lg font-medium transition-colors;
  }

  .btn-primary {
    @apply bg-primary text-white hover:bg-primary/90;
  }

  .btn-secondary {
    @apply bg-surface text-text hover:bg-surface/80 border border-border;
  }

  .card {
    @apply bg-surface rounded-xl p-6 border border-border;
  }

  .input {
    @apply bg-background border border-border rounded-lg px-4 py-2 text-text focus:outline-none focus:ring-2 focus:ring-primary;
  }
}
```

- [ ] **Step 3: 更新根布局**

```tsx
// /opt/trading-web/app/layout.tsx
import type { Metadata } from 'next'
import '../styles/globals.css'

export const metadata: Metadata = {
  title: '交易智慧 - 让交易智慧常伴左右',
  description: '每日交易语录，帮助投资者建立正确的交易心态和纪律',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="zh-CN">
      <head>
        <link rel="icon" href="/favicon.ico" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
      </head>
      <body className="min-h-screen">
        {children}
      </body>
    </html>
  )
}
```

- [ ] **Step 4: 提交**

```bash
git add lib/theme.ts styles/globals.css app/layout.tsx
git commit -m "feat: add theme configuration and global styles"
```

---

### Task 11: 创建 API 客户端

**Files:**
- Create: `/opt/trading-web/lib/api.ts`

- [ ] **Step 1: 创建 API 客户端**

```typescript
// /opt/trading-web/lib/api.ts
import axios from 'axios';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || '';

export interface Quote {
  id: number;
  content: string;
  category: string;
  categoryDisplay: string;
  marketType: string;
  marketTypeDisplay: string;
  source: string;
  isFavorite: boolean;
  viewCount?: number;
  createdAt: number;
}

export interface QuoteListResponse {
  quotes: Quote[];
  total: number;
  limit: number;
  offset: number;
}

export interface CategoryLabel {
  value: string;
  label: string;
}

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 公共 API
export const quotesApi = {
  getList: (params?: {
    category?: string;
    marketType?: string;
    source?: string;
    limit?: number;
    offset?: number;
  }) =>
    api.get<QuoteListResponse>('/api/quotes', { params }),

  getRandom: (params?: { category?: string; marketType?: string }) =>
    api.get<Quote>('/api/quotes/random', { params }),

  getById: (id: number) =>
    api.get<Quote>(`/api/quotes/${id}`),

  getCategories: () =>
    api.get<CategoryLabel[]>('/api/quotes/categories'),

  getMarketTypes: () =>
    api.get<CategoryLabel[]>('/api/quotes/market-types'),

  getSystemQuotes: () =>
    api.get<Quote[]>('/api/system/quotes'),
};

// 管理 API
export const adminApi = {
  login: (password: string) =>
    api.post<{ token: string }>('/api/admin/login', { password }),

  // 设置 token
  setToken: (token: string) => {
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  },

  clearToken: () => {
    delete api.defaults.headers.common['Authorization'];
  },

  getQuotes: () =>
    api.get<{ quotes: Quote[]; total: number }>('/api/admin/quotes'),

  createQuote: (data: {
    content: string;
    category: string;
    marketType: string;
    source: string;
  }) =>
    api.post<Quote>('/api/admin/quotes', data),

  updateQuote: (id: number, data: {
    content?: string;
    category?: string;
    marketType?: string;
  }) =>
    api.put<Quote>(`/api/admin/quotes/${id}`, data),

  deleteQuote: (id: number) =>
    api.delete<{ message: string }>(`/api/admin/quotes/${id}`),

  batchImport: (quotes: Array<{
    content: string;
    category: string;
    marketType: string;
  }>) =>
    api.post<{ created: Quote[]; count: number }>('/api/admin/quotes/batch', { quotes }),
};

export default api;
```

- [ ] **Step 2: 提交**

```bash
git add lib/api.ts
git commit -m "feat: add API client"
```

---

### Task 12: 创建公共组件

**Files:**
- Create: `/opt/trading-web/components/Header.tsx`
- Create: `/opt/trading-web/components/Footer.tsx`
- Create: `/opt/trading-web/components/QuoteCard.tsx`
- Create: `/opt/trading-web/components/CategoryFilter.tsx`

- [ ] **Step 1: 创建 Header 组件**

```tsx
// /opt/trading-web/components/Header.tsx
import Link from 'next/link';

export default function Header() {
  return (
    <header className="sticky top-0 z-50 w-full border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto px-4 h-16 flex items-center justify-between">
        <Link href="/" className="text-xl font-bold text-primary">
          交易智慧
        </Link>
        <nav className="flex gap-6">
          <Link href="/" className="text-sm text-text hover:text-primary transition-colors">
            首页
          </Link>
          <Link href="/quotes" className="text-sm text-text hover:text-primary transition-colors">
            语录
          </Link>
          <Link href="/about" className="text-sm text-text hover:text-primary transition-colors">
            关于
          </Link>
        </nav>
      </div>
    </header>
  );
}
```

- [ ] **Step 2: 创建 Footer 组件**

```tsx
// /opt/trading-web/components/Footer.tsx'
export default function Footer() {
  return (
    <footer className="w-full border-t border-border bg-surface py-8">
      <div className="container mx-auto px-4 text-center">
        <p className="text-text-muted text-sm">
          © 2024 交易智慧. 让交易智慧常伴左右.
        </p>
      </div>
    </footer>
  );
}
```

- [ ] **Step 3: 创建 QuoteCard 组件**

```tsx
// /opt/trading-web/components/QuoteCard.tsx
import { Quote } from '@/lib/api';
import { theme } from '@/lib/theme';

interface QuoteCardProps {
  quote: Quote;
  showActions?: boolean;
}

export default function QuoteCard({ quote, showActions = false }: QuoteCardProps) {
  const categoryColor = theme.categoryColors[quote.category as keyof typeof theme.categoryColors] || '#64748B';

  return (
    <div className="card max-w-2xl mx-auto">
      <p className="text-xl md:text-2xl text-text leading-relaxed mb-4">
        {quote.content}
      </p>
      <div className="flex items-center justify-between text-sm">
        <span
          className="px-3 py-1 rounded-full text-xs font-medium"
          style={{ backgroundColor: `${categoryColor}20`, color: categoryColor }}
        >
          {quote.categoryDisplay}
        </span>
        {showActions && quote.viewCount !== undefined && (
          <span className="text-text-muted">
            ❤️ {quote.viewCount}
          </span>
        )}
      </div>
    </div>
  );
}
```

- [ ] **Step 4: 创建 CategoryFilter 组件**

```tsx
// /opt/trading-web/components/CategoryFilter.tsx
'use client';

import { theme } from '@/lib/theme';

interface CategoryFilterProps {
  selected: string | null;
  onSelect: (category: string | null) => void;
}

export default function CategoryFilter({ selected, onSelect }: CategoryFilterProps) {
  const categories = [
    { value: null, label: '全部' },
    ...Object.entries(theme.categoryLabels).map(([value, label]) => ({ value, label })),
  ];

  return (
    <div className="flex gap-2 flex-wrap justify-center">
      {categories.map((cat) => {
        const isSelected = selected === cat.value;
        return (
          <button
            key={cat.value || 'all'}
            onClick={() => onSelect(cat.value)}
            className={`
              px-4 py-2 rounded-full text-sm font-medium transition-all
              ${isSelected
                ? 'bg-primary text-white shadow-lg shadow-primary/25'
                : 'bg-surface text-text-muted hover:text-text border border-border'
              }
            `}
          >
            {cat.label}
          </button>
        );
      })}
    </div>
  );
}
```

- [ ] **Step 5: 提交**

```bash
git add components/Header.tsx components/Footer.tsx components/QuoteCard.tsx components/CategoryFilter.tsx
git commit -m "feat: add common components (Header, Footer, QuoteCard, CategoryFilter)"
```

---

### Task 13: 创建首页

**Files:**
- Create: `/opt/trading-web/app/page.tsx`

- [ ] **Step 1: 创建首页组件**

```tsx
// /opt/trading-web/app/page.tsx
import Link from 'next/link';
import Header from '@/components/Header';
import Footer from '@/components/Footer';

export default function HomePage() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 flex items-center justify-center px-4">
        <div className="text-center max-w-2xl">
          <h1 className="text-5xl md:text-6xl font-bold text-primary mb-6">
            交易智慧
          </h1>
          <p className="text-xl text-text-muted mb-12">
            让交易智慧常伴左右
          </p>

          <div className="flex gap-4 justify-center">
            <Link
              href="/quotes"
              className="btn btn-primary"
            >
              浏览语录
            </Link>
            <a
              href="#download"
              className="btn btn-secondary"
            >
              下载 App
            </a>
          </div>

          <div className="mt-16 grid grid-cols-3 gap-8">
            <div>
              <div className="text-3xl font-bold text-primary mb-2">1000+</div>
              <div className="text-sm text-text-muted">精选语录</div>
            </div>
            <div>
              <div className="text-3xl font-bold text-primary mb-2">4</div>
              <div className="text-sm text-text-muted">分类</div>
            </div>
            <div>
              <div className="text-3xl font-bold text-primary mb-2">免费</div>
              <div className="text-sm text-text-muted">完全免费</div>
            </div>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
}
```

- [ ] **Step 2: 测试构建**

```bash
cd /opt/trading-web && npm run build
```

Expected: 构建成功，输出到 `out/` 目录

- [ ] **Step 3: 提交**

```bash
git add app/page.tsx
git commit -m "feat: add home page with minimalist design"
```

---

### Task 14: 创建语录浏览页

**Files:**
- Create: `/opt/trading-web/app/quotes/page.tsx`

- [ ] **Step 1: 创建语录浏览页**

```tsx
// /opt/trading-web/app/quotes/page.tsx
'use client';

import { useState, useEffect } from 'react';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import QuoteCard from '@/components/QuoteCard';
import CategoryFilter from '@/components/CategoryFilter';
import { quotesApi, Quote } from '@/lib/api';
import { theme } from '@/lib/theme';

export default function QuotesPage() {
  const [quote, setQuote] = useState<Quote | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchQuote = async () => {
    setLoading(true);
    try {
      const params = selectedCategory ? { category: selectedCategory } : undefined;
      const response = await quotesApi.getRandom(params);
      setQuote(response.data);
    } catch (error) {
      console.error('Failed to fetch quote:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchQuote();
  }, [selectedCategory]);

  const handleCategoryChange = (category: string | null) => {
    setSelectedCategory(category);
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 container mx-auto px-4 py-8">
        <div className="max-w-2xl mx-auto">
          <CategoryFilter
            selected={selectedCategory}
            onSelect={handleCategoryChange}
          />

          <div className="mt-8 min-h-[300px] flex items-center justify-center">
            {loading ? (
              <div className="text-text-muted">加载中...</div>
            ) : quote ? (
              <QuoteCard quote={quote} showActions />
            ) : (
              <div className="text-text-muted">暂无语录</div>
            )}
          </div>

          <div className="mt-8 flex justify-center">
            <button
              onClick={fetchQuote}
              disabled={loading}
              className="btn btn-primary flex items-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              换一换
            </button>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
}
```

- [ ] **Step 2: 提交**

```bash
git add app/quotes/page.tsx
git commit -m "feat: add quotes browse page with single-quote layout"
```

---

### Task 15: 创建关于页面

**Files:**
- Create: `/opt/trading-web/app/about/page.tsx`

- [ ] **Step 1: 创建关于页面**

```tsx
// /opt/trading-web/app/about/page.tsx
import Header from '@/components/Header';
import Footer from '@/components/Footer';

export default function AboutPage() {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />

      <main className="flex-1 container mx-auto px-4 py-12">
        <div className="max-w-2xl mx-auto">
          <h1 className="text-3xl font-bold text-primary mb-8">关于交易智慧</h1>

          <div className="space-y-6 text-text-muted">
            <p>
              交易智慧是一款专注于交易心态和投资理念的 App，每日推送精选交易语录，
              帮助投资者建立正确的交易心态和纪律。
            </p>

            <div className="card">
              <h2 className="text-lg font-semibold text-text mb-4">功能特色</h2>
              <ul className="space-y-2">
                <li>• 每日精选交易语录推送</li>
                <li>• 按分类浏览：风险管理、交易心态、交易纪律、技术分析</li>
                <li>• 收藏喜欢的语录</li>
                <li>• 分享语录到社交平台</li>
                <li>• 自定义提醒时间</li>
              </ul>
            </div>

            <div className="card">
              <h2 className="text-lg font-semibold text-text mb-4">联系我们</h2>
              <p>如有问题或建议，欢迎通过以下方式联系：</p>
              <p className="mt-2">邮箱: contact@trading.tangping.me</p>
            </div>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
}
```

- [ ] **Step 2: 提交**

```bash
git add app/about/page.tsx
git commit -m "feat: add about page"
```

---

### Task 16: 创建管理后台组件

**Files:**
- Create: `/opt/trading-web/components/admin/Sidebar.tsx`
- Create: `/opt/trading-web/components/admin/QuoteList.tsx`
- Create: `/opt/trading-web/components/admin/QuoteEditor.tsx`

- [ ] **Step 1: 创建侧边栏组件**

```tsx
// /opt/trading-web/components/admin/Sidebar.tsx
import Link from 'next/link';
import { usePathname } from 'next/navigation';

interface SidebarProps {
  onLogout?: () => void;
}

export default function Sidebar({ onLogout }: SidebarProps) {
  const pathname = usePathname();

  const isActive = (path: string) => pathname?.startsWith(path);

  return (
    <aside className="w-64 bg-surface border-r border-border min-h-screen p-4">
      <div className="text-lg font-bold text-primary mb-8">管理后台</div>

      <nav className="space-y-2">
        <Link
          href="/xyz-admin/dashboard"
          className={`
            block px-4 py-2 rounded-lg transition-colors
            ${isActive('/xyz-admin/dashboard') ? 'bg-primary text-white' : 'text-text-muted hover:text-text hover:bg-border'}
          `}
        >
          📝 语录管理
        </Link>
        <div className="px-4 py-2 text-text-muted opacity-50">
          📊 统计 (待开发)
        </div>
        <div className="px-4 py-2 text-text-muted opacity-50">
          ⚙️ 设置 (待开发)
        </div>
      </nav>

      {onLogout && (
        <button
          onClick={onLogout}
          className="absolute bottom-4 left-4 right-4 px-4 py-2 text-text-muted hover:text-red-400 transition-colors"
        >
          退出登录
        </button>
      )}
    </aside>
  );
}
```

- [ ] **Step 2: 创建语录列表组件**

```tsx
// /opt/trading-web/components/admin/QuoteList.tsx
'use client';

import { Quote } from '@/lib/api';
import { theme } from '@/lib/theme';

interface QuoteListProps {
  quotes: Quote[];
  onEdit: (quote: Quote) => void;
  onDelete: (id: number) => void;
}

export default function QuoteList({ quotes, onEdit, onDelete }: QuoteListProps) {
  return (
    <div className="space-y-3">
      {quotes.map((quote) => {
        const categoryColor = theme.categoryColors[quote.category as keyof typeof theme.categoryColors];
        return (
          <div
            key={quote.id}
            className="card flex items-center justify-between gap-4"
          >
            <div className="flex-1">
              <p className="text-text mb-2">{quote.content}</p>
              <div className="flex gap-2">
                <span
                  className="text-xs px-2 py-1 rounded"
                  style={{ backgroundColor: `${categoryColor}20`, color: categoryColor }}
                >
                  {quote.categoryDisplay}
                </span>
                <span className="text-xs text-text-muted">
                  {quote.marketTypeDisplay}
                </span>
              </div>
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => onEdit(quote)}
                className="px-3 py-1 text-sm text-text-muted hover:text-primary transition-colors"
              >
                编辑
              </button>
              <button
                onClick={() => onDelete(quote.id)}
                className="px-3 py-1 text-sm text-text-muted hover:text-error transition-colors"
              >
                删除
              </button>
            </div>
          </div>
        );
      })}
    </div>
  );
}
```

- [ ] **Step 3: 创建语录编辑器组件**

```tsx
// /opt/trading-web/components/admin/QuoteEditor.tsx
'use client';

import { useState, useEffect } from 'react';
import { Quote } from '@/lib/api';
import { theme } from '@/lib/theme';

interface QuoteEditorProps {
  quote?: Quote;
  onSave: (data: { content: string; category: string; marketType: string }) => void;
  onCancel: () => void;
}

export default function QuoteEditor({ quote, onSave, onCancel }: QuoteEditorProps) {
  const [content, setContent] = useState(quote?.content || '');
  const [category, setCategory] = useState(quote?.category || 'RISK_MGMT');
  const [marketType, setMarketType] = useState(quote?.marketType || 'GENERAL');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (content.trim()) {
      onSave({ content: content.trim(), category, marketType });
    }
  };

  return (
    <div className="card">
      <h3 className="text-lg font-semibold text-text mb-4">
        {quote ? '编辑语录' : '新增语录'}
      </h3>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm text-text-muted mb-2">
            语录内容 *
          </label>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="输入交易语录..."
            className="input w-full h-24 resize-none"
            maxLength={500}
            required
          />
          <p className="text-xs text-text-muted mt-1">
            {content.length}/500
          </p>
        </div>

        <div>
          <label className="block text-sm text-text-muted mb-2">
            分类
          </label>
          <select
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            className="input w-full"
          >
            {Object.entries(theme.categoryLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-sm text-text-muted mb-2">
            市场类型
          </label>
          <select
            value={marketType}
            onChange={(e) => setMarketType(e.target.value)}
            className="input w-full"
          >
            {Object.entries(theme.marketTypeLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>

        <div className="flex gap-3">
          <button type="submit" className="btn btn-primary">
            保存
          </button>
          <button type="button" onClick={onCancel} className="btn btn-secondary">
            取消
          </button>
        </div>
      </form>
    </div>
  );
}
```

- [ ] **Step 4: 提交**

```bash
git add components/admin/Sidebar.tsx components/admin/QuoteList.tsx components/admin/QuoteEditor.tsx
git commit -m "feat: add admin components (Sidebar, QuoteList, QuoteEditor)"
```

---

### Task 17: 创建管理登录页面

**Files:**
- Create: `/opt/trading-web/app/xyz-admin/page.tsx`

- [ ] **Step 1: 创建登录页面**

```tsx
// /opt/trading-web/app/xyz-admin/page.tsx
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { adminApi } from '@/lib/api';

export default function AdminLoginPage() {
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await adminApi.login(password);
      const token = response.data.token;

      // 保存 token
      localStorage.setItem('adminToken', token);
      adminApi.setToken(token);

      router.push('/xyz-admin/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message || '登录失败，请检查密码');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background px-4">
      <div className="card w-full max-w-md">
        <h1 className="text-2xl font-bold text-primary mb-6 text-center">
          管理后台登录
        </h1>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm text-text-muted mb-2">
              管理密码
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="输入管理密码"
              className="input w-full"
              required
            />
          </div>

          {error && (
            <p className="text-sm text-error">{error}</p>
          )}

          <button
            type="submit"
            disabled={loading}
            className="btn btn-primary w-full"
          >
            {loading ? '登录中...' : '登录'}
          </button>
        </form>

        <p className="text-xs text-text-muted text-center mt-6">
          默认密码: admin123 (请登录后尽快修改)
        </p>
      </div>
    </div>
  );
}
```

- [ ] **Step 2: 提交**

```bash
git add app/xyz-admin/page.tsx
git commit -m "feat: add admin login page"
```

---

### Task 18: 创建管理后台主页

**Files:**
- Create: `/opt/trading-web/app/xyz-admin/dashboard/page.tsx`

- [ ] **Step 1: 创建管理后台主页**

```tsx
// /opt/trading-web/app/xyz-admin/dashboard/page.tsx
'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Sidebar from '@/components/admin/Sidebar';
import QuoteList from '@/components/admin/QuoteList';
import QuoteEditor from '@/components/admin/QuoteEditor';
import { adminApi, Quote } from '@/lib/api';

export default function AdminDashboardPage() {
  const router = useRouter();
  const [quotes, setQuotes] = useState<Quote[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingQuote, setEditingQuote] = useState<Quote | undefined>();
  const [showEditor, setShowEditor] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem('adminToken');
    if (!token) {
      router.push('/xyz-admin');
      return;
    }
    adminApi.setToken(token);
    fetchQuotes();
  }, []);

  const fetchQuotes = async () => {
    try {
      const response = await adminApi.getQuotes();
      setQuotes(response.data.quotes);
    } catch (error) {
      console.error('Failed to fetch quotes:', error);
      // Token 可能过期，跳转到登录页
      router.push('/xyz-admin');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingQuote(undefined);
    setShowEditor(true);
  };

  const handleEdit = (quote: Quote) => {
    setEditingQuote(quote);
    setShowEditor(true);
  };

  const handleSave = async (data: { content: string; category: string; marketType: string }) => {
    try {
      if (editingQuote) {
        await adminApi.updateQuote(editingQuote.id, data);
      } else {
        await adminApi.createQuote({
          ...data,
          source: 'SYSTEM',
        });
      }
      setShowEditor(false);
      fetchQuotes();
    } catch (error) {
      console.error('Failed to save quote:', error);
      alert('保存失败');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('确定要删除这条语录吗？')) return;

    try {
      await adminApi.deleteQuote(id);
      fetchQuotes();
    } catch (error) {
      console.error('Failed to delete quote:', error);
      alert('删除失败');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('adminToken');
    adminApi.clearToken();
    router.push('/xyz-admin');
  };

  return (
    <div className="flex min-h-screen bg-background">
      <Sidebar onLogout={handleLogout} />

      <main className="flex-1 p-8">
        <div className="max-w-4xl mx-auto">
          <div className="flex items-center justify-between mb-8">
            <h1 className="text-2xl font-bold text-text">语录管理</h1>
            <button
              onClick={handleCreate}
              className="btn btn-primary"
            >
              + 新增语录
            </button>
          </div>

          {loading ? (
            <p className="text-text-muted">加载中...</p>
          ) : showEditor ? (
            <QuoteEditor
              quote={editingQuote}
              onSave={handleSave}
              onCancel={() => setShowEditor(false)}
            />
          ) : quotes.length === 0 ? (
            <p className="text-text-muted">暂无语录</p>
          ) : (
            <QuoteList
              quotes={quotes}
              onEdit={handleEdit}
              onDelete={handleDelete}
            />
          )}
        </div>
      </main>
    </div>
  );
}
```

- [ ] **Step 2: 提交**

```bash
git add app/xyz-admin/dashboard/page.tsx
git commit -m "feat: add admin dashboard page"
```

---

## 阶段三：部署配置

### Task 19: 配置 Nginx

**Files:**
- Create: `/opt/trading-deploy/nginx.conf`

- [ ] **Step 1: 创建 Nginx 配置文件**

```nginx
# /opt/trading-deploy/nginx.conf
server {
    listen 80;
    server_name trading.tangping.me;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name trading.tangping.me;

    # SSL 证书路径 (acme.sh 会自动配置)
    ssl_certificate /root/.acme.sh/trading.tangping.me_ecc/fullchain.cer;
    ssl_certificate_key /root/.acme.sh/trading.tangping.me_ecc/trading.tangping.me.key;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

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
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

- [ ] **Step 2: 复制到 Nginx 配置目录**

```bash
# 在服务器上执行
sudo cp /opt/trading-deploy/nginx.conf /etc/nginx/sites-available/trading.tangping.me
sudo ln -s /etc/nginx/sites-available/trading.tangping.me /etc/nginx/sites-enabled/
sudo nginx -t
```

Expected: "syntax is ok"

- [ ] **Step 3: 提交**

```bash
git add trading-deploy/nginx.conf
git commit -m "feat: add nginx configuration"
```

---

### Task 20: 申请 SSL 证书

**Files:**
- Create: `/opt/trading-deploy/setup-ssl.sh`

- [ ] **Step 1: 创建 SSL 设置脚本**

```bash
#!/bin/bash
# /opt/trading-deploy/setup-ssl.sh

DOMAIN="trading.tangping.me"

# 检查 acme.sh 是否已安装
if ! command -v acme.sh &> /dev/null; then
    echo "安装 acme.sh..."
    curl https://get.acme.sh | sh
    source ~/.bashrc
fi

# 申请证书
echo "申请 SSL 证书..."
acme.sh --issue -d $DOMAIN --nginx /etc/nginx

# 安装证书
echo "安装证书..."
acme.sh --install-cert -d $DOMAIN --ecc \
  --key-file /etc/nginx/ssl/$DOMAIN.key \
  --fullchain-file /etc/nginx/ssl/$DOMAIN.cer \
  --reloadcmd "systemctl reload nginx"

# 创建 SSL 目录
sudo mkdir -p /etc/nginx/ssl

echo "SSL 证书配置完成!"
echo "证书将自动续期"
```

- [ ] **Step 2: 执行 SSL 设置**

```bash
# 在服务器上执行
chmod +x /opt/trading-deploy/setup-ssl.sh
sudo /opt/trading-deploy/setup-ssl.sh
```

Expected: SSL 证书申请成功

- [ ] **Step 3: 提交**

```bash
git add trading-deploy/setup-ssl.sh
git commit -m "feat: add SSL certificate setup script"
```

---

### Task 21: 配置 systemd 服务

**Files:**
- Create: `/opt/trading-deploy/trading-api.service`
- Create: `/opt/trading-deploy/trading-web.service`

- [ ] **Step 1: 创建 API 服务配置**

```ini
# /opt/trading-deploy/trading-api.service
[Unit]
Description=Trading Wisdom API
After=network.target mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/trading-api
Environment="DB_DSN=root:YOUR_PASSWORD@tcp(127.0.0.1:3306)/trading_wisdom?parseTime=true"
ExecStart=/opt/trading-api/trading-api
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

- [ ] **Step 2: 创建 Web 服务配置**

```ini
# /opt/trading-deploy/trading-web.service
[Unit]
Description=Trading Wisdom Web
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/trading-web
ExecStart=/usr/bin/npm run start
Restart=on-failure
RestartSec=5
Environment=NODE_ENV=production
Environment=PORT=3000

[Install]
WantedBy=multi-user.target
```

- [ ] **Step 3: 安装并启动服务**

```bash
# 在服务器上执行

# 复制服务文件
sudo cp /opt/trading-deploy/trading-api.service /etc/systemd/system/
sudo cp /opt/trading-deploy/trading-web.service /etc/systemd/system/

# 修改 API 服务中的数据库密码
sudo nano /etc/systemd/system/trading-api.service

# 重载 systemd
sudo systemctl daemon-reload

# 启动并启用服务
sudo systemctl enable trading-api
sudo systemctl enable trading-web
sudo systemctl start trading-api
sudo systemctl start trading-web

# 检查状态
sudo systemctl status trading-api
sudo systemctl status trading-web
```

- [ ] **Step 4: 提交**

```bash
git add trading-deploy/trading-api.service trading-deploy/trading-web.service
git commit -m "feat: add systemd service configurations"
```

---

### Task 22: 最终测试

**Files:**
- None

- [ ] **Step 1: 测试网站访问**

```bash
curl -I https://trading.tangping.me
```

Expected: 200 OK

- [ ] **Step 2: 测试 API**

```bash
curl https://trading.tangping.me/api/quotes/random
```

Expected: 返回语录 JSON

- [ ] **Step 3: 测试管理后台**

在浏览器访问 `https://trading.tangping.me/xyz-admin`，使用默认密码 `admin123` 登录

Expected: 登录成功，显示管理后台

- [ ] **Step 4: 检查服务状态**

```bash
sudo systemctl status trading-api trading-web nginx
```

Expected: 所有服务处于 active (running) 状态

- [ ] **Step 5: 最终提交**

```bash
git add .
git commit -m "feat: complete trading wisdom website and admin backend implementation"
```

---

## 附录：API 接口文档

### 公共 API

#### 获取语录列表
```
GET /api/quotes
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| category | string | 否 | RISK_MGMT, MINDSET, DISCIPLINE, TECHNICAL |
| marketType | string | 否 | STOCK, FUTURES, GENERAL |
| source | string | 否 | SYSTEM, USER |
| limit | int | 否 | 默认 20，最大 100 |
| offset | int | 否 | 默认 0 |

#### 获取随机语录
```
GET /api/quotes/random
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| category | string | 否 | 指定分类 |
| marketType | string | 否 | 指定市场类型 |

#### 获取系统预设语录
```
GET /api/system/quotes
```

用于 App 首次安装时初始化本地数据库。

### 管理 API

#### 管理员登录
```
POST /api/admin/login
Content-Type: application/json

{
  "password": "admin123"
}
```

响应:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "trading-admin-session-2024"
  }
}
```

管理 API 请求需在 Header 中携带:
```
Authorization: Bearer {token}
```

---

## 完成

所有任务完成后，网站将包含：

1. **公开站点**
   - 首页 (`/`)
   - 语录浏览页 (`/quotes`)
   - 关于页面 (`/about`)

2. **管理后台**
   - 登录页 (`/xyz-admin`)
   - 管理面板 (`/xyz-admin/dashboard`)

3. **API 服务**
   - 公共 API (`/api/quotes/*`, `/api/system/quotes`)
   - 管理 API (`/api/admin/*`)

4. **部署配置**
   - Nginx 反向代理配置
   - SSL 证书自动续期
   - systemd 服务自动启动
