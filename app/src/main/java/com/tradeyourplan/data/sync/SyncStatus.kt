package com.tradeyourplan.data.sync

/**
 * 同步状态
 */
enum class SyncStatus {
    NEVER,      // 从未同步
    SUCCESS,    // 同步成功
    FAILED,     // 同步失败
    SYNCING     // 正在同步
}
