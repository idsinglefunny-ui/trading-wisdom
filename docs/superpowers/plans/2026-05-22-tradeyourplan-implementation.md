# TradeYourPlan Android App - Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个面向投资者的 Android 提醒应用，提供交易智慧和自定义提醒功能

**Architecture:** MVVM + Repository 模式，使用 Jetpack Compose UI，Room 本地存储，Hilt 依赖注入

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, Coroutines, Flow, WorkManager, AlarmManager

---

## File Structure Overview

```
app/src/main/java/com/tradeyourplan/
├── TradeYourPlanApplication.kt           # Application 类，Hilt 初始化
├── di/
│   ├── AppModule.kt                      # Hilt 模块
│   ├── DatabaseModule.kt                 # 数据库模块
│   └── RepositoryModule.kt               # Repository 模块
├── data/
│   ├── local/
│   │   ├── TradeYourPlanDatabase.kt      # Room 数据库
│   │   ├── dao/
│   │   │   ├── QuoteDao.kt              # 语录 DAO
│   │   │   ├── AlarmDao.kt              # 闹钟 DAO
│   │   │   └── SettingsDao.kt           # 设置 DAO
│   │   └── entity/
│   │       ├── QuoteEntity.kt           # 语录实体
│   │       ├── AlarmEntity.kt           # 闹钟实体
│   │       └── SettingEntity.kt         # 设置实体
│   ├── repository/
│   │   ├── QuoteRepository.kt           # 语录 Repository
│   │   ├── AlarmRepository.kt           # 闹钟 Repository
│   │   └── SettingsRepository.kt        # 设置 Repository
│   └── model/
│       ├── Quote.kt                     # 语录领域模型
│       ├── Alarm.kt                     # 闹钟领域模型
│       └── Theme.kt                     # 主题模型
├── domain/
│   ├── usecase/
│   │   ├── GetRandomQuoteUseCase.kt     # 获取随机语录
│   │   ├── GetQuotesUseCase.kt          # 获取语录列表
│   │   ├── AddQuoteUseCase.kt           # 添加语录
│   │   ├── UpdateQuoteUseCase.kt        # 更新语录
│   │   ├── DeleteQuoteUseCase.kt        # 删除语录
│   │   ├── ToggleFavoriteUseCase.kt     # 切换收藏
│   │   ├── GetAlarmsUseCase.kt          # 获取闹钟列表
│   │   ├── AddAlarmUseCase.kt           # 添加闹钟
│   │   ├── UpdateAlarmUseCase.kt        # 更新闹钟
│   │   ├── DeleteAlarmUseCase.kt        # 删除闹钟
│   │   └── GetThemeUseCase.kt           # 获取当前主题
│   └── model/
│       ├── Category.kt                  # 分类枚举
│       ├── MarketType.kt               # 市场类型枚举
│       ├── AlarmType.kt                # 闹钟类型枚举
│       ├── NotificationLevel.kt        # 通知级别枚举
│       └── RepeatMode.kt               # 重复模式枚举
├── ui/
│   ├── theme/
│   │   ├── Color.kt                     # 颜色定义
│   │   ├── Type.kt                     # 字体定义
│   │   └── Theme.kt                    # 主题配置
│   ├── main/
│   │   ├── MainActivity.kt             # 主 Activity
│   │   └── MainScreen.kt               # 主屏幕 Composable
│   ├── quote/
│   │   ├── QuoteListScreen.kt          # 语录列表
│   │   ├── QuoteDetailScreen.kt        # 语录详情
│   │   ├── AddQuoteScreen.kt           # 添加语录
│   │   └── QuoteViewModel.kt           # 语录 ViewModel
│   ├── alarm/
│   │   ├── AlarmListScreen.kt          # 闹钟列表
│   │   ├── AlarmEditScreen.kt          # 编辑闹钟
│   │   └── AlarmViewModel.kt           # 闹钟 ViewModel
│   ├── settings/
│   │   ├── SettingsScreen.kt           # 设置界面
│   │   ├── ThemePickerScreen.kt        # 主题选择
│   │   └── SettingsViewModel.kt        # 设置 ViewModel
│   ├── components/
│   │   ├── QuoteCard.kt                # 语录卡片
│   │   ├── AlarmCard.kt                # 闹钟卡片
│   │   ├── EmptyState.kt               # 空状态组件
│   │   ├── TYPButton.kt                # 按钮组件
│   │   └── TYPTextField.kt             # 输入框组件
│   └── navigation/
│       └── NavGraph.kt                 # 导航图
├── notification/
│   ├── AlarmScheduler.kt               # 闹钟调度器
│   ├── AlarmReceiver.kt                # 闹钟广播接收器
│   └── NotificationHelper.kt           # 通知辅助类
├── util/
│   ├── DateUtils.kt                    # 日期工具
│   └── ShareHelper.kt                  # 分享辅助
└── model/
    └── Quote.kt                        # 共享数据模型

app/src/main/assets/
└── quotes.json                         # 预置语录数据

app/src/main/res/
├── drawable/                           # 图标资源
├── values/
│   ├── strings.xml                     # 字符串资源
│   └── colors.xml                      # 颜色资源（备用）
└── values-night/
    └── colors.xml                      # 深色模式颜色

app/src/androidTest/java/com/tradeyourplan/
└── database/
    └── QuoteDaoTest.kt                 # 数据库测试

app/src/test/java/com/tradeyourplan/
├── data/
│   └── repository/
│       └── QuoteRepositoryTest.kt      # Repository 测试
└── domain/
    └── usecase/
        └── GetRandomQuoteUseCaseTest.kt # UseCase 测试
```

---

## Task 1: 项目初始化

**Files:**
- Create: `app/build.gradle.kts`
- Create: `build.gradle.kts`
- Create: `settings.gradle.kts`
- Create: `gradle.properties`
- Create: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: 创建根目录 build.gradle.kts**

```kotlin
// build.gradle.kts
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}
```

- [ ] **Step 2: 创建 settings.gradle.kts**

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TradeYourPlan"
include(":app")
```

- [ ] **Step 3: 创建 gradle.properties**

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
```

- [ ] **Step 4: 创建 app/build.gradle.kts**

```kotlin
// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.tradeyourplan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tradeyourplan"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

- [ ] **Step 5: 创建 AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- 闹钟权限 -->
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <!-- 启动权限 -->
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application
        android:name=".TradeYourPlanApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.TradeYourPlan">

        <activity
            android:name=".ui.main.MainActivity"
            android:exported="true"
            android:theme="@style/Theme.TradeYourPlan">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- 闹钟广播接收器 -->
        <receiver
            android:name=".notification.AlarmReceiver"
            android:enabled="true"
            android:exported="false">
            <intent-filter>
                <action android:name="com.tradeyourplan.ALARM_TRIGGERED" />
            </intent-filter>
        </receiver>

    </application>

</manifest>
```

- [ ] **Step 6: 创建基础资源文件 strings.xml**

```xml
<!-- app/src/main/res/values/strings.xml -->
<resources>
    <string name="app_name">交易你的计划</string>
    <string name="quote">语录</string>
    <string name="alarm">闹钟</string>
    <string name="settings">设置</string>
    <string name="today_quote">今日语录</string>
    <string name="refresh">换一换</string>
    <string name="favorite">收藏</string>
    <string name="share">分享</string>
    <string name="add_quote">添加语录</string>
    <string name="edit_quote">编辑语录</string>
    <string name="delete_quote">删除语录</string>
    <string name="add_alarm">添加提醒</string>
    <string name="edit_alarm">编辑提醒</string>
    <string name="delete_alarm">删除提醒</string>
    <string name="empty_quotes">暂无语录</string>
    <string name="empty_quotes_hint">点击下方按钮添加你的第一条交易智慧</string>
    <string name="empty_alarms">暂无提醒</string>
    <string name="empty_alarms_hint">设置一个提醒，让交易智慧常伴左右</string>
    <string name="quote_content">语录内容</string>
    <string name="quote_category">分类</string>
    <string name="quote_market_type">市场类型</string>
    <string name="save">保存</string>
    <string name="cancel">取消</string>
    <string name="delete">删除</string>
    <string name="confirm">确认</string>
    <string name="confirm_delete">确认删除？</string>
    <string name="theme">主题</string>
    <string name="theme_professional_dark">专业深色</string>
    <string name="theme_warm_encouraging">温馨鼓励</string>
    <string name="theme_minimal_light">极简浅色</string>
    <string name="notification_level">通知强度</string>
    <string name="notification_level_silent">静默</string>
    <string name="notification_level_normal">标准</string>
    <string name="notification_level_full_screen">强提醒</string>
    <string name="quote_source">语录来源</string>
    <string name="source_system">系统</string>
    <string name="source_user">用户</string>
    <string name="source_mixed">混合</string>
</resources>
```

- [ ] **Step 7: 提交**

```bash
git add build.gradle.kts settings.gradle.kts gradle.properties app/build.gradle.kts app/src/main/AndroidManifest.xml app/src/main/res/values/strings.xml
git commit -m "feat: initialize project structure and dependencies"
```

---

## Task 2: 领域模型定义

**Files:**
- Create: `app/src/main/java/com/tradeyourplan/domain/model/Category.kt`
- Create: `app/src/main/java/com/tradeyourplan/domain/model/MarketType.kt`
- Create: `app/src/main/java/com/tradeyourplan/domain/model/QuoteSource.kt`
- Create: `app/src/main/java/com/tradeyourplan/domain/model/AlarmType.kt`
- Create: `app/src/main/java/com/tradeyourplan/domain/model/NotificationLevel.kt`
- Create: `app/src/main/java/com/tradeyourplan/domain/model/RepeatMode.kt`
- Create: `app/src/main/java/com/tradeyourplan/data/model/Quote.kt`
- Create: `app/src/main/java/com/tradeyourplan/data/model/Alarm.kt`

