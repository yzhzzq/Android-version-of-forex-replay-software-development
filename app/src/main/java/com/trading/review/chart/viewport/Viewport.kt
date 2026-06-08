package com.trading.review.chart.viewport

/**
 * 视口配置
 * 定义屏幕上可见的K线范围
 */
data class Viewport(
    var startIndex: Int = 0,      // 可见区域起始索引
    var visibleCount: Int = 100,   // 可见K线数量
    var candleWidth: Float = 10f   // 单根K线宽度 (像素)
) {
    /**
     * 计算可见区域结束索引
     */
    val endIndex: Int get() = startIndex + visibleCount
    
    /**
     * 检查索引是否在可见范围内
     */
    fun isVisible(index: Int): Boolean {
        return index >= startIndex && index < endIndex
    }
    
    /**
     * 限制startIndex在有效范围内
     */
    fun clampStartIndex(maxIndex: Int) {
        startIndex = startIndex.coerceIn(0, maxIndex - visibleCount)
    }
    
    /**
     * 设置缩放级别
     * @param width 新的K线宽度
     * @param minWidth 最小宽度
     * @param maxWidth 最大宽度
     */
    fun setCandleWidth(width: Float, minWidth: Float = 2f, maxWidth: Float = 50f) {
        candleWidth = width.coerceIn(minWidth, maxWidth)
    }
    
    /**
     * 向左移动视口
     */
    fun scrollLeft(amount: Int) {
        startIndex -= amount
    }
    
    /**
     * 向右移动视口
     */
    fun scrollRight(amount: Int, maxIndex: Int) {
        startIndex += amount
        clampStartIndex(maxIndex)
    }
}
