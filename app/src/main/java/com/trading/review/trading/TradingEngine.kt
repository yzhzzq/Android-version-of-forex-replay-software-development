package com.trading.review.trading

/**
 * 订单类型
 */
enum class OrderType {
    BUY,
    SELL
}

/**
 * 订单状态
 */
enum class OrderStatus {
    OPEN,
    CLOSED,
    PENDING
}

/**
 * 持仓数据结构
 */
data class Position(
    val id: Long,
    val symbol: String,
    val type: OrderType,
    val entryPrice: Float,
    val stopLoss: Float,
    val takeProfit: Float,
    val volume: Float, // 手数
    val openTime: Long,
    var closeTime: Long? = null,
    var closePrice: Float? = null,
    var status: OrderStatus = OrderStatus.OPEN
) {
    /**
     * 计算当前盈亏
     * @param currentPrice 当前价格
     */
    fun calculatePnL(currentPrice: Float): Float {
        return when (type) {
            OrderType.BUY -> (currentPrice - entryPrice) * volume * 100000f
            OrderType.SELL -> (entryPrice - currentPrice) * volume * 100000f
        }
    }
    
    /**
     * 计算已实现盈亏
     */
    fun calculateRealizedPnL(): Float {
        val close = closePrice ?: return 0f
        return when (type) {
            OrderType.BUY -> (close - entryPrice) * volume * 100000f
            OrderType.SELL -> (entryPrice - close) * volume * 100000f
        }
    }
    
    /**
     * 检查是否触发止损
     */
    fun checkStopLoss(currentPrice: Float): Boolean {
        return when (type) {
            OrderType.BUY -> stopLoss > 0 && currentPrice <= stopLoss
            OrderType.SELL -> stopLoss > 0 && currentPrice >= stopLoss
        }
    }
    
    /**
     * 检查是否触发止盈
     */
    fun checkTakeProfit(currentPrice: Float): Boolean {
        return when (type) {
            OrderType.BUY -> takeProfit > 0 && currentPrice >= takeProfit
            OrderType.SELL -> takeProfit > 0 && currentPrice <= takeProfit
        }
    }
}

/**
 * 交易系统引擎
 * 管理订单、持仓和风险控制
 */
class TradingEngine {
    
    private val positions = mutableListOf<Position>()
    private var nextOrderId = 1L
    
    /**
     * 获取所有未平仓持仓
     */
    fun getOpenPositions(): List<Position> {
        return positions.filter { it.status == OrderStatus.OPEN }
    }
    
    /**
     * 获取所有已平仓持仓
     */
    fun getClosedPositions(): List<Position> {
        return positions.filter { it.status == OrderStatus.CLOSED }
    }
    
    /**
     * 开仓
     */
    fun openPosition(
        symbol: String,
        type: OrderType,
        entryPrice: Float,
        volume: Float,
        stopLoss: Float = 0f,
        takeProfit: Float = 0f
    ): Position {
        val position = Position(
            id = nextOrderId++,
            symbol = symbol,
            type = type,
            entryPrice = entryPrice,
            stopLoss = stopLoss,
            takeProfit = takeProfit,
            volume = volume,
            openTime = System.currentTimeMillis()
        )
        
        positions.add(position)
        return position
    }
    
    /**
     * 平仓
     */
    fun closePosition(positionId: Long, closePrice: Float): Position? {
        val position = positions.find { it.id == positionId && it.status == OrderStatus.OPEN }
            ?: return null
        
        position.closePrice = closePrice
        position.closeTime = System.currentTimeMillis()
        position.status = OrderStatus.CLOSED
        
        return position
    }
    
    /**
     * 检查并执行止损止盈
     * @return 被平仓的订单列表
     */
    fun checkStopLossTakeProfit(currentPrice: Float): List<Position> {
        val closedPositions = mutableListOf<Position>()
        
        for (position in getOpenPositions()) {
            if (position.checkStopLoss(currentPrice)) {
                closePosition(position.id, position.stopLoss)?.let {
                    closedPositions.add(it)
                }
            } else if (position.checkTakeProfit(currentPrice)) {
                closePosition(position.id, position.takeProfit)?.let {
                    closedPositions.add(it)
                }
            }
        }
        
        return closedPositions
    }
    
    /**
     * 计算总浮盈浮亏
     */
    fun getTotalUnrealizedPnL(currentPrice: Float): Float {
        return getOpenPositions().sumOf { it.calculatePnL(currentPrice).toDouble() }.toFloat()
    }
    
    /**
     * 计算总已实现盈亏
     */
    fun getTotalRealizedPnL(): Float {
        return getClosedPositions().sumOf { it.calculateRealizedPnL().toDouble() }.toFloat()
    }
    
    /**
     * 获取持仓数量
     */
    fun getPositionCount(): Int {
        return positions.size
    }
    
    /**
     * 获取未平仓数量
     */
    fun getOpenPositionCount(): Int {
        return getOpenPositions().size
    }
}