- [ ] **Step 1: 创建 Category 枚举**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/model/Category.kt
package com.tradeyourplan.domain.model

enum class Category(val displayName: String) {
    RISK_MGMT("风险管理"),
    MINDSET("交易心态"),
    DISCIPLINE("交易纪律"),
    TECHNICAL("技术分析");

    companion object {
        fun fromString(value: String): Category? {
            return values().find { it.name == value }
        }
    }
}
```

- [ ] **Step 2: 创建 MarketType 枚举**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/model/MarketType.kt
package com.tradeyourplan.domain.model

enum class MarketType(val displayName: String) {
    STOCK("股票"),
    FUTURES("期货"),
    GENERAL("通用");

    companion object {
        fun fromString(value: String): MarketType? {
            return values().find { it.name == value }
        }
    }
}
```

- [ ] **Step 3: 创建 QuoteSource 枚举**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/model/QuoteSource.kt
package com.tradeyourplan.domain.model

enum class QuoteSource(val displayName: String) {
    SYSTEM("系统"),
    USER("用户");

    companion object {
        fun fromString(value: String): QuoteSource? {
            return values().find { it.name == value }
        }
    }
}
```

- [ ] **Step 4: 创建 AlarmType 枚举**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/model/AlarmType.kt
package com.tradeyourplan.domain.model

enum class AlarmType(val displayName: String) {
    FIXED("固定时间"),
    RANDOM("随机时间"),
    EVENT_TRIGGERED("事件触发");

    companion object {
        fun fromString(value: String): AlarmType? {
            return values().find { it.name == value }
        }
    }
}
```

- [ ] **Step 5: 创建 NotificationLevel 枚举**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/model/NotificationLevel.kt
package com.tradeyourplan.domain.model

enum class NotificationLevel(val displayName: String) {
    SILENT("静默"),
    NORMAL("标准"),
    FULL_SCREEN("强提醒");

    companion object {
        fun fromString(value: String): NotificationLevel? {
            return values().find { it.name == value }
        }
    }
}
```

- [ ] **Step 6: 创建 RepeatMode 枚举**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/model/RepeatMode.kt
package com.tradeyourplan.domain.model

enum class RepeatMode(val displayName: String) {
    ONCE("一次"),
    DAILY("每天"),
    WEEKDAYS("工作日"),
    CUSTOM("自定义");

    companion object {
        fun fromString(value: String): RepeatMode? {
            return values().find { it.name == value }
        }
    }
}
```

- [ ] **Step 7: 创建 Quote 数据模型**

```kotlin
// app/src/main/java/com/tradeyourplan/data/model/Quote.kt
package com.tradeyourplan.data.model

import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource

data class Quote(
    val id: Long = 0,
    val content: String,
    val category: Category,
    val marketType: MarketType,
    val source: QuoteSource,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 8: 创建 Alarm 数据模型**

```kotlin
// app/src/main/java/com/tradeyourplan/data/model/Alarm.kt
package com.tradeyourplan.data.model

import com.tradeyourplan.domain.model.AlarmType
import com.tradeyourplan.domain.model.NotificationLevel
import com.tradeyourplan.domain.model.RepeatMode

data class Alarm(
    val id: Long = 0,
    val type: AlarmType,
    val hour: Int? = null,           // FIXED 类型用
    val minute: Int? = null,         // FIXED 类型用
    val startHour: Int? = null,      // RANDOM 类型用
    val endHour: Int? = null,        // RANDOM 类型用
    val targetPackage: String? = null, // EVENT_TRIGGERED 类型用
    val delaySeconds: Int? = null,   // EVENT_TRIGGERED 类型用
    val repeatMode: RepeatMode = RepeatMode.DAILY,
    val isEnabled: Boolean = true,
    val notificationLevel: NotificationLevel = NotificationLevel.NORMAL
) {
    val timeDisplay: String
        get() = when (type) {
            AlarmType.FIXED -> String.format("%02d:%02d", hour ?: 0, minute ?: 0)
            AlarmType.RANDOM -> String.format("%02d:00-%02d:00", startHour ?: 9, endHour ?: 15)
            AlarmType.EVENT_TRIGGERED -> "事件触发"
        }
}
```

- [ ] **Step 9: 提交**

```bash
git add app/src/main/java/com/tradeyourplan/domain/model/ app/src/main/java/com/tradeyourplan/data/model/
git commit -m "feat: define domain models and enums"
```

---

## Task 3: 数据库实现

**Files:**
- Create: `app/src/main/java/com/tradeyourplan/data/local/entity/QuoteEntity.kt`
- Create: `app/src/main/java/com/tradeyourplan/data/local/entity/AlarmEntity.kt`
- Create: `app/src/main/java/com/tradeyourplan/data/local/entity/SettingEntity.kt`
- Create: `app/src/main/java/com/tradeyourplan/data/local/dao/QuoteDao.kt`
- Create: `app/src/main/java/com/tradeyourplan/data/local/dao/AlarmDao.kt`
- Create: `app/src/main/java/com/tradeyourplan/data/local/dao/SettingsDao.kt`
- Create: `app/src/main/java/com/tradeyourplan/data/local/TradeYourPlanDatabase.kt`

- [ ] **Step 1: 创建 QuoteEntity**

```kotlin
// app/src/main/java/com/tradeyourplan/data/local/entity/QuoteEntity.kt
package com.tradeyourplan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val category: String,        // Category.name
    val marketType: String,      // MarketType.name
    val source: String,          // QuoteSource.name
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: 创建 AlarmEntity**

```kotlin
// app/src/main/java/com/tradeyourplan/data/local/entity/AlarmEntity.kt
package com.tradeyourplan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tradeyourplan.domain.model.AlarmType
import com.tradeyourplan.domain.model.NotificationLevel
import com.tradeyourplan.domain.model.RepeatMode

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,            // AlarmType.name
    val hour: Int? = null,
    val minute: Int? = null,
    val startHour: Int? = null,
    val endHour: Int? = null,
    val targetPackage: String? = null,
    val delaySeconds: Int? = null,
    val repeatMode: String,      // RepeatMode.name
    val isEnabled: Boolean = true,
    val notificationLevel: String // NotificationLevel.name
)
```

- [ ] **Step 3: 创建 SettingEntity**

```kotlin
// app/src/main/java/com/tradeyourplan/data/local/entity/SettingEntity.kt
package com.tradeyourplan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
```

- [ ] **Step 4: 创建 QuoteDao**

```kotlin
// app/src/main/java/com/tradeyourplan/data/local/dao/QuoteDao.kt
package com.tradeyourplan.data.local.dao

import androidx.room.*
import com.tradeyourplan.data.local.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes ORDER BY createdAt DESC")
    fun getAllQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getQuoteById(id: Long): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE source = 'SYSTEM'")
    suspend fun getSystemQuotes(): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE source = 'USER'")
    suspend fun getUserQuotes(): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE category = :category")
    fun getQuotesByCategory(category: String): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE marketType = :marketType")
    fun getQuotesByMarketType(marketType: String): Flow<List<QuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<QuoteEntity>)

    @Update
    suspend fun updateQuote(quote: QuoteEntity)

    @Delete
    suspend fun deleteQuote(quote: QuoteEntity)

    @Query("DELETE FROM quotes WHERE id = :id")
    suspend fun deleteQuoteById(id: Long)

    @Query("UPDATE quotes SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)
}
```

- [ ] **Step 5: 创建 AlarmDao**

```kotlin
// app/src/main/java/com/tradeyourplan/data/local/dao/AlarmDao.kt
package com.tradeyourplan.data.local.dao

import androidx.room.*
import com.tradeyourplan.data.local.entity.AlarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY id ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarms(): List<AlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: Long)

    @Query("UPDATE alarms SET isEnabled = :enabled WHERE id = :id")
    suspend fun setAlarmEnabled(id: Long, enabled: Boolean)
}
```

- [ ] **Step 6: 创建 SettingsDao**

```kotlin
// app/src/main/java/com/tradeyourplan/data/local/dao/SettingsDao.kt
package com.tradeyourplan.data.local.dao

import androidx.room.*
import com.tradeyourplan.data.local.entity.SettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE key = :key")
    suspend fun getSetting(key: String): SettingEntity?

    @Query("SELECT value FROM settings WHERE key = :key")
    suspend fun getSettingValue(key: String): String?

    @Query("SELECT value FROM settings WHERE key = :key")
    fun getSettingValueFlow(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: SettingEntity)

    @Query("INSERT OR REPLACE INTO settings (key, value) VALUES (:key, :value)")
    suspend fun setSettingValue(key: String, value: String)
}
```

- [ ] **Step 7: 创建数据库类**

```kotlin
// app/src/main/java/com/tradeyourplan/data/local/TradeYourPlanDatabase.kt
package com.tradeyourplan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tradeyourplan.data.local.dao.AlarmDao
import com.tradeyourplan.data.local.dao.QuoteDao
import com.tradeyourplan.data.local.dao.SettingsDao
import com.tradeyourplan.data.local.entity.AlarmEntity
import com.tradeyourplan.data.local.entity.QuoteEntity
import com.tradeyourplan.data.local.entity.SettingEntity

@Database(
    entities = [QuoteEntity::class, AlarmEntity::class, SettingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TradeYourPlanDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    abstract fun alarmDao(): AlarmDao
    abstract fun settingsDao(): SettingsDao
}
```

- [ ] **Step 8: 提交**

