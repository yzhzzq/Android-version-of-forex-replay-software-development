package com.trading.review.chart.gesture

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.trading.review.chart.viewport.Viewport

/**
 * 手势处理器配置
 */
data class GestureConfig(
    val minCandleWidth: Float = 2f,
    val maxCandleWidth: Float = 50f,
    val dragSensitivity: Float = 1f,
    val zoomSensitivity: Float = 0.5f
)

/**
 * 图表手势系统
 * 支持缩放、拖动、惯性滑动
 */
class ChartGestureHandler(
    private val viewport: Viewport,
    private val config: GestureConfig = GestureConfig(),
    private val onViewportChanged: () -> Unit = {}
) {
    
    private var isDragging = false
    private var lastDragX = 0f
    
    /**
     * 创建 Compose  Modifier 用于手势处理
     */
    fun createModifier(): Modifier {
        return Modifier
            .pointerInput(viewport) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    // 处理缩放手势
                    if (zoom != 1f) {
                        val newWidth = viewport.candleWidth * zoom
                        viewport.setCandleWidth(
                            width = newWidth,
                            minWidth = config.minCandleWidth,
                            maxWidth = config.maxCandleWidth
                        )
                        onViewportChanged()
                    }
                }
            }
            .pointerInput(viewport) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        isDragging = true
                        lastDragX = it.x
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        // 处理拖动手势
                        val delta = (dragAmount * config.dragSensitivity / viewport.candleWidth).toInt()
                        
                        if (delta != 0) {
                            viewport.scrollLeft(delta)
                            onViewportChanged()
                        }
                        
                        lastDragX = change.position.x
                    },
                    onDragEnd = {
                        isDragging = false
                    }
                )
            }
    }
    
    /**
     * 将触摸X坐标转换为K线索引
     */
    fun xToIndex(touchX: Float): Int {
        return (touchX / viewport.candleWidth + viewport.startIndex).toInt()
    }
    
    /**
     * 将触摸Y坐标转换为价格
     */
    fun yToPrice(
        touchY: Float,
        chartHeight: Int,
        minPrice: Float,
        maxPrice: Float
    ): Float {
        val padding = chartHeight * 0.1f
        val drawHeight = chartHeight - padding * 2
        val ratio = (chartHeight - padding - touchY) / drawHeight
        return minPrice + ratio * (maxPrice - minPrice)
    }
    
    /**
     * 检查是否在拖动中
     */
    fun isDragging(): Boolean = isDragging
}
