// /home/mnyagent/tradeyourplan/trading-api/handlers/public.go
package handlers

import (
	"database/sql"
	"net/http"
	"strconv"
	"strings"

	"github.com/gorilla/mux"
	"trading-api/database"
	"trading-api/models"
	"trading-api/utils"
)

// isValidCategory 验证分类枚举值是否有效
func isValidCategory(category string) bool {
	switch models.Category(category) {
	case models.CategoryRiskMgmt, models.CategoryMindset, models.CategoryDiscipline, models.CategoryTechnical:
		return true
	default:
		return false
	}
}

// isValidMarketType 验证市场类型枚举值是否有效
func isValidMarketType(marketType string) bool {
	switch models.MarketType(marketType) {
	case models.MarketTypeStock, models.MarketTypeFutures, models.MarketTypeGeneral:
		return true
	default:
		return false
	}
}

// isValidSource 验证来源枚举值是否有效
func isValidSource(source string) bool {
	switch models.QuoteSource(strings.ToUpper(source)) {
	case models.SourceSystem, models.SourceUser:
		return true
	default:
		return false
	}
}

// QuoteDB 数据库操作接口
type QuoteDB interface {
	GetAll(category models.Category, marketType models.MarketType, source models.QuoteSource, limit, offset int) ([]models.Quote, int, error)
	GetRandom(category models.Category, marketType models.MarketType) (*models.Quote, error)
	GetByID(id int64) (*models.Quote, error)
	GetSystemQuotes() ([]models.Quote, error)
}

// PagedQuotesResponse 分页语录响应
type PagedQuotesResponse struct {
	Quotes []models.QuoteWithDisplay `json:"quotes"`
	Total  int                       `json:"total"`
	Limit  int                       `json:"limit"`
	Offset int                       `json:"offset"`
}

// HandleGetQuotes GET /api/quotes - 获取语录列表（支持筛选和分页）
func HandleGetQuotes(db *sql.DB) http.HandlerFunc {
	quoteDB := database.NewQuoteDB(db)
	return func(w http.ResponseWriter, r *http.Request) {
		// 解析查询参数
		categoryStr := r.URL.Query().Get("category")
		marketTypeStr := r.URL.Query().Get("marketType")
		sourceStr := r.URL.Query().Get("source")

		// 验证枚举值
		var category models.Category
		if categoryStr != "" {
			if !isValidCategory(categoryStr) {
				utils.Error(w, utils.CodeParamError, "Invalid category value")
				return
			}
			category = models.Category(categoryStr)
		}

		var marketType models.MarketType
		if marketTypeStr != "" {
			if !isValidMarketType(marketTypeStr) {
				utils.Error(w, utils.CodeParamError, "Invalid marketType value")
				return
			}
			marketType = models.MarketType(marketTypeStr)
		}

		source := models.QuoteSource(strings.ToUpper(sourceStr))
		if sourceStr != "" {
			if !isValidSource(sourceStr) {
				utils.Error(w, utils.CodeParamError, "Invalid source value")
				return
			}
			source = models.QuoteSource(strings.ToUpper(sourceStr))
		}

		// 解析分页参数
		limit := 20
		offset := 0

		if limitStr := r.URL.Query().Get("limit"); limitStr != "" {
			if l, err := strconv.Atoi(limitStr); err == nil && l > 0 && l <= 100 {
				limit = l
			} else if l > 100 {
				limit = 100
			}
		}

		if offsetStr := r.URL.Query().Get("offset"); offsetStr != "" {
			if o, err := strconv.Atoi(offsetStr); err == nil && o >= 0 {
				offset = o
			}
		}

		// 查询数据
		quotes, total, err := quoteDB.GetAll(category, marketType, source, limit, offset)
		if err != nil {
			utils.Error(w, utils.CodeServerError, "Failed to fetch quotes")
			return
		}

		// 转换为带显示名称的格式
		quotesWithDisplay := make([]models.QuoteWithDisplay, len(quotes))
		for i, quote := range quotes {
			quotesWithDisplay[i] = quote.ToWithDisplay()
		}

		// 返回分页结果
		utils.Success(w, PagedQuotesResponse{
			Quotes: quotesWithDisplay,
			Total:  total,
			Limit:  limit,
			Offset: offset,
		})
	}
}