```bash
git add app/src/main/java/com/tradeyourplan/data/local/
git commit -m "feat: implement Room database entities and DAOs"
```

---

## Task 4: 依赖注入模块

**Files:**
- Create: `app/src/main/java/com/tradeyourplan/di/DatabaseModule.kt`
- Create: `app/src/main/java/com/tradeyourplan/di/RepositoryModule.kt`
- Create: `app/src/main/java/com/tradeyourplan/di/AppModule.kt`
- Create: `app/src/main/java/com/tradeyourplan/TradeYourPlanApplication.kt`

- [ ] **Step 1: 创建 DatabaseModule**

```kotlin
// app/src/main/java/com/tradeyourplan/di/DatabaseModule.kt
package com.tradeyourplan.di

import android.content.Context
import androidx.room.Room
import com.tradeyourplan.data.local.TradeYourPlanDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TradeYourPlanDatabase {
        return Room.databaseBuilder(
            context,
            TradeYourPlanDatabase::class.java,
            "tradeyourplan.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideQuoteDao(database: TradeYourPlanDatabase) = database.quoteDao()

    @Provides
    @Singleton
    fun provideAlarmDao(database: TradeYourPlanDatabase) = database.alarmDao()

    @Provides
    @Singleton
    fun provideSettingsDao(database: TradeYourPlanDatabase) = database.settingsDao()
}
```

- [ ] **Step 2: 创建 RepositoryModule（先留空，Task 5 实现）**

```kotlin
// app/src/main/java/com/tradeyourplan/di/RepositoryModule.kt
package com.tradeyourplan.di

import com.tradeyourplan.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideQuoteRepository(/* TODO */): QuoteRepository {
        // TODO: Implement in Task 5
        throw NotImplementedError()
    }
}
```

- [ ] **Step 3: 创建 AppModule**

```kotlin
// app/src/main/java/com/tradeyourplan/di/AppModule.kt
package com.tradeyourplan.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.dataStore
}
```

- [ ] **Step 4: 创建 Application 类**

```kotlin
// app/src/main/java/com/tradeyourplan/TradeYourPlanApplication.kt
package com.tradeyourplan

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TradeYourPlanApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 初始化预置数据将在 Task 6 中实现
    }
}
```

- [ ] **Step 5: 提交**

```bash
git add app/src/main/java/com/tradeyourplan/di/ app/src/main/java/com/tradeyourplan/TradeYourPlanApplication.kt
git commit -m "feat: setup Hilt dependency injection modules"
```

---

## Task 5: Repository 实现

**Files:**
- Create: `app/src/main/java/com/tradeyourplan/data/repository/QuoteRepository.kt`
- Create: `app/src/main/java/com/tradeyourplan/data/repository/AlarmRepository.kt`
- Create: `app/src/main/java/com/tradeyourplan/data/repository/SettingsRepository.kt`
- Modify: `app/src/main/java/com/tradeyourplan/di/RepositoryModule.kt`

- [ ] **Step 1: 创建 QuoteRepository**

```kotlin
// app/src/main/java/com/tradeyourplan/data/repository/QuoteRepository.kt
package com.tradeyourplan.data.repository

import com.tradeyourplan.data.local.dao.QuoteDao
import com.tradeyourplan.data.local.entity.QuoteEntity
import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepository @Inject constructor(
    private val quoteDao: QuoteDao
) {
    fun getAllQuotes(): Flow<List<Quote>> {
        return quoteDao.getAllQuotes().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getQuoteById(id: Long): Quote? {
        return quoteDao.getQuoteById(id)?.toDomainModel()
    }

    fun getFavoriteQuotes(): Flow<List<Quote>> {
        return quoteDao.getFavoriteQuotes().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getRandomQuote(
        sourceFilter: QuoteSource? = null,
        category: Category? = null,
        marketType: MarketType? = null
    ): Quote? {
        val quotes = when (sourceFilter) {
            QuoteSource.SYSTEM -> quoteDao.getSystemQuotes()
            QuoteSource.USER -> quoteDao.getUserQuotes()
            null -> {
                // 混合模式，获取所有
                buildList {
                    addAll(quoteDao.getSystemQuotes())
                    addAll(quoteDao.getUserQuotes())
                }
            }
        }

        val filtered = quotes.filter { entity ->
            val categoryMatch = category == null || entity.category == category.name
            val marketTypeMatch = marketType == null || entity.marketType == marketType.name
            categoryMatch && marketTypeMatch
        }

        return if (filtered.isNotEmpty()) {
            filtered.random().toDomainModel()
        } else null
    }

    fun getQuotesByCategory(category: Category): Flow<List<Quote>> {
        return quoteDao.getQuotesByCategory(category.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun addQuote(quote: Quote): Long {
        return quoteDao.insertQuote(quote.toEntity())
    }

    suspend fun updateQuote(quote: Quote) {
        quoteDao.updateQuote(quote.toEntity())
    }

    suspend fun deleteQuote(quote: Quote) {
        quoteDao.deleteQuote(quote.toEntity())
    }

    suspend fun deleteQuoteById(id: Long) {
        quoteDao.deleteQuoteById(id)
    }

    suspend fun toggleFavorite(id: Long) {
        quoteDao.toggleFavorite(id)
    }

    suspend fun insertInitialQuotes(quotes: List<Quote>) {
        quoteDao.insertQuotes(quotes.map { it.toEntity() })
    }

    private fun QuoteEntity.toDomainModel() = Quote(
        id = id,
        content = content,
        category = Category.fromString(category) ?: Category.GENERAL,
        marketType = MarketType.fromString(marketType) ?: MarketType.GENERAL,
        source = QuoteSource.fromString(source) ?: QuoteSource.SYSTEM,
        isFavorite = isFavorite,
        createdAt = createdAt
    )

    private fun Quote.toEntity() = QuoteEntity(
        id = id,
        content = content,
        category = category.name,
        marketType = marketType.name,
        source = source.name,
        isFavorite = isFavorite,
        createdAt = createdAt
    )
}
```

- [ ] **Step 2: 创建 AlarmRepository**

```kotlin
// app/src/main/java/com/tradeyourplan/data/repository/AlarmRepository.kt
package com.tradeyourplan.data.repository

import com.tradeyourplan.data.local.dao.AlarmDao
import com.tradeyourplan.data.local.entity.AlarmEntity
import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.domain.model.AlarmType
import com.tradeyourplan.domain.model.NotificationLevel
import com.tradeyourplan.domain.model.RepeatMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao
) {
    fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getAlarmById(id: Long): Alarm? {
        return alarmDao.getAlarmById(id)?.toDomainModel()
    }

    suspend fun getEnabledAlarms(): List<Alarm> {
        return alarmDao.getEnabledAlarms().map { it.toDomainModel() }
    }

    suspend fun addAlarm(alarm: Alarm): Long {
        return alarmDao.insertAlarm(alarm.toEntity())
    }

    suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(alarm.toEntity())
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm.toEntity())
    }

    suspend fun deleteAlarmById(id: Long) {
        alarmDao.deleteAlarmById(id)
    }

    suspend fun setAlarmEnabled(id: Long, enabled: Boolean) {
        alarmDao.setAlarmEnabled(id, enabled)
    }

    private fun AlarmEntity.toDomainModel() = Alarm(
        id = id,
        type = AlarmType.fromString(type) ?: AlarmType.FIXED,
        hour = hour,
        minute = minute,
        startHour = startHour,
        endHour = endHour,
        targetPackage = targetPackage,
        delaySeconds = delaySeconds,
        repeatMode = RepeatMode.fromString(repeatMode) ?: RepeatMode.DAILY,
        isEnabled = isEnabled,
        notificationLevel = NotificationLevel.fromString(notificationLevel) ?: NotificationLevel.NORMAL
    )

    private fun Alarm.toEntity() = AlarmEntity(
        id = id,
        type = type.name,
        hour = hour,
        minute = minute,
        startHour = startHour,
        endHour = endHour,
        targetPackage = targetPackage,
        delaySeconds = delaySeconds,
        repeatMode = repeatMode.name,
        isEnabled = isEnabled,
        notificationLevel = notificationLevel.name
    )
}
```

- [ ] **Step 3: 创建 SettingsRepository**

```kotlin
// app/src/main/java/com/tradeyourplan/data/repository/SettingsRepository.kt
package com.tradeyourplan.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val QUOTE_SOURCE = stringPreferencesKey("quote_source")
        private val NOTIFICATION_LEVEL = stringPreferencesKey("notification_level")
    }

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "PROFESSIONAL_DARK"
    }

    val quoteSource: Flow<String> = dataStore.data.map { preferences ->
        preferences[QUOTE_SOURCE] ?: "MIXED"
    }

    val notificationLevel: Flow<String> = dataStore.data.map { preferences ->
        preferences[NOTIFICATION_LEVEL] ?: "NORMAL"
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setQuoteSource(source: String) {
        dataStore.edit { preferences ->
            preferences[QUOTE_SOURCE] = source
        }
    }

    suspend fun setNotificationLevel(level: String) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_LEVEL] = level
        }
    }
}
```

- [ ] **Step 4: 更新 RepositoryModule**

```kotlin
// app/src/main/java/com/tradeyourplan/di/RepositoryModule.kt
package com.tradeyourplan.di

import com.tradeyourplan.data.repository.AlarmRepository
import com.tradeyourplan.data.repository.QuoteRepository
import com.tradeyourplan.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideQuoteRepository(repository: QuoteRepository): QuoteRepository = repository

    @Provides
    @Singleton
    fun provideAlarmRepository(repository: AlarmRepository): AlarmRepository = repository

    @Provides
    @Singleton
    fun provideSettingsRepository(repository: SettingsRepository): SettingsRepository = repository
}
```

