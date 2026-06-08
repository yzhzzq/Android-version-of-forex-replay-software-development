package com.trading.review.data.buffer

/**
 * SOA (Structure Of Arrays) 高性能K线缓冲区
 * 避免创建大量对象，使用原始数组存储数据
 */
class CandleBuffer(size: Int) {
    
    val time = LongArray(size)
    val open = FloatArray(size)
    val high = FloatArray(size)
    val low = FloatArray(size)
    val close = FloatArray(size)
    val volume = FloatArray(size)
    
    var count = 0
    
    fun add(
        time: Long,
        open: Float,
        high: Float,
        low: Float,
        close: Float,
        volume: Float
    ) {
        if (count >= this.time.size) return
        
        this.time[count] = time
        this.open[count] = open
        this.high[count] = high
        this.low[count] = low
        this.close[count] = close
        this.volume[count] = volume
        count++
    }
    
    fun getTime(index: Int): Long = time[index]
    fun getOpen(index: Int): Float = open[index]
    fun getHigh(index: Int): Float = high[index]
    fun getLow(index: Int): Float = low[index]
    fun getClose(index: Int): Float = close[index]
    fun getVolume(index: Int): Float = volume[index]
    
    fun isEmpty(): Boolean = count == 0
    fun isNotEmpty(): Boolean = count > 0
    
    fun validIndex(index: Int): Boolean = index >= 0 && index < count
}
