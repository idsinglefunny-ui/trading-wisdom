// /home/mnyagent/tradeyourplan/trading-api/main.go
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