- [ ] **Step 5: 提交**

```bash
git add app/src/main/java/com/tradeyourplan/data/repository/ app/src/main/java/com/tradeyourplan/di/RepositoryModule.kt
git commit -m "feat: implement repository layer"
```

---

## Task 6: 预置数据和初始化

**Files:**
- Create: `app/src/main/assets/quotes.json`
- Create: `app/src/main/java/com/tradeyourplan/data/initializer/QuotesInitializer.kt`

- [ ] **Step 1: 创建预置语录 JSON 文件**

```json
[
  {
    "content": "计划你的交易，交易你的计划",
    "category": "DISCIPLINE",
    "marketType": "GENERAL",
    "source": "SYSTEM"
  },
  {
    "content": "截断亏损，让利润奔跑",
    "category": "RISK_MGMT",
    "marketType": "GENERAL",
    "source": "SYSTEM"
  },
  {
    "content": "不要把所有鸡蛋放在一个篮子里",
    "category": "RISK_MGMT",
    "marketType": "GENERAL",
    "source": "SYSTEM"
  },
  {
    "content": "市场永远是对的，意见经常是错的",
    "category": "MINDSET",
    "marketType": "GENERAL",
    "source": "SYSTEM"
  },
  {
    "content": "趋势是你的朋友",
    "category": "TECHNICAL",
    "marketType": "GENERAL",
    "source": "SYSTEM"
  },
  {
    "content": "恐惧和贪婪是最大的敌人",
    "category": "MINDSET",
    "marketType": "GENERAL",
    "source": "SYSTEM"
  },
  {
    "content": "不要在亏损的头寸上加仓",
    "category": "RISK_MGMT",
    "marketType": "GENERAL",
    "source": "SYSTEM"
  },
  {
    "content": "保住本金是第一原则",
    "category": "RISK_MGMT",
    "marketType": "GENERAL",
    "source": "SYSTEM"
  },
  {
    "content": "等待确认信号，不要猜测",
    "category": "TECHNICAL",
    "marketType": "STOCK",
    "source": "SYSTEM"
  },
  {
    "content": "期货交易是概率游戏，不是赌博",
    "category": "MINDSET",
    "marketType": "FUTURES",
    "source": "SYSTEM"
  }
]
```

- [ ] **Step 2: 创建语录初始化器**

```kotlin
// app/src/main/java/com/tradeyourplan/data/initializer/QuotesInitializer.kt
package com.tradeyourplan.data.initializer

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tradeyourplan.data.local.dao.QuoteDao
import com.tradeyourplan.data.local.entity.QuoteEntity
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuotesInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val quoteDao: QuoteDao
) {
    private val gson = Gson()

    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        val existingQuotes = quoteDao.getAllQuotes()
        // 如果已有语录，不再初始化
        // 这里需要第一次获取，实际可以用单次执行标志
        val count = try {
            // 获取当前值
            val list = mutableListOf<QuoteEntity>()
            quoteDao.getAllQuotes().collect { list.addAll(it) }
            list.size
        } catch (e: Exception) {
            0
        }

        if (count == 0) {
            loadQuotesFromAssets()
        }
    }

    private fun loadQuotesFromAssets() {
        try {
            val json = context.assets.open("quotes.json").bufferedReader().use { it.readText() }
            val quoteType = object : TypeToken<List<QuoteDto>>() {}.type
            val quoteDtos: List<QuoteDto> = gson.fromJson(json, quoteType)

            quoteDtos.forEach { dto ->
                val entity = QuoteEntity(
                    content = dto.content,
                    category = dto.category,
                    marketType = dto.marketType,
                    source = dto.source
                )
                quoteDao.insertQuote(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private data class QuoteDto(
        val content: String,
        val category: String,
        val marketType: String,
        val source: String
    )
}
```

- [ ] **Step 3: 更新 Application 类**

```kotlin
// app/src/main/java/com/tradeyourplan/TradeYourPlanApplication.kt
package com.tradeyourplan

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TradeYourPlanApplication : Application() {

    @Inject
    lateinit var quotesInitializer: com.tradeyourplan.data.initializer.QuotesInitializer

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            quotesInitializer.initializeIfNeeded()
        }
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add app/src/main/assets/quotes.json app/src/main/java/com/tradeyourplan/data/initializer/ app/src/main/java/com/tradeyourplan/TradeYourPlanApplication.kt
git commit -m "feat: add initial quotes data and initializer"
```

---

## Task 7: Use Case 层实现

**Files:**
- Create: `app/src/main/java/com/tradeyourplan/domain/usecase/GetRandomQuoteUseCase.kt`
- Create: `app/src/main/java/com/tradeyourplan/domain/usecase/GetQuotesUseCase.kt`
- Create: `app/src/main/java/com/tradeyourplan/domain/usecase/AddQuoteUseCase.kt`
- Create: `app/src/main/java/com/tradeyourplan/domain/usecase/UpdateQuoteUseCase.kt`
- Create: `app/src/main/java/com/tradeyourplan/domain/usecase/DeleteQuoteUseCase.kt`
- Create: `app/src/main/java/com/tradeyourplan/domain/usecase/ToggleFavoriteUseCase.kt`
- Create: `app/src/main/java/com/tradeyourplan/domain/usecase/GetAlarmsUseCase.kt`
- Create: `app/src/main/java/com/tradeyourplan/domain/usecase/AddAlarmUseCase.kt`
- Create: `app/src/main/java/com/tradeyourplan/domain/usecase/UpdateAlarmUseCase.kt`
- Create: `app/src/main/java/com/tradeyourplan/domain/usecase/DeleteAlarmUseCase.kt`
- Create: `app/src/main/java/com/tradeyourplan/domain/usecase/ToggleAlarmUseCase.kt`

- [ ] **Step 1: 创建 GetRandomQuoteUseCase**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/usecase/GetRandomQuoteUseCase.kt
package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.data.repository.QuoteRepository
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource
import javax.inject.Inject

class GetRandomQuoteUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    suspend operator fun invoke(
        sourceFilter: QuoteSource? = null,
        category: Category? = null,
        marketType: MarketType? = null
    ): Quote? {
        return repository.getRandomQuote(sourceFilter, category, marketType)
    }
}
```

- [ ] **Step 2: 创建 GetQuotesUseCase**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/usecase/GetQuotesUseCase.kt
package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.data.repository.QuoteRepository
import com.tradeyourplan.domain.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQuotesUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    operator fun invoke(category: Category? = null): Flow<List<Quote>> {
        return if (category == null) {
            repository.getAllQuotes()
        } else {
            repository.getQuotesByCategory(category)
        }
    }

    fun getFavoriteQuotes(): Flow<List<Quote>> {
        return repository.getFavoriteQuotes()
    }
}
```

- [ ] **Step 3: 创建 AddQuoteUseCase**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/usecase/AddQuoteUseCase.kt
package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.data.repository.QuoteRepository
import javax.inject.Inject

class AddQuoteUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    suspend operator fun invoke(quote: Quote): Long {
        return repository.addQuote(quote)
    }
}
```

- [ ] **Step 4: 创建 UpdateQuoteUseCase**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/usecase/UpdateQuoteUseCase.kt
package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.data.repository.QuoteRepository
import javax.inject.Inject

class UpdateQuoteUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    suspend operator fun invoke(quote: Quote) {
        repository.updateQuote(quote)
    }
}
```

- [ ] **Step 5: 创建 DeleteQuoteUseCase**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/usecase/DeleteQuoteUseCase.kt
package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.data.repository.QuoteRepository
import javax.inject.Inject

class DeleteQuoteUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    suspend operator fun invoke(quote: Quote) {
        repository.deleteQuote(quote)
    }

    suspend fun deleteById(id: Long) {
        repository.deleteQuoteById(id)
    }
}
```

- [ ] **Step 6: 创建 ToggleFavoriteUseCase**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/usecase/ToggleFavoriteUseCase.kt
package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.repository.QuoteRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: QuoteRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.toggleFavorite(id)
    }
}
```

- [ ] **Step 7: 创建 GetAlarmsUseCase**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/usecase/GetAlarmsUseCase.kt
package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.data.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlarmsUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    operator fun invoke(): Flow<List<Alarm>> {
        return repository.getAllAlarms()
    }

    suspend fun getEnabledAlarms(): List<Alarm> {
        return repository.getEnabledAlarms()
    }
}
```

- [ ] **Step 8: 创建 AddAlarmUseCase**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/usecase/AddAlarmUseCase.kt
package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.data.repository.AlarmRepository
import javax.inject.Inject

class AddAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(alarm: Alarm): Long {
        return repository.addAlarm(alarm)
    }
}
```

- [ ] **Step 9: 创建 UpdateAlarmUseCase**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/usecase/UpdateAlarmUseCase.kt
package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.data.repository.AlarmRepository
import javax.inject.Inject

class UpdateAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(alarm: Alarm) {
        repository.updateAlarm(alarm)
    }
}
```

- [ ] **Step 10: 创建 DeleteAlarmUseCase**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/usecase/DeleteAlarmUseCase.kt
package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.data.repository.AlarmRepository
import javax.inject.Inject

class DeleteAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(alarm: Alarm) {
        repository.deleteAlarm(alarm)
    }

    suspend fun deleteById(id: Long) {
        repository.deleteAlarmById(id)
    }
}
```

- [ ] **Step 11: 创建 ToggleAlarmUseCase**

```kotlin
// app/src/main/java/com/tradeyourplan/domain/usecase/ToggleAlarmUseCase.kt
package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.repository.AlarmRepository
import javax.inject.Inject

class ToggleAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(id: Long, enabled: Boolean) {
        repository.setAlarmEnabled(id, enabled)
    }
}
```

