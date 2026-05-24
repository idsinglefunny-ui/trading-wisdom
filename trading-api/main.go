// /home/mnyagent/tradeyourplan/trading-api/main.go
package main

import (
    "database/sql"
    "log"
    "net/http"
    "os"

    _ "github.com/go-sql-driver/mysql"
    "github.com/gorilla/mux"
    "trading-api/handlers"
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
    r.HandleFunc("/api/quotes", handlers.HandleGetQuotes(db)).Methods("GET")
    r.HandleFunc("/api/quotes/random", handlers.HandleGetRandomQuote(db)).Methods("GET")
    r.HandleFunc("/api/quotes/categories", handlers.HandleGetCategories).Methods("GET")
    r.HandleFunc("/api/quotes/market-types", handlers.HandleGetMarketTypes).Methods("GET")
    r.HandleFunc("/api/quotes/{id}", handlers.HandleGetQuoteByID(db)).Methods("GET")
    r.HandleFunc("/api/system/quotes", handlers.HandleGetSystemQuotes(db)).Methods("GET")

    // 管理 API
    r.HandleFunc("/api/admin/login", handlers.HandleAdminLogin(db)).Methods("POST")
    r.HandleFunc("/api/admin/quotes", handlers.HandleAdminGetQuotes(db)).Methods("GET")
    r.HandleFunc("/api/admin/quotes", handlers.HandleAdminCreateQuote(db)).Methods("POST")
    r.HandleFunc("/api/admin/quotes/{id}", handlers.HandleAdminUpdateQuote(db)).Methods("PUT")
    r.HandleFunc("/api/admin/quotes/{id}", handlers.HandleAdminDeleteQuote(db)).Methods("DELETE")
    r.HandleFunc("/api/admin/quotes/batch", handlers.HandleAdminBatchImport(db)).Methods("POST")

    // 启动服务器
    port := os.Getenv("PORT")
    if port == "" {
        port = "8080"
    }

    log.Printf("Server starting on port %s", port)
    log.Fatal(http.ListenAndServe(":"+port, r))
}
