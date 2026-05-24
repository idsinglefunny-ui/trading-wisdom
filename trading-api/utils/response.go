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
    w.WriteHeader(status)

    writeJSON(w, Response{
        Code:    code,
        Message: message,
    })
}

func writeJSON(w http.ResponseWriter, data interface{}) {
    w.Header().Set("Content-Type", "application/json")
    json.NewEncoder(w).Encode(data)
}