- [ ] **Step 12: 提交**

```bash
git add app/src/main/java/com/tradeyourplan/domain/usecase/
git commit -m "feat: implement use cases for quotes and alarms"
```

---

## Task 8: UI 主题实现

**Files:**
- Create: `app/src/main/java/com/tradeyourplan/ui/theme/Color.kt`
- Create: `app/src/main/java/com/tradeyourplan/ui/theme/Type.kt`
- Create: `app/src/main/java/com/tradeyourplan/ui/theme/Theme.kt`

- [ ] **Step 1: 创建颜色定义**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/theme/Color.kt
package com.tradeyourplan.ui.theme

import androidx.compose.ui.graphics.Color

// 专业深色主题（默认）
val Primary = Color(0xFF1E3A5F)
val OnPrimary = Color(0xFFFFFFFF)
val Secondary = Color(0xFF2563EB)
val Accent = Color(0xFF059669)
val Background = Color(0xFF0F172A)
val Surface = Color(0xFF1E293B)
val OnBackground = Color(0xFFF8FAFC)
val OnSurface = Color(0xFFF8FAFC)
val Muted = Color(0xFF334155)
val Border = Color(0xFF334155)
val Destructive = Color(0xFFDC2626)

// 温馨鼓励主题
val WarmPrimary = Color(0xFF0F766E)
val WarmSecondary = Color(0xFF14B8A6)
val WarmAccent = Color(0xFFF59E0B)
val WarmBackground = Color(0xFFFFFBEB)
val WarmSurface = Color(0xFFFFFFFF)
val WarmOnBackground = Color(0xFF1E293B)
val WarmOnSurface = Color(0xFF1E293B)
val WarmBorder = Color(0xFF99F6E4)

// 极简浅色主题
val LightPrimary = Color(0xFF0F172A)
val LightSecondary = Color(0xFF334155)
val LightAccent = Color(0xFF0369A1)
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF020617)
val LightOnSurface = Color(0xFF020617)
val LightBorder = Color(0xFFE2E8F0)
```

- [ ] **Step 2: 创建字体定义**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/theme/Type.kt
package com.tradeyourplan.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = (32 * 1.2).sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = (24 * 1.3).sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = (18 * 1.4).sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = (16 * 1.5).sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = (14 * 1.5).sp,
        letterSpacing = 0.25.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = (14 * 1.4).sp,
        letterSpacing = 0.1.sp
    )
)
```

- [ ] **Step 3: 创建主题配置**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/theme/Theme.kt
package com.tradeyourplan.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ProfessionalDarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    tertiary = Accent,
    background = Background,
    surface = Surface,
    onBackground = OnBackground,
    onSurface = OnSurface,
    error = Destructive,
    onError = Color.White,
    outline = Border
)

private val WarmEncouragingColorScheme = lightColorScheme(
    primary = WarmPrimary,
    onPrimary = OnPrimary,
    secondary = WarmSecondary,
    tertiary = WarmAccent,
    background = WarmBackground,
    surface = WarmSurface,
    onBackground = WarmOnBackground,
    onSurface = WarmOnSurface,
    error = Destructive,
    onError = Color.White,
    outline = WarmBorder
)

private val MinimalLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = OnPrimary,
    secondary = LightSecondary,
    tertiary = LightAccent,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    error = Destructive,
    onError = Color.White,
    outline = LightBorder
)

enum class ThemeMode {
    PROFESSIONAL_DARK,
    WARM_ENCOURAGING,
    MINIMAL_LIGHT
}

@Composable
fun TradeYourPlanTheme(
    themeMode: ThemeMode = ThemeMode.PROFESSIONAL_DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.PROFESSIONAL_DARK -> ProfessionalDarkColorScheme
        ThemeMode.WARM_ENCOURAGING -> WarmEncouragingColorScheme
        ThemeMode.MINIMAL_LIGHT -> MinimalLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

- [ ] **Step 4: 提交**

```bash
git add app/src/main/java/com/tradeyourplan/ui/theme/
git commit -m "feat: implement UI theme system"
```

---

## Task 9: 通用 UI 组件

**Files:**
- Create: `app/src/main/java/com/tradeyourplan/ui/components/TYPButton.kt`
- Create: `app/src/main/java/com/tradeyourplan/ui/components/TYPOutlinedTextField.kt`
- Create: `app/src/main/java/com/tradeyourplan/ui/components/QuoteCard.kt`
- Create: `app/src/main/java/com/tradeyourplan/ui/components/AlarmCard.kt`
- Create: `app/src/main/java/com/tradeyourplan/ui/components/EmptyState.kt`

- [ ] **Step 1: 创建 TYPButton**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/components/TYPButton.kt
package com.tradeyourplan.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TYPButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    text: String
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun TYPOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}
```

- [ ] **Step 2: 创建 TYPOutlinedTextField**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/components/TYPOutlinedTextField.kt
package com.tradeyourplan.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun TYPOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String? = null,
    supportingText: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 10,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = if (placeholder != null) {
                { Text(placeholder) }
            } else null,
            supportingText = if (supportingText != null) {
                { Text(supportingText) }
            } else null,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        if (supportingText != null) {
            Spacer(Modifier.height(4.dp))
        }
    }
}
```

- [ ] **Step 3: 创建 QuoteCard**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/components/QuoteCard.kt
package com.tradeyourplan.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tradeyourplan.data.model.Quote

@Composable
fun QuoteCard(
    quote: Quote,
    modifier: Modifier = Modifier,
    onFavoriteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 语录内容
            Text(
                text = "\"${quote.content}\"",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            // 分类和来源标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(quote.category.displayName, style = MaterialTheme.typography.bodySmall) }
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text(quote.marketType.displayName, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "分享",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (quote.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "收藏",
                        tint = if (quote.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 4: 创建 AlarmCard**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/components/AlarmCard.kt
package com.tradeyourplan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tradeyourplan.data.model.Alarm

@Composable
fun AlarmCard(
    alarm: Alarm,
    modifier: Modifier = Modifier,
    onToggle: (Boolean) -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = alarm.type.displayName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = alarm.timeDisplay,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
        }
    }
}
```

- [ ] **Step 5: 创建 EmptyState**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/components/EmptyState.kt
package com.tradeyourplan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(
    icon: (@Composable () -> Unit)? = null,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Spacer(Modifier.height(16.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (action != null) {
            Spacer(Modifier.height(24.dp))
            action()
        }
    }
}
```

- [ ] **Step 6: 提交**

```bash
git add app/src/main/java/com/tradeyourplan/ui/components/
git commit -m "feat: implement common UI components"
```

---

## Task 10: 主界面实现

**Files:**
- Create: `app/src/main/java/com/tradeyourplan/ui/main/MainScreen.kt`
- Create: `app/src/main/java/com/tradeyourplan/ui/main/MainViewModel.kt`
- Create: `app/src/main/java/com/tradeyourplan/ui/main/MainActivity.kt`
- Create: `app/src/main/java/com/tradeyourplan/ui/navigation/NavGraph.kt`

- [ ] **Step 1: 创建 MainViewModel**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/main/MainViewModel.kt
package com.tradeyourplan.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.domain.usecase.GetRandomQuoteUseCase
import com.tradeyourplan.domain.usecase.ToggleFavoriteUseCase
import com.tradeyourplan.ui.theme.ThemeMode
import com.tradeyourplan.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getRandomQuoteUseCase: GetRandomQuoteUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .map { modeStr ->
            when (modeStr) {
                "WARM_ENCOURAGING" -> ThemeMode.WARM_ENCOURAGING
                "MINIMAL_LIGHT" -> ThemeMode.MINIMAL_LIGHT
                else -> ThemeMode.PROFESSIONAL_DARK
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.PROFESSIONAL_DARK
        )

    init {
        loadRandomQuote()
    }

    fun loadRandomQuote() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            val quote = getRandomQuoteUseCase()
            _uiState.value = if (quote != null) {
                MainUiState.Success(quote)
            } else {
                MainUiState.Empty
            }
        }
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch {
            toggleFavoriteUseCase(id)
            // 重新加载以更新状态
            loadRandomQuote()
        }
    }
}

sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(val quote: Quote) : MainUiState()
    object Empty : MainUiState()
}
```

- [ ] **Step 2: 创建 MainScreen**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/main/MainScreen.kt
package com.tradeyourplan.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradeyourplan.ui.components.QuoteCard
import com.tradeyourplan.ui.theme.TradeYourPlanTheme

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToQuotes: () -> Unit = {},
    onNavigateToAlarms: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    var selectedTab by remember { mutableStateOf("home") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("交易你的计划") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                BottomNavItem("home", Icons.Default.Home, "首页")
                BottomNavItem("quotes", Icons.Default.FormatQuote, "语录")
                BottomNavItem("alarms", Icons.Default.Alarm, "闹钟")
                BottomNavItem("settings", Icons.Default.Settings, "设置")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                "home" -> HomeTab(
                    uiState = uiState,
                    onRefresh = { viewModel.loadRandomQuote() },
                    onFavorite = { id -> viewModel.toggleFavorite(id) }
                )
                "quotes" -> LaunchedEffect(Unit) { onNavigateToQuotes() }
                "alarms" -> LaunchedEffect(Unit) { onNavigateToAlarms() }
                "settings" -> LaunchedEffect(Unit) { onNavigateToSettings() }
            }
        }
    }
}

@Composable
private fun HomeTab(
    uiState: MainUiState,
    onRefresh: () -> Unit,
    onFavorite: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (uiState) {
            is MainUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is MainUiState.Success -> {
                QuoteCard(
                    quote = (uiState as MainUiState.Success).quote,
                    modifier = Modifier.weight(1f),
                    onFavoriteClick = { onFavorite((uiState as MainUiState.Success).quote.id) },
                    onShareClick = { /* TODO */ }
                )
                TYPButton(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth(),
                    icon = { Icon(Icons.Default.Refresh, null) },
                    text = "换一换"
                )
            }
            is MainUiState.Empty -> {
                Text("暂无语录，请先添加语录")
            }
        }
    }
}

@Composable
private fun NavigationBar(
    items: List<BottomNavItem>,
    onItemClicked: (String) -> Unit = {}
) {
    var selectedItem by remember { mutableStateOf(0) }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    onItemClicked(item.route)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            )
        }
    }
}
```

