// /home/mnyagent/tradeyourplan/trading-api/handlers/admin.go
package handlers

import (
	"database/sql"
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/gorilla/mux"
	"golang.org/x/crypto/bcrypt"
	"trading-api/database"
	"trading-api/middleware"
	"trading-api/models"
	"trading-api/utils"
)

// AdminQuoteDB 管理员数据库操作接口
type AdminQuoteDB interface {
	GetAll(category models.Category, marketType models.MarketType, source models.QuoteSource, limit, offset int) ([]models.Quote, int, error)
	GetByID(id int64) (*models.Quote, error)
	Create(req models.CreateQuoteRequest) (*models.Quote, error)
	Update(id int64, req models.UpdateQuoteRequest) error
	Delete(id int64) error
	BatchCreate(quotes []models.CreateQuoteRequest) ([]models.Quote, error)
	GetAdminPasswordHash() (string, error)
}

// HandleAdminLogin POST /api/admin/login - 管理员登录
func HandleAdminLogin(db *sql.DB) http.HandlerFunc {
	quoteDB := database.NewQuoteDB(db)
	return func(w http.ResponseWriter, r *http.Request) {
		// 解析请求体
		var req models.AdminLoginRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			utils.Error(w, utils.CodeParamError, "Invalid request body")
			return
		}

		// 验证密码
		if !verifyAdminPassword(quoteDB, req.Password) {
			utils.Error(w, utils.CodePasswordErr, "Invalid password")
			return
		}

		// 生成 token
		token := middleware.GenerateLoginToken()

		// 返回 token
		utils.Success(w, map[string]string{
			"token": token,
		})
	}
}

// verifyAdminPassword 验证管理员密码
func verifyAdminPassword(db AdminQuoteDB, password string) bool {
	hash, err := db.GetAdminPasswordHash()
	if err != nil {
		return false
	}

	// 使用 bcrypt 验证密码
	err = bcrypt.CompareHashAndPassword([]byte(hash), []byte(password))
	return err == nil
}

// HandleAdminGetQuotes GET /api/admin/quotes - 获取所有语录（管理员）
func HandleAdminGetQuotes(db *sql.DB) http.HandlerFunc {
	quoteDB := database.NewQuoteDB(db)
	return middleware.AuthMiddleware(func(w http.ResponseWriter, r *http.Request) {
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

		source := models.QuoteSource(sourceStr)
		if sourceStr != "" {
			if !isValidSource(sourceStr) {
				utils.Error(w, utils.CodeParamError, "Invalid source value")
				return
			}
			source = models.QuoteSource(sourceStr)
		}

		// 解析分页参数
		limit := 100 // 管理员默认更多
		offset := 0

		if limitStr := r.URL.Query().Get("limit"); limitStr != "" {
			if l, err := strconv.Atoi(limitStr); err == nil && l > 0 && l <= 500 {
				limit = l
			} else if l > 500 {
				limit = 500
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
	})
}

// HandleAdminCreateQuote POST /api/admin/quotes - 创建语录
func HandleAdminCreateQuote(db *sql.DB) http.HandlerFunc {
	quoteDB := database.NewQuoteDB(db)
	return middleware.AuthMiddleware(func(w http.ResponseWriter, r *http.Request) {
		// 解析请求体
		var req models.CreateQuoteRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			utils.Error(w, utils.CodeParamError, "Invalid request body")
			return
		}

		// 验证必填字段
		if req.Content == "" {
			utils.Error(w, utils.CodeParamError, "Content is required")
			return
		}

		if req.Category == "" {
			utils.Error(w, utils.CodeParamError, "Category is required")
			return
		}

		if !isValidCategory(string(req.Category)) {
			utils.Error(w, utils.CodeParamError, "Invalid category value")
			return
		}

		if req.MarketType == "" {
			req.MarketType = models.MarketTypeGeneral
		}

		if !isValidMarketType(string(req.MarketType)) {
			utils.Error(w, utils.CodeParamError, "Invalid marketType value")
			return
		}

		if req.Source == "" {
			req.Source = models.SourceUser
		}

		// 创建语录
		quote, err := quoteDB.Create(req)
		if err != nil {
			utils.Error(w, utils.CodeServerError, "Failed to create quote")
			return
		}

		utils.Success(w, quote.ToWithDisplay())
	})
}

// HandleAdminUpdateQuote PUT /api/admin/quotes/:id - 更新语录
func HandleAdminUpdateQuote(db *sql.DB) http.HandlerFunc {
	quoteDB := database.NewQuoteDB(db)
	return middleware.AuthMiddleware(func(w http.ResponseWriter, r *http.Request) {
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

		// 解析请求体
		var req models.UpdateQuoteRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			utils.Error(w, utils.CodeParamError, "Invalid request body")
			return
		}

		// 验证至少有一个字段
		if req.Content == nil && req.Category == nil && req.MarketType == nil {
			utils.Error(w, utils.CodeParamError, "At least one field must be provided")
			return
		}

		// 验证枚举值
		if req.Category != nil && !isValidCategory(string(*req.Category)) {
			utils.Error(w, utils.CodeParamError, "Invalid category value")
			return
		}

		if req.MarketType != nil && !isValidMarketType(string(*req.MarketType)) {
			utils.Error(w, utils.CodeParamError, "Invalid marketType value")
			return
		}

		// 更新语录
		err = quoteDB.Update(id, req)
		if err != nil {
			if err == sql.ErrNoRows {
				utils.Error(w, utils.CodeNotFound, "Quote not found")
				return
			}
			utils.Error(w, utils.CodeServerError, "Failed to update quote")
			return
		}

		// 获取更新后的语录
		quote, err := quoteDB.GetByID(id)
		if err != nil {
			utils.Error(w, utils.CodeServerError, "Failed to fetch updated quote")
			return
		}

		utils.Success(w, quote.ToWithDisplay())
	})
}

