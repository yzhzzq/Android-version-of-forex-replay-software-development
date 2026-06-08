package com.trading.review.indicators

/**
 * 指标缓冲区
 * 预计算并缓存技术指标数据
 */
class IndicatorBuffer(size: Int) {
    
    val ema20 = FloatArray(size)
    val ema60 = FloatArray(size)
    val ma200 = FloatArray(size)
    val rsi14 = FloatArray(size)
    val macdLine = FloatArray(size)
    val signalLine = FloatArray(size)
    val histogram = FloatArray(size)
    
    var count = 0
    
    fun isEmpty(): Boolean = count == 0
    fun isNotEmpty(): Boolean = count > 0
}

/**
 * 指标计算器
 * 提供各类技术指标的计算方法
 */
object IndicatorCalculator {
    
    /**
     * 计算指数移动平均线 (EMA)
     * EMA_t = P_t * k + EMA_{t-1} * (1 - k)
     * k = 2 / (n + 1)
     */
    fun calculateEma(
        closes: FloatArray,
        count: Int,
        period: Int,
        output: FloatArray
    ) {
        if (count < period) return
        
        val k = 2.0f / (period + 1)
        
        // 先计算简单移动平均作为初始值
        var sum = 0f
        for (i in 0 until period) {
            sum += closes[i]
        }
        output[period - 1] = sum / period
        
        // 计算EMA
        for (i in period until count) {
            output[i] = closes[i] * k + output[i - 1] * (1 - k)
        }
    }
    
    /**
     * 计算简单移动平均线 (SMA)
     */
    fun calculateSma(
        closes: FloatArray,
        count: Int,
        period: Int,
        output: FloatArray
    ) {
        if (count < period) return
        
        var sum = 0f
        for (i in 0 until period) {
            sum += closes[i]
        }
        output[period - 1] = sum / period
        
        for (i in period until count) {
            sum = sum - closes[i - period] + closes[i]
            output[i] = sum / period
        }
    }
    
    /**
     * 计算相对强弱指数 (RSI)
     */
    fun calculateRsi(
        closes: FloatArray,
        count: Int,
        period: Int = 14,
        output: FloatArray
    ) {
        if (count < period + 1) return
        
        var avgGain = 0f
        var avgLoss = 0f
        
        // 计算初始平均值
        for (i in 1..period) {
            val change = closes[i] - closes[i - 1]
            if (change > 0) {
                avgGain += change
            } else {
                avgLoss -= change
            }
        }
        avgGain /= period
        avgLoss /= period
        
        output[period] = if (avgLoss == 0f) 100f else 100f - 100f / (1f + avgGain / avgLoss)
        
        // 计算后续RSI
        for (i in period + 1 until count) {
            val change = closes[i] - closes[i - 1]
            val gain = if (change > 0) change else 0f
            val loss = if (change < 0) -change else 0f
            
            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period
            
            output[i] = if (avgLoss == 0f) 100f else 100f - 100f / (1f + avgGain / avgLoss)
        }
    }
    
    /**
     * 计算MACD
     * MACD Line = EMA12 - EMA26
     * Signal Line = EMA9 of MACD Line
     * Histogram = MACD Line - Signal Line
     */
    fun calculateMacd(
        closes: FloatArray,
        count: Int,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        signalPeriod: Int = 9,
        macdLine: FloatArray,
        signalLine: FloatArray,
        histogram: FloatArray
    ) {
        val emaFast = FloatArray(count)
        val emaSlow = FloatArray(count)
        
        calculateEma(closes, count, fastPeriod, emaFast)
        calculateEma(closes, count, slowPeriod, emaSlow)
        
        // 计算MACD Line
        val startIdx = maxOf(fastPeriod, slowPeriod)
        for (i in startIdx until count) {
            macdLine[i] = emaFast[i] - emaSlow[i]
        }
        
        // 计算Signal Line
        calculateEma(macdLine, count, signalPeriod, signalLine)
        
        // 计算Histogram
        for (i in startIdx until count) {
            histogram[i] = macdLine[i] - signalLine[i]
        }
    }
    
    /**
     * 计算完整指标集
     */
    fun calculateAll(
        closes: FloatArray,
        count: Int,
        buffer: IndicatorBuffer
    ) {
        buffer.count = count
        
        calculateEma(closes, count, 20, buffer.ema20)
        calculateEma(closes, count, 60, buffer.ema60)
        calculateSma(closes, count, 200, buffer.ma200)
        calculateRsi(closes, count, 14, buffer.rsi14)
        calculateMacd(
            closes = closes,
            count = count,
            macdLine = buffer.macdLine,
            signalLine = buffer.signalLine,
            histogram = buffer.histogram
        )
    }
}