- [ ] **Step 3: 创建导航图**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/navigation/NavGraph.kt
package com.tradeyourplan.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Quotes : Screen("quotes")
    object AlarmEdit : Screen("alarm_edit/{alarmId}") {
        fun create(alarmId: Long = 0) = "alarm_edit/$alarmId"
    }
    object Settings : Screen("settings")
    object ThemePicker : Screen("theme_picker")
}
```

- [ ] **Step 4: 创建 MainActivity**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/main/MainActivity.kt
package com.tradeyourplan.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tradeyourplan.ui.theme.TradeYourPlanTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TradeYourPlanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}
```

- [ ] **Step 5: 提交**

```bash
git add app/src/main/java/com/tradeyourplan/ui/main/ app/src/main/java/com/tradeyourplan/ui/navigation/
git commit -m "feat: implement main screen and navigation"
```

---

## Task 11: 语录管理界面

**Files:**
- Create: `app/src/main/java/com/tradeyourplan/ui/quote/QuoteListScreen.kt`
- Create: `app/src/main/java/com/tradeyourplan/ui/quote/QuoteViewModel.kt`
- Create: `app/src/main/java/com/tradeyourplan/ui/quote/AddQuoteScreen.kt`

- [ ] **Step 1: 创建 QuoteViewModel**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/quote/QuoteViewModel.kt
package com.tradeyourplan.ui.quote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource
import com.tradeyourplan.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val getQuotesUseCase: GetQuotesUseCase,
    private val addQuoteUseCase: AddQuoteUseCase,
    private val updateQuoteUseCase: UpdateQuoteUseCase,
    private val deleteQuoteUseCase: DeleteQuoteUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val quotes: StateFlow<List<Quote>> = getQuotesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val favoriteQuotes: StateFlow<List<Quote>> = getQuotesUseCase.getFavoriteQuotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _filterCategory = MutableStateFlow<Category?>(null)
    val filterCategory: StateFlow<Category?> = _filterCategory.asStateFlow()

    fun setFilterCategory(category: Category?) {
        _filterCategory.value = category
    }

    fun addQuote(
        content: String,
        category: Category,
        marketType: MarketType
    ) {
        viewModelScope.launch {
            val quote = Quote(
                content = content,
                category = category,
                marketType = marketType,
                source = QuoteSource.USER
            )
            addQuoteUseCase(quote)
        }
    }

    fun updateQuote(quote: Quote) {
        viewModelScope.launch {
            updateQuoteUseCase(quote)
        }
    }

    fun deleteQuote(quote: Quote) {
        viewModelScope.launch {
            deleteQuoteUseCase(quote)
        }
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch {
            toggleFavoriteUseCase(id)
        }
    }
}
```

- [ ] **Step 2: 创建 QuoteListScreen**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/quote/QuoteListScreen.kt
package com.tradeyourplan.ui.quote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteListScreen(
    viewModel: QuoteViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onAddQuote: () -> Unit = {}
) {
    val quotes by viewModel.quotes.collectAsState()
    val filterCategory by viewModel.filterCategory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("语录列表") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onAddQuote) {
                        Icon(Icons.Default.Add, "添加语录")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (quotes.isEmpty()) {
            EmptyState(
                icon = {
                    Icon(
                        Icons.Outlined.Inbox,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                },
                title = "暂无语录",
                message = "点击右上角添加你的第一条交易智慧",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 分类筛选
                item {
                    CategoryFilterRow(
                        selectedCategory = filterCategory,
                        onCategorySelected = { viewModel.setFilterCategory(it) }
                    )
                }

                // 语录列表
                items(quotes) { quote ->
                    QuoteCard(
                        quote = quote,
                        onFavoriteClick = { viewModel.toggleFavorite(quote.id) },
                        onShareClick = { /* TODO */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit
) {
    val categories = listOf(null) + Category.values()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        category?.displayName ?: "全部",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                shape = MaterialTheme.shapes.small
            )
        }
    }
}
```

