// /opt/trading-api/models/quote.go
package models

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
