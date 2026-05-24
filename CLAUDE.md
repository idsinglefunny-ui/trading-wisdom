# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure

This is a **monorepo** containing three applications for "TradeYourPlan" (交易智慧) — a trading wisdom quotes platform:

1. **Android App** (`app/`) — Kotlin/Compose mobile app
2. **Go API** (`trading-api/`) — RESTful backend API
3. **Web Frontend** (`trading-web/`) — Next.js 16 static site

---

## Android App (`app/`)

### Build Environment

- **JDK**: 17 (path: `/home/mnyagent/.local/java/jdk-17.0.12`)
- **Kotlin**: 1.9.20, JVM target: 17
- **Android SDK**: compileSdk 34, targetSdk 34, minSdk 24
- **Gradle**: 8.5

### Build Commands

```bash
# Set Java environment and build release APK
export JAVA_HOME=/home/mnyagent/.local/java/jdk-17.0.12 && export PATH="$JAVA_HOME/bin:$PATH" && ./gradlew assembleRelease --no-daemon

# Run tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.tradeyourplan.data.repository.QuoteRepositoryTest"

# Build debug
./gradlew assembleDebug
```

Release APK output: `app/build/outputs/apk/release/app-release.apk`

### Signing

- Keystore: `app/typ-release.jks`
- Store password/key password: `tradeyourplan`
- Key alias: `typ`

### Architecture

**MVVM + Clean Architecture** with Hilt DI:

- `data/` — Data layer (local DB, API sync, repositories)
  - `local/` — Room database (v3, entities + DAOs)
  - `api/` — Retrofit API interfaces for sync
  - `repository/` — Repository implementations
  - `sync/` — Sync logic for remote quotes
- `domain/` — Business logic (models, use cases)
- `ui/` — Jetpack Compose screens with ViewModels
- `di/` — Hilt modules

**Key Patterns:**
- Room for local storage (Flow-based queries)
- DataStore for settings persistence
- AlarmManager for scheduled quote notifications
- Single-screen app with bottom navigation (no separate routes)

**Important:** R8/minification is DISABLED (`isMinifyEnabled = false`) — Gson TypeToken breaks under R8.

---

## Go API (`trading-api/`)

### Build Environment

- **Go**: 1.25.0
- **Dependencies**: gorilla/mux (router), go-sql-driver/mysql

### Build Commands

```bash
# Run locally
go run main.go

# Build binary
go build -o trading-api main.go

# Run tests
go test ./...

# Test specific package
go test ./handlers

# Initialize database
mysql -u root < trading-api/scripts/init-db.sql
```

### Architecture

**Simple HTTP API** with MySQL backend:

- `main.go` — Entry point, route registration, DB connection
- `handlers/` — HTTP handlers (public.go, admin.go)
- `database/` — Database operations layer
- `models/` — Data structures
- `middleware/` — Auth, logging, etc.

**Database:**
- MySQL `trading_wisdom` database
- Tables: `quotes`, `admin_config`
- Categories: RISK_MGMT, DISCIPLINE, MINDSET, TECHNICAL, FUNDAMENTAL, PSYCHOLOGY, STRATEGY, GENERAL
- Sources: SYSTEM (preset), USER (custom)

**API Routes:**
- Public: `/api/quotes/*` (get quotes, random, by category)
- Admin: `/api/admin/*` (CRUD quotes, batch import, login)

---

## Web Frontend (`trading-web/`)

### Build Environment

- **Next.js**: 16.2.6 (App Router — **NOT** the Next.js you know, breaking changes apply)
- **React**: 19.2.4
- **Tailwind CSS**: v4
- **TypeScript**: 5

### Build Commands

```bash
# Development server
cd trading-web && npm run dev

# Production build (static export)
cd trading-web && npm run build

# Start production server (serves built static files)
cd trading-web && npm run start

# Lint
cd trading-web && npm run lint
```

### Architecture

**Static Site** with minimal routing:
- `app/page.tsx` — Landing page with download link
- `app/about/` — About page
- `app/quotes/` — Browse quotes page
- `app/admin/` — Admin interface

**Important:** Next.js 16 has breaking changes from training data. Read `node_modules/next/dist/docs/` before modifying.

---

## Deployment

Deployment scripts are in `deploy/`:

```bash
# Full deployment (requires sudo)
sudo ./deploy/deploy.sh

# Setup SSL
sudo ./deploy/setup-ssl.sh
```

**Deployment targets:**
- Go API → `/opt/trading-api/` (systemd service)
- Web build → `/opt/trading-web/` (nginx static)
- Nginx config → `/etc/nginx/sites-available/trading.tangping.me`

**Services:**
- `trading-api.service` — Go API systemd service
- `trading-web.service` — Optional web service

---

## Database Schema (Common)

The Android app and Go API share the same conceptual schema:

**Quotes Table:**
- `id` — Primary key
- `content` — Quote text
- `author` — Optional author
- `category` — ENUM (8 categories)
- `source` — SYSTEM or USER
- `is_active` / `isFavorite` — Active/favorite flags
- `createdAt`, `updatedAt` — Timestamps

**Sync Direction:** Android app syncs FROM Go API (one-way).
