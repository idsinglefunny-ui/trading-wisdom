#!/bin/bash
# TradeYourPlan API Test Script

API_URL="${1:-http://localhost:8080}"
PASSED=0
FAILED=0

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_test() { echo -e "${YELLOW}[TEST]${NC} $1"; }
log_pass() { echo -e "${GREEN}[PASS]${NC} $1"; ((PASSED++)); }
log_fail() { echo -e "${RED}[FAIL]${NC} $1"; ((FAILED++)); }

echo "========================================="
echo "  TradeYourPlan API Test Suite"
echo "  API URL: $API_URL"
echo "========================================="
echo ""

log_test "Checking if API is running..."
if curl -s -o /dev/null -w "%{http_code}" "$API_URL/health" 2>/dev/null | grep -q "200"; then
    log_pass "API is running"
else
    log_fail "API is not running"
    echo "Start with: ./trading-api"
    exit 1
fi

echo ""
echo "--- Public API Tests ---"
echo ""

for endpoint in "/api/quotes/categories" "/api/quotes/market-types" "/api/system/quotes" "/api/quotes?limit=5" "/api/quotes/random"; do
    log_test "GET $endpoint"
    if curl -s "$API_URL$endpoint" | grep -q '"code":0'; then
        log_pass "$endpoint"
    else
        log_fail "$endpoint"
    fi
done

echo ""
echo "--- Admin API Tests ---"
echo ""

log_test "POST /api/admin/login"
RESP=$(curl -s -X POST "$API_URL/api/admin/login" -H "Content-Type: application/json" -d '{"password":"admin123"}')
if echo "$RESP" | grep -q '"code":0'; then
    log_pass "Login successful"
    TOKEN=$(echo "$RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
else
    log_fail "Login failed (may need database init)"
fi

if [ -n "$TOKEN" ]; then
    log_test "GET /api/admin/quotes (auth)"
    curl -s "$API_URL/api/admin/quotes" -H "Authorization: Bearer $TOKEN" | grep -q '"code":0' && log_pass "Auth works" || log_fail "Auth failed"
    
    log_test "GET /api/admin/quotes (no auth)"
    curl -s "$API_URL/api/admin/quotes" | grep -q '"code":1002' && log_pass "Blocked" || log_fail "Not blocked"
fi

echo ""
echo "========================================="
echo "  Results: $PASSED passed, $FAILED failed"
echo "========================================="
