package com.trading.review.chart.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.trading.review.data.binary.CandleFileReader
import com.trading.review.chart.viewport.Viewport

/**
 * K线渲染器
 * 负责绘制K线图，仅绘制视口范围内的数据
 */
class CandleRenderer {
    
    private val bullPaint = Paint().apply {
        color = Color.parseColor("#26A69A") // 绿色阳线
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val bearPaint = Paint().apply {
        color = Color.parseColor("#EF5350") // 红色阴线
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val wickPaint = Paint().apply {
        strokeWidth = 1f
        isAntiAlias = true
    }
    
    private val gridPaint = Paint().apply {
        color = Color.parseColor("#33FFFFFF")
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
    
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 12f
        isAntiAlias = true
    }
    
    private val rectF = RectF()
    
    /**
     * 绘制K线图
     * @param canvas 画布
     * @param reader K线数据读取器
     * @param viewport 视口配置
     * @param width 图表宽度
     * @param height 图表高度
     */
    fun draw(
        canvas: Canvas,
        reader: CandleFileReader,
        viewport: Viewport,
        width: Int,
        height: Int
    ) {
        if (reader.count == 0) return
        
        // 计算可见范围
        val startIndex = viewport.startIndex.coerceIn(0, reader.count - 1)
        val endIndex = minOf(viewport.endIndex, reader.count)
        
        // 计算价格范围
        var minPrice = Float.MAX_VALUE
        var maxPrice = Float.MIN_VALUE
        
        for (i in startIndex until endIndex) {
            val low = reader.getLow(i)
            val high = reader.getHigh(i)
            if (low < minPrice) minPrice = low
            if (high > maxPrice) maxPrice = high
        }
        
        if (minPrice >= maxPrice) {
            minPrice = maxPrice * 0.99f
            maxPrice = maxPrice * 1.01f
        }
        
        val priceRange = maxPrice - minPrice
        val padding = height * 0.1f
        val drawHeight = height - padding * 2
        
        // 绘制网格
        drawGrid(canvas, width, height, minPrice, maxPrice, priceRange)
        
        // 绘制K线
        for (i in startIndex until endIndex) {
            val open = reader.getOpen(i)
            val high = reader.getHigh(i)
            val low = reader.getLow(i)
            val close = reader.getClose(i)
            
            val x = (i - startIndex) * viewport.candleWidth
            val candleWidth = viewport.candleWidth * 0.8f
            
            // 计算Y坐标
            val openY = priceToY(open, minPrice, maxPrice, drawHeight, padding, height)
            val closeY = priceToY(close, minPrice, maxPrice, drawHeight, padding, height)
            val highY = priceToY(high, minPrice, maxPrice, drawHeight, padding, height)
            val lowY = priceToY(low, minPrice, maxPrice, drawHeight, padding, height)
            
            // 绘制影线
            wickPaint.color = if (close >= open) Color.parseColor("#26A69A") else Color.parseColor("#EF5350")
            canvas.drawLine(x + viewport.candleWidth / 2, highY, x + viewport.candleWidth / 2, lowY, wickPaint)
            
            // 绘制实体
            val top = minOf(openY, closeY)
            val bottom = maxOf(openY, closeY)
            val bodyHeight = maxOf(bottom - top, 1f) // 至少1像素
            
            rectF.set(x, top, x + candleWidth, bottom)
            
            if (close >= open) {
                canvas.drawRect(rectF, bullPaint)
            } else {
                canvas.drawRect(rectF, bearPaint)
            }
        }
        
        // 绘制价格标签
        drawPriceLabels(canvas, width, height, minPrice, maxPrice, priceRange, drawHeight, padding)
    }
    
    private fun priceToY(
        price: Float,
        minPrice: Float,
        maxPrice: Float,
        drawHeight: Float,
        padding: Float,
        chartHeight: Int
    ): Float {
        val ratio = (price - minPrice) / (maxPrice - minPrice)
        return chartHeight - padding - ratio * drawHeight
    }
    
    private fun drawGrid(
        canvas: Canvas,
        width: Int,
        height: Int,
        minPrice: Float,
        maxPrice: Float,
        priceRange: Float
    ) {
        // 水平网格线 (价格)
        val gridCount = 5
        for (i in 0..gridCount) {
            val price = minPrice + priceRange * i / gridCount
            val y = priceToY(price, minPrice, maxPrice, (height * 0.9f), height * 0.05f, height)
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
        }
        
        // 垂直网格线 (时间)
        val verticalGridCount = 10
        for (i in 0..verticalGridCount) {
            val x = width.toFloat() * i / verticalGridCount
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
        }
    }
    
    private fun drawPriceLabels(
        canvas: Canvas,
        width: Int,
        height: Int,
        minPrice: Float,
        maxPrice: Float,
        priceRange: Float,
        drawHeight: Float,
        padding: Float
    ) {
        val gridCount = 5
        for (i in 0..gridCount) {
            val price = minPrice + priceRange * i / gridCount
            val y = priceToY(price, minPrice, maxPrice, drawHeight, padding, height)
            canvas.drawText(String.format("%.5f", price), width - 60f, y + 4f, textPaint)
        }
    }
}
