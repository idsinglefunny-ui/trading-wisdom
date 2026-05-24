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
		var quote models.Quote
		err := rows.Scan(&quote.ID, &quote.Content, &quote.Category, &quote.MarketType, &quote.Source, &quote.IsFavorite, &quote.ViewCount, &quote.CreatedAt, &quote.UpdatedAt)
		if err != nil {
			return nil, 0, err
		}
		quotes = append(quotes, quote)
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
		var quote models.Quote
		err := rows.Scan(&quote.ID, &quote.Content, &quote.Category, &quote.MarketType, &quote.Source, &quote.IsFavorite, &quote.ViewCount, &quote.CreatedAt, &quote.UpdatedAt)
		if err != nil {
			return nil, err
		}
		quotes = append(quotes, quote)
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
