package com.trading.review.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 交易记录实体
 */
@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val symbol: String,
    val type: String, // BUY or SELL
    val entryPrice: Float,
    val closePrice: Float?,
    val volume: Float,
    val stopLoss: Float,
    val takeProfit: Float,
    val openTime: Long,
    val closeTime: Long?,
    val pnl: Float?,
    val status: String, // OPEN or CLOSED
    val notes: String? = null
)

/**
 * 复盘会话实体
 */
@Entity(tableName = "replay_sessions")
data class ReplaySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val symbol: String,
    val timeframe: String,
    val startTime: Long,
    val endTime: Long?,
    val startCandleIndex: Int,
    val endCandleIndex: Int,
    val totalTrades: Int = 0,
    val winningTrades: Int = 0,
    val losingTrades: Int = 0,
    val totalPnl: Float = 0f,
    val notes: String? = null
)

/**
 * 用户配置实体
 */
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
