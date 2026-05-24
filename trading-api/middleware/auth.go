// /home/mnyagent/tradeyourplan/trading-api/middleware/auth.go
package middleware

import (
	"crypto/rand"
	"encoding/hex"
	"net/http"
	"strings"
	"sync"
	"time"

	"trading-api/utils"
)

// TokenInfo 存储的 token 信息
type TokenInfo struct {
	Token     string
	CreatedAt int64
	ExpiresAt int64
}

// TokenStore token 存储（生产环境应使用 Redis 或数据库）
// 使用 RWMutex 保护并发访问
type TokenStore struct {
	mu     sync.RWMutex
	tokens map[string]*TokenInfo
}

// NewTokenStore 创建 token 存储
func NewTokenStore() *TokenStore {
	return &TokenStore{
		tokens: make(map[string]*TokenInfo),
	}
}

// GenerateToken 生成 token
func (ts *TokenStore) GenerateToken() string {
	// 使用 crypto/rand 生成 32 字节随机值，更安全
	randomBytes := make([]byte, 32)
	if _, err := rand.Read(randomBytes); err != nil {
		// 如果随机数生成失败，使用时间戳作为后备方案
		// 但这种情况极少发生
		randomBytes = []byte(string(time.Now().UnixNano()))
	}
	token := hex.EncodeToString(randomBytes)

	// 24小时过期
	expiresAt := time.Now().Add(24 * time.Hour).UnixMilli()

	// 使用写锁保护 map 写入
	ts.mu.Lock()
	ts.tokens[token] = &TokenInfo{
		Token:     token,
		CreatedAt: time.Now().UnixMilli(),
		ExpiresAt: expiresAt,
	}
	ts.mu.Unlock()

	return token
}

// ValidateToken 验证 token
func (ts *TokenStore) ValidateToken(token string) bool {
	// 使用读锁保护 map 读取
	ts.mu.RLock()
	info, exists := ts.tokens[token]
	ts.mu.RUnlock()

	if !exists {
		return false
	}

	// 检查是否过期
	if time.Now().UnixMilli() > info.ExpiresAt {
		// 过期则删除，需要写锁
		ts.mu.Lock()
		delete(ts.tokens, token)
		ts.mu.Unlock()
		return false
	}

	return true
}

// RevokeToken 撤销 token
func (ts *TokenStore) RevokeToken(token string) {
	// 使用写锁保护 map 删除
	ts.mu.Lock()
	delete(ts.tokens, token)
	ts.mu.Unlock()
}

// 全局 token 存储
var globalTokenStore = NewTokenStore()

// GetTokenStore 获取全局 token 存储
func GetTokenStore() *TokenStore {
	return globalTokenStore
}

// AuthMiddleware 认证中间件
func AuthMiddleware(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		// 获取 Authorization 头
		authHeader := r.Header.Get("Authorization")
		if authHeader == "" {
			utils.Error(w, utils.CodeUnauthorized, "Missing authorization header")
			return
		}

		// 检查 Bearer 格式
		if !strings.HasPrefix(authHeader, "Bearer ") {
			utils.Error(w, utils.CodeUnauthorized, "Invalid authorization format")
			return
		}

		// 提取 token
		token := strings.TrimPrefix(authHeader, "Bearer ")
		if token == "" {
			utils.Error(w, utils.CodeUnauthorized, "Missing token")
			return
		}

		// 验证 token
		if !globalTokenStore.ValidateToken(token) {
			utils.Error(w, utils.CodeUnauthorized, "Invalid or expired token")
			return
		}

		// token 有效，继续处理请求
		next(w, r)
	}
}

// GenerateLoginToken 生成登录 token
func GenerateLoginToken() string {
	return globalTokenStore.GenerateToken()
}

// RevokeLoginToken 撤销登录 token
func RevokeLoginToken(token string) {
	globalTokenStore.RevokeToken(token)
}