// HandleGetRandomQuote GET /api/quotes/random - 获取随机语录
func HandleGetRandomQuote(db *sql.DB) http.HandlerFunc {
	quoteDB := database.NewQuoteDB(db)
	return func(w http.ResponseWriter, r *http.Request) {
		// 解析查询参数
		categoryStr := r.URL.Query().Get("category")
		marketTypeStr := r.URL.Query().Get("marketType")

		// 验证枚举值
		var category models.Category
		if categoryStr != "" {
			if !isValidCategory(categoryStr) {
				utils.Error(w, utils.CodeParamError, "Invalid category value")
				return
			}
			category = models.Category(categoryStr)
		}

		var marketType models.MarketType
		if marketTypeStr != "" {
			if !isValidMarketType(marketTypeStr) {
				utils.Error(w, utils.CodeParamError, "Invalid marketType value")
				return
			}
			marketType = models.MarketType(marketTypeStr)
		}

		// 查询随机语录
		quote, err := quoteDB.GetRandom(category, marketType)
		if err != nil {
			utils.Error(w, utils.CodeServerError, "Failed to fetch random quote")
			return
		}

		if quote == nil {
			utils.Error(w, utils.CodeNotFound, "No quotes found")
			return
		}

		utils.Success(w, quote.ToWithDisplay())
	}
}

// HandleGetQuoteByID GET /api/quotes/:id - 根据 ID 获取语录
func HandleGetQuoteByID(db *sql.DB) http.HandlerFunc {
	quoteDB := database.NewQuoteDB(db)
	return func(w http.ResponseWriter, r *http.Request) {

		// 从 URL 获取 ID
		vars := mux.Vars(r)
		idStr := vars["id"]

		id, err := strconv.ParseInt(idStr, 10, 64)
		if err != nil {
			utils.Error(w, utils.CodeParamError, "Invalid quote ID")
			return
		}

		if id <= 0 {
			utils.Error(w, utils.CodeParamError, "Invalid quote ID")
			return
		}

		// 查询语录
		quote, err := quoteDB.GetByID(id)
		if err != nil {
			utils.Error(w, utils.CodeServerError, "Failed to fetch quote")
			return
		}

		if quote == nil {
			utils.Error(w, utils.CodeNotFound, "Quote not found")
			return
		}

		utils.Success(w, quote.ToWithDisplay())
	}
}

// HandleGetCategories GET /api/quotes/categories - 获取分类列表
func HandleGetCategories(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	categories := []map[string]string{
		{"value": "RISK_MGMT", "label": "风险管理"},
		{"value": "MINDSET", "label": "交易心态"},
		{"value": "DISCIPLINE", "label": "交易纪律"},
		{"value": "TECHNICAL", "label": "技术分析"},
	}

	utils.Success(w, categories)
}

// HandleGetMarketTypes GET /api/quotes/market-types - 获取市场类型列表
func HandleGetMarketTypes(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	marketTypes := []map[string]string{
		{"value": "STOCK", "label": "股票"},
		{"value": "FUTURES", "label": "期货"},
		{"value": "GENERAL", "label": "通用"},
	}

	utils.Success(w, marketTypes)
}

// HandleGetSystemQuotes GET /api/system/quotes - 获取系统预设语录
func HandleGetSystemQuotes(db *sql.DB) http.HandlerFunc {
	quoteDB := database.NewQuoteDB(db)
	return func(w http.ResponseWriter, r *http.Request) {

		// 查询系统语录
		quotes, err := quoteDB.GetSystemQuotes()
		if err != nil {
			utils.Error(w, utils.CodeServerError, "Failed to fetch system quotes")
			return
		}

		// 转换为带显示名称的格式
		quotesWithDisplay := make([]models.QuoteWithDisplay, len(quotes))
		for i, quote := range quotes {
			quotesWithDisplay[i] = quote.ToWithDisplay()
		}

		utils.Success(w, quotesWithDisplay)
	}
}

// HealthCheckHandler 健康检查端点
func HealthCheckHandler(w http.ResponseWriter, r *http.Request) {
	utils.Success(w, map[string]string{
		"status": "ok",
	})
}
