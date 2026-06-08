package com.trading.review.persistence

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    
    @Query("SELECT * FROM trades ORDER BY openTime DESC")
    fun getAllTrades(): Flow<List<TradeEntity>>
    
    @Query("SELECT * FROM trades WHERE status = 'OPEN' ORDER BY openTime DESC")
    fun getOpenTrades(): Flow<List<TradeEntity>>
    
    @Query("SELECT * FROM trades WHERE status = 'CLOSED' ORDER BY closeTime DESC")
    fun getClosedTrades(): Flow<List<TradeEntity>>
    
    @Query("SELECT * FROM trades WHERE id = :id")
    suspend fun getTradeById(id: Long): TradeEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trade: TradeEntity): Long
    
    @Update
    suspend fun update(trade: TradeEntity)
    
    @Delete
    suspend fun delete(trade: TradeEntity)
    
    @Query("DELETE FROM trades WHERE status = 'CLOSED'")
    suspend fun deleteClosedTrades()
    
    @Query("SELECT COUNT(*) FROM trades")
    suspend fun getCount(): Int
    
    @Query("SELECT SUM(pnl) FROM trades WHERE status = 'CLOSED'")
    suspend fun getTotalPnl(): Float?
}