- [ ] **Step 3: 创建 AddQuoteScreen**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/quote/AddQuoteScreen.kt
package com.tradeyourplan.ui.quote

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuoteScreen(
    viewModel: QuoteViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Category.GENERAL) }
    var selectedMarketType by remember { mutableStateOf(MarketType.GENERAL) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showMarketTypeMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加语录") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 语录内容
            TYPOutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = "语录内容",
                placeholder = "输入交易智慧...",
                supportingText = if (content.length > 200) "已超过 200 字符" else "${content.length}/200",
                singleLine = false,
                maxLines = 5
            )

            // 分类选择
            ExposedDropdownMenuBox(
                expanded = showCategoryMenu,
                onExpandedChange = { showCategoryMenu = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("分类") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    Category.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = {
                                selectedCategory = category
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

            // 市场类型选择
            ExposedDropdownMenuBox(
                expanded = showMarketTypeMenu,
                onExpandedChange = { showMarketTypeMenu = it }
            ) {
                OutlinedTextField(
                    value = selectedMarketType.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("市场类型") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMarketTypeMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showMarketTypeMenu,
                    onDismissRequest = { showMarketTypeMenu = false }
                ) {
                    MarketType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = {
                                selectedMarketType = type
                                showMarketTypeMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // 保存按钮
            TYPButton(
                onClick = {
                    if (content.isNotBlank()) {
                        viewModel.addQuote(content, selectedCategory, selectedMarketType)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = content.isNotBlank(),
                text = "保存"
            )
        }
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add app/src/main/java/com/tradeyourplan/ui/quote/
git commit -m "feat: implement quote management screens"
```

---

## Task 12: 闹钟界面实现

**Files:**
- Create: `app/src/main/java/com/tradeyourplan/ui/alarm/AlarmListScreen.kt`
- Create: `app/src/main/java/com/tradeyourplan/ui/alarm/AlarmViewModel.kt`
- Create: `app/src/main/java/com/tradeyourplan/ui/alarm/AlarmEditScreen.kt`

- [ ] **Step 1: 创建 AlarmViewModel**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/alarm/AlarmViewModel.kt
package com.tradeyourplan.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val getAlarmsUseCase: GetAlarmsUseCase,
    private val addAlarmUseCase: AddAlarmUseCase,
    private val updateAlarmUseCase: UpdateAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val toggleAlarmUseCase: ToggleAlarmUseCase
) : ViewModel() {

    val alarms: StateFlow<List<Alarm>> = getAlarmsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            addAlarmUseCase(alarm)
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            updateAlarmUseCase(alarm)
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            deleteAlarmUseCase(alarm)
        }
    }

    fun toggleAlarm(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            toggleAlarmUseCase(id, enabled)
        }
    }
}
```

- [ ] **Step 2: 创建 AlarmListScreen**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/alarm/AlarmListScreen.kt
package com.tradeyourplan.ui.alarm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    viewModel: AlarmViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onAddAlarm: () -> Unit = {},
    onEditAlarm: (Long) -> Unit = {}
) {
    val alarms by viewModel.alarms.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("提醒设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onAddAlarm) {
                        Icon(Icons.Default.Add, "添加提醒")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (alarms.isEmpty()) {
            EmptyState(
                icon = {
                    Icon(
                        Icons.Outlined.Inbox,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                },
                title = "暂无提醒",
                message = "设置一个提醒，让交易智慧常伴左右",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alarms) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggle = { enabled -> viewModel.toggleAlarm(alarm.id, enabled) },
                        onClick = { onEditAlarm(alarm.id) }
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 3: 创建 AlarmEditScreen**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/alarm/AlarmEditScreen.kt
package com.tradeyourplan.ui.alarm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.timepicker.TimePickerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.domain.model.*
import com.tradeyourplan.ui.components.*
import java.util.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    alarmId: Long = 0,
    viewModel: AlarmViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    var selectedType by remember { mutableStateOf(AlarmType.FIXED) }
    var timePickerState by remember {
        mutableStateOf(TimePickerState(9, 30, is24Hour = true))
    }
    var startHour by remember { mutableIntStateOf(9) }
    var endHour by remember { mutableIntStateOf(15) }
    var selectedRepeatMode by remember { mutableStateOf(RepeatMode.DAILY) }
    var selectedNotificationLevel by remember { mutableStateOf(NotificationLevel.NORMAL) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (alarmId == 0L) "添加提醒" else "编辑提醒") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 提醒类型选择
            Text("提醒类型", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AlarmType.values().forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.displayName, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 根据类型显示不同配置
            when (selectedType) {
                AlarmType.FIXED -> {
                    Text("提醒时间", style = MaterialTheme.typography.titleMedium)
                    // 时间选择器（简化版，实际使用 TimePicker）
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = timePickerState.hour.toString(),
                            onValueChange = { },
                            label = { Text("时") },
                            modifier = Modifier.weight(1f),
                            readOnly = true
                        )
                        Text(":")
                        OutlinedTextField(
                            value = timePickerState.minute.toString().padStart(2, '0'),
                            onValueChange = { },
                            label = { Text("分") },
                            modifier = Modifier.weight(1f),
                            readOnly = true
                        )
                    }
                }
                AlarmType.RANDOM -> {
                    Text("时间范围", style = MaterialTheme.typography.titleMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startHour.toString(),
                            onValueChange = { startHour = it.toIntOrNull() ?: 9 },
                            label = { Text("开始时") },
                            modifier = Modifier.weight(1f)
                        )
                        Text("~")
                        OutlinedTextField(
                            value = endHour.toString(),
                            onValueChange = { endHour = it.toIntOrNull() ?: 15 },
                            label = { Text("结束时") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                AlarmType.EVENT_TRIGGERED -> {
                    Text("事件触发", style = MaterialTheme.typography.titleMedium)
                    Text("检测到交易应用启动后触发提醒", style = MaterialTheme.typography.bodySmall)
                }
            }

            // 重复模式
            Text("重复模式", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RepeatMode.values().forEach { mode ->
                    FilterChip(
                        selected = selectedRepeatMode == mode,
                        onClick = { selectedRepeatMode = mode },
                        label = { Text(mode.displayName, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 通知强度
            Text("通知强度", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationLevel.values().forEach { level ->
                    FilterChip(
                        selected = selectedNotificationLevel == level,
                        onClick = { selectedNotificationLevel = level },
                        label = { Text(level.displayName, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // 保存按钮
            TYPButton(
                onClick = {
                    val alarm = Alarm(
                        id = alarmId,
                        type = selectedType,
                        hour = if (selectedType == AlarmType.FIXED) timePickerState.hour else null,
                        minute = if (selectedType == AlarmType.FIXED) timePickerState.minute else null,
                        startHour = if (selectedType == AlarmType.RANDOM) startHour else null,
                        endHour = if (selectedType == AlarmType.RANDOM) endHour else null,
                        repeatMode = selectedRepeatMode,
                        notificationLevel = selectedNotificationLevel
                    )
                    if (alarmId == 0L) {
                        viewModel.addAlarm(alarm)
                    } else {
                        viewModel.updateAlarm(alarm)
                    }
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                text = "保存"
            )
        }
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add app/src/main/java/com/tradeyourplan/ui/alarm/
git commit -m "feat: implement alarm management screens"
```

---

## Task 13: 设置界面实现

**Files:**
- Create: `app/src/main/java/com/tradeyourplan/ui/settings/SettingsScreen.kt`
- Create: `app/src/main/java/com/tradeyourplan/ui/settings/SettingsViewModel.kt`
- Create: `app/src/main/java/com/tradeyourplan/ui/settings/ThemePickerScreen.kt`

- [ ] **Step 1: 创建 SettingsViewModel**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/settings/SettingsViewModel.kt
package com.tradeyourplan.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradeyourplan.data.repository.SettingsRepository
import com.tradeyourplan.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.PROFESSIONAL_DARK
        )

    val quoteSource: StateFlow<String> = settingsRepository.quoteSource
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "MIXED"
        )

    val notificationLevel: StateFlow<String> = settingsRepository.notificationLevel
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "NORMAL"
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode.name)
        }
    }

    fun setQuoteSource(source: String) {
        viewModelScope.launch {
            settingsRepository.setQuoteSource(source)
        }
    }

    fun setNotificationLevel(level: String) {
        viewModelScope.launch {
            settingsRepository.setNotificationLevel(level)
        }
    }
}
```

- [ ] **Step 2: 创建 SettingsScreen**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/settings/SettingsScreen.kt
package com.tradeyourplan.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradeyourplan.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onThemePicker: () -> Unit = {}
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val quoteSource by viewModel.quoteSource.collectAsState()
    val notificationLevel by viewModel.notificationLevel.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 主题选择
            SettingItem(
                title = "主题",
                description = getThemeName(themeMode),
                onClick = onThemePicker,
                icon = Icons.Default.Palette
            )

            Divider()

            // 语录来源
            SettingDropdown(
                title = "语录来源",
                selectedValue = quoteSource,
                options = mapOf(
                    "SYSTEM" to "仅系统",
                    "USER" to "仅用户",
                    "MIXED" to "混合"
                ),
                onValueChange = { viewModel.setQuoteSource(it) },
                icon = Icons.Default.FormatQuote
            )

            Divider()

            // 通知强度
            SettingDropdown(
                title = "通知强度",
                selectedValue = notificationLevel,
                options = mapOf(
                    "SILENT" to "静默",
                    "NORMAL" to "标准",
                    "FULL_SCREEN" to "强提醒"
                ),
                onValueChange = { viewModel.setNotificationLevel(it) },
                icon = Icons.Default.Notifications
            )

            Divider()

            // 关于
            SettingItem(
                title = "关于",
                description = "TradeYourPlan v1.0.0",
                onClick = { },
                icon = Icons.Default.Info
            )
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingDropdown(
    title: String,
    selectedValue: String,
    options: Map<String, String>,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Divider()
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .menuAnchor(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        options[selectedValue] ?: selectedValue,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    options.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onValueChange(key)
                                expanded = false
                            },
                            leadingIcon = if (key == selectedValue) {
                                { Icon(Icons.Default.Check, null) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

private fun getThemeName(mode: ThemeMode): String {
    return when (mode) {
        ThemeMode.PROFESSIONAL_DARK -> "专业深色"
        ThemeMode.WARM_ENCOURAGING -> "温馨鼓励"
        ThemeMode.MINIMAL_LIGHT -> "极简浅色"
    }
}
```

- [ ] **Step 3: 创建 ThemePickerScreen**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/settings/ThemePickerScreen.kt
package com.tradeyourplan.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradeyourplan.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val currentTheme by viewModel.themeMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择主题") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "选择你喜欢的主题风格",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ThemeCard(
                name = "专业深色",
                description = "深色调，专业金融风格",
                previewColors = listOf(Primary, Secondary, Accent),
                isSelected = currentTheme == ThemeMode.PROFESSIONAL_DARK,
                onClick = { viewModel.setThemeMode(ThemeMode.PROFESSIONAL_DARK) }
            )

            ThemeCard(
                name = "温馨鼓励",
                description = "暖色调，温暖鼓励",
                previewColors = listOf(WarmPrimary, WarmSecondary, WarmAccent),
                isSelected = currentTheme == ThemeMode.WARM_ENCOURAGING,
                onClick = { viewModel.setThemeMode(ThemeMode.WARM_ENCOURAGING) }
            )

            ThemeCard(
                name = "极简浅色",
                description = "浅色调，清爽简约",
                previewColors = listOf(LightPrimary, LightSecondary, LightAccent),
                isSelected = currentTheme == ThemeMode.MINIMAL_LIGHT,
                onClick = { viewModel.setThemeMode(ThemeMode.MINIMAL_LIGHT) }
            )
        }
    }
}

@Composable
private fun ThemeCard(
    name: String,
    description: String,
    previewColors: List<Color>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 颜色预览
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                previewColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color, RoundedCornerShape(8.dp))
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(2f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add app/src/main/java/com/tradeyourplan/ui/settings/
git commit -m "feat: implement settings and theme picker screens"
```

---

## Task 14: 通知系统实现

**Files:**
- Create: `app/src/main/java/com/tradeyourplan/notification/NotificationHelper.kt`
- Create: `app/src/main/java/com/tradeyourplan/notification/AlarmReceiver.kt`
- Create: `app/src/main/java/com/tradeyourplan/notification/AlarmScheduler.kt`

- [ ] **Step 1: 创建 NotificationHelper**

```kotlin
// app/src/main/java/com/tradeyourplan/notification/NotificationHelper.kt
package com.tradeyourplan.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tradeyourplan.R
import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.ui.main.MainActivity

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "trade_quotes"
        private const val CHANNEL_NAME = "交易提醒"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "交易智慧提醒通知"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showQuoteNotification(quote: Quote) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("交易提醒")
            .setContentText(quote.content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(quote.content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // 权限未授予
        }
    }
}
```

- [ ] **Step 2: 创建 AlarmReceiver**

```kotlin
// app/src/main/java/com/tradeyourplan/notification/AlarmReceiver.kt
package com.tradeyourplan.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var getRandomQuoteUseCase: com.tradeyourplan.domain.usecase.GetRandomQuoteUseCase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val notificationHelper = NotificationHelper(context)

        scope.launch {
            val quote = getRandomQuoteUseCase()
            if (quote != null) {
                notificationHelper.showQuoteNotification(quote)
            }
        }
    }
}
```

- [ ] **Step 3: 创建 AlarmScheduler**

```kotlin
// app/src/main/java/com/tradeyourplan/notification/AlarmScheduler.kt
package com.tradeyourplan.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.domain.model.AlarmType

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: Alarm) {
        when (alarm.type) {
            AlarmType.FIXED -> scheduleFixedAlarm(alarm)
            AlarmType.RANDOM -> scheduleRandomAlarm(alarm)
            AlarmType.EVENT_TRIGGERED -> scheduleEventAlarm(alarm)
        }
    }

    private fun scheduleFixedAlarm(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.tradeyourplan.ALARM_TRIGGERED"
            putExtra("alarm_id", alarm.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, alarm.hour ?: 9)
            set(java.util.Calendar.MINUTE, alarm.minute ?: 0)
            set(java.util.Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun scheduleRandomAlarm(alarm: Alarm) {
        val startHour = alarm.startHour ?: 9
        val endHour = alarm.endHour ?: 15

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.tradeyourplan.RANDOM_ALARM_TRIGGERED"
            putExtra("alarm_id", alarm.id)
            putExtra("start_hour", startHour)
            putExtra("end_hour", endHour)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, startHour)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }

        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            (endHour - startHour) * 3600 * 1000L,
            pendingIntent
        )
    }

    private fun scheduleEventAlarm(alarm: Alarm) {
        // 事件触发需要通过其他机制（如 UsageStatsManager）
        // 这里简化处理，仅作为占位
    }

    fun cancelAlarm(alarmId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
        }
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add app/src/main/java/com/tradeyourplan/notification/
git commit -m "feat: implement notification system"
```

---

## Task 15: 分享功能实现

**Files:**
- Create: `app/src/main/java/com/tradeyourplan/util/ShareHelper.kt`

- [ ] **Step 1: 创建 ShareHelper**

```kotlin
// app/src/main/java/com/tradeyourplan/util/ShareHelper.kt
package com.tradeyourplan.util

import android.content.Context
import android.content.Intent
import com.tradeyourplan.data.model.Quote

class ShareHelper(private val context: Context) {

    fun shareQuote(quote: Quote) {
        val shareText = """
            |"${quote.content}"
            |
            |— 交易你的计划
            |
            |#${quote.category.displayName} #交易智慧
        """.trimMargin()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        context.startActivity(Intent.createChooser(intent, "分享语录"))
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add app/src/main/java/com/tradeyourplan/util/
git commit -m "feat: implement share functionality"
```

---

## Task 16: 完善导航和集成

**Files:**
- Modify: `app/src/main/java/com/tradeyourplan/ui/main/MainScreen.kt`
- Modify: `app/src/main/java/com/tradeyourplan/ui/main/MainActivity.kt`
- Create: `app/src/main/java/com/tradeyourplan/ui/navigation/TYPNavHost.kt`

- [ ] **Step 1: 更新导航宿主**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/navigation/TYPNavHost.kt
package com.tradeyourplan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tradeyourplan.ui.alarm.AlarmEditScreen
import com.tradeyourplan.ui.alarm.AlarmListScreen
import com.tradeyourplan.ui.main.MainScreen
import com.tradeyourplan.ui.quote.AddQuoteScreen
import com.tradeyourplan.ui.quote.QuoteListScreen
import com.tradeyourplan.ui.settings.SettingsScreen
import com.tradeyourplan.ui.settings.ThemePickerScreen

@Composable
fun TYPNavHost(
    navController: NavController,
    startDestination: String = Screen.Main.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToQuotes = { navController.navigate(Screen.Quotes.route) },
                onNavigateToAlarms = { navController.navigate(Screen.Alarms.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Quotes.route) {
            QuoteListScreen(
                onBack = { navController.popBackStack() },
                onAddQuote = { navController.navigate("add_quote") }
            )
        }

        composable("add_quote") {
            AddQuoteScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Alarms.route) {
            AlarmListScreen(
                onBack = { navController.popBackStack() },
                onAddAlarm = { navController.navigate(Screen.AlarmEdit.create()) },
                onEditAlarm = { alarmId ->
                    navController.navigate(Screen.AlarmEdit.create(alarmId))
                }
            )
        }

        composable(
            route = Screen.AlarmEdit.route,
            arguments = listOf(
                navArgument("alarmId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: 0L
            AlarmEditScreen(
                alarmId = alarmId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onThemePicker = { navController.navigate(Screen.ThemePicker.route) }
            )
        }

        composable(Screen.ThemePicker.route) {
            ThemePickerScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

- [ ] **Step 2: 更新 MainActivity**

```kotlin
// app/src/main/java/com/tradeyourplan/ui/main/MainActivity.kt
package com.tradeyourplan.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.tradeyourplan.ui.navigation.TYPNavHost
import com.tradeyourplan.ui.theme.TradeYourPlanTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TradeYourPlanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    TYPNavHost(navController)
                }
            }
        }
    }
}
```

- [ ] **Step 3: 添加图标资源**

在 `app/src/main/res/drawable/` 中添加所需的图标，或使用 Material Icons。

- [ ] **Step 4: 提交**

```bash
git add app/src/main/java/com/tradeyourplan/ui/navigation/ app/src/main/java/com/tradeyourplan/ui/main/MainActivity.kt
git commit -m "feat: integrate navigation and complete UI"
```

---

## Task 17: 单元测试

**Files:**
- Create: `app/src/test/java/com/tradeyourplan/domain/usecase/GetRandomQuoteUseCaseTest.kt`
- Create: `app/src/test/java/com/tradeyourplan/data/repository/QuoteRepositoryTest.kt`

- [ ] **Step 1: 创建 GetRandomQuoteUseCase 测试**

```kotlin
// app/src/test/java/com/tradeyourplan/domain/usecase/GetRandomQuoteUseCaseTest.kt
package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.data.repository.QuoteRepository
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class GetRandomQuoteUseCaseTest {

    @Mock
    private lateinit var repository: QuoteRepository

    private lateinit var useCase: GetRandomQuoteUseCase

    private val testQuotes = listOf(
        Quote(1, "Test 1", Category.DISCIPLINE, MarketType.GENERAL, QuoteSource.SYSTEM),
        Quote(2, "Test 2", Category.RISK_MGMT, MarketType.STOCK, QuoteSource.USER)
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = GetRandomQuoteUseCase(repository)
    }

    @Test
    fun `invoke returns quote when available`() = runTest {
        `when`(repository.getRandomQuote(null, null, null)).thenReturn(testQuotes[0])

        val result = useCase()

        assertNotNull(result)
    }

    @Test
    fun `invoke returns null when no quotes`() = runTest {
        `when`(repository.getRandomQuote(null, null, null)).thenReturn(null)

        val result = useCase()

        assertNull(result)
    }

    @Test
    fun `invoke filters by category`() = runTest {
        `when`(repository.getRandomQuote(null, Category.DISCIPLINE, null)).thenReturn(testQuotes[0])

        val result = useCase(category = Category.DISCIPLINE)

        assertNotNull(result)
    }
}
```

- [ ] **Step 2: 创建 QuoteRepository 测试**

```kotlin
// app/src/test/java/com/tradeyourplan/data/repository/QuoteRepositoryTest.kt
package com.tradeyourplan.data.repository

import com.tradeyourplan.data.local.dao.QuoteDao
import com.tradeyourplan.data.local.entity.QuoteEntity
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.domain.model.QuoteSource
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class QuoteRepositoryTest {

    @Mock
    private lateinit var quoteDao: QuoteDao

    private lateinit var repository: QuoteRepository

    private val testEntities = listOf(
        QuoteEntity(1, "Test 1", "DISCIPLINE", "GENERAL", "SYSTEM", false, 0),
        QuoteEntity(2, "Test 2", "RISK_MGMT", "STOCK", "USER", false, 0)
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = QuoteRepository(quoteDao)
    }

    @Test
    fun `toggleFavorite calls dao toggleFavorite`() = runTest {
        repository.toggleFavorite(1)

        verify(quoteDao).toggleFavorite(1)
    }

    @Test
    fun `deleteQuoteById calls dao deleteQuoteById`() = runTest {
        repository.deleteQuoteById(1)

        verify(quoteDao).deleteQuoteById(1)
    }

    @Test
    fun `insertInitialQuotes calls dao insertQuotes`() = runTest {
        val quotes = testEntities.map { it.toDomainModel() }

        repository.insertInitialQuotes(quotes)

        verify(quoteDao).insertQuotes(testEntities)
    }
}
```

- [ ] **Step 3: 添加测试依赖到 build.gradle.kts**

```kotlin
// 添加到 app/build.gradle.kts 的 dependencies 块
testImplementation("org.mockito:mockito-core:5.7.0")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
```

- [ ] **Step 4: 提交**

```bash
git add app/src/test/ app/build.gradle.kts
git commit -m "test: add unit tests for domain and data layers"
```

---

## Task 18: 清理和优化

**Files:**
- Create: `app/proguard-rules.pro`
- Update: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`

- [ ] **Step 1: 创建 ProGuard 规则**

```kotlin
// app/proguard-rules.pro
-dontwarn com.tradeyourplan.**
-keep class com.tradeyourplan.data.model.** { *; }
-keep class com.tradeyourplan.domain.model.** { *; }
-keep class com.tradeyourplan.data.local.entity.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
```

- [ ] **Step 2: 运行代码检查**

```bash
./gradlew lint
./gradlew test
```

- [ ] **Step 3: 构建发布版本**

```bash
./gradlew assembleRelease
```

- [ ] **Step 4: 提交**

```bash
git add app/proguard-rules.pro
git commit -m "chore: add ProGuard rules and final optimizations"
```

---

## 验收标准

实施完成后，应用应满足以下标准：

1. **功能验收**
   - [ ] 应用启动后显示今日语录
   - [ ] 可以换一换随机显示语录
   - [ ] 可以收藏语录
   - [ ] 可以分享语录
   - [ ] 可以添加自定义语录
   - [ ] 可以编辑和删除语录
   - [ ] 可以添加固定时间提醒
   - [ ] 可以添加随机时间提醒
   - [ ] 可以切换主题（3种）
   - [ ] 可以配置通知强度
   - [ ] 可以配置语录来源偏好

2. **UI 验收**
   - [ ] 所有触摸目标 ≥48dp
   - [ ] 对比度符合 WCAG AA 标准
   - [ ] 支持深色和浅色主题
   - [ ] 动画流畅（150-300ms）
   - [ ] 空状态有友好提示

3. **技术验收**
   - [ ] 代码通过 lint 检查
   - [ ] 单元测试覆盖率 >70%
   - [ ] 无内存泄漏
   - [ ] 遵循 Material Design 规范

---

**实施计划完成**

总任务数：18
预计工作量：每个任务 30-60 分钟
总工作量：约 9-18 小时