// HandleAdminDeleteQuote DELETE /api/admin/quotes/:id - 删除语录
func HandleAdminDeleteQuote(db *sql.DB) http.HandlerFunc {
	quoteDB := database.NewQuoteDB(db)
	return middleware.AuthMiddleware(func(w http.ResponseWriter, r *http.Request) {
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

		// 删除语录
		err = quoteDB.Delete(id)
		if err != nil {
			if err == sql.ErrNoRows {
				utils.Error(w, utils.CodeNotFound, "Quote not found")
				return
			}
			utils.Error(w, utils.CodeServerError, "Failed to delete quote")
			return
		}

		utils.Success(w, map[string]interface{}{
			"id": id,
		})
	})
}

// HandleAdminBatchImport POST /api/admin/quotes/batch - 批量导入语录
func HandleAdminBatchImport(db *sql.DB) http.HandlerFunc {
	quoteDB := database.NewQuoteDB(db)
	return middleware.AuthMiddleware(func(w http.ResponseWriter, r *http.Request) {
		// 解析请求体
		var req models.BatchImportRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			utils.Error(w, utils.CodeParamError, "Invalid request body")
			return
		}

		// 验证 quotes 数组
		if len(req.Quotes) == 0 {
			utils.Error(w, utils.CodeParamError, "Quotes array is empty")
			return
		}

		if len(req.Quotes) > 100 {
			utils.Error(w, utils.CodeParamError, "Maximum 100 quotes per batch")
			return
		}

		// 验证每个 quote
		for i, quote := range req.Quotes {
			if quote.Content == "" {
				utils.Error(w, utils.CodeParamError, "Quote at index "+strconv.Itoa(i)+" is missing content")
				return
			}
			if quote.Category == "" {
				utils.Error(w, utils.CodeParamError, "Quote at index "+strconv.Itoa(i)+" is missing category")
				return
			}
			if !isValidCategory(string(quote.Category)) {
				utils.Error(w, utils.CodeParamError, "Quote at index "+strconv.Itoa(i)+" has invalid category")
				return
			}
			if quote.MarketType == "" {
				req.Quotes[i].MarketType = models.MarketTypeGeneral
			}
			if !isValidMarketType(string(req.Quotes[i].MarketType)) {
				utils.Error(w, utils.CodeParamError, "Quote at index "+strconv.Itoa(i)+" has invalid marketType")
				return
			}
			if quote.Source == "" {
				req.Quotes[i].Source = models.SourceUser
			}
		}

		// 批量创建
		created, err := quoteDB.BatchCreate(req.Quotes)
		if err != nil {
			utils.Error(w, utils.CodeServerError, "Failed to batch create quotes")
			return
		}

		// 转换为带显示名称的格式
		quotesWithDisplay := make([]models.QuoteWithDisplay, len(created))
		for i, quote := range created {
			quotesWithDisplay[i] = quote.ToWithDisplay()
		}

		utils.Success(w, map[string]interface{}{
			"created": len(created),
			"quotes":  quotesWithDisplay,
		})
	})
}
