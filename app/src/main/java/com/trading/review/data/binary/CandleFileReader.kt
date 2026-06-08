package com.trading.review.data.binary

import android.util.Log
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * Binary文件读取器
 * 使用MappedByteBuffer实现内存映射，支持大数据量高效读取
 */
class CandleFileReader(private val file: File) {
    
    private val mappedBuffer = RandomAccessFile(file, "r").use { raf ->
        raf.channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
    }.apply {
        order(ByteOrder.LITTLE_ENDIAN)
    }
    
    private val headerData: HeaderData by lazy {
        readHeader()
    }
    
    data class HeaderData(
        val magic: Int,
        val version: Int,
        val symbol: String,
        val timeframe: Int,
        val count: Int
    )
    
    init {
        Log.d(TAG, "Loaded ${file.name}: ${headerData.count} candles")
    }
    
    private fun readHeader(): HeaderData {
        mappedBuffer.position(0)
        
        val magic = mappedBuffer.int
        val version = mappedBuffer.int
        
        val symbolBytes = ByteArray(8)
        mappedBuffer.get(symbolBytes)
        val symbol = String(symbolBytes).trimEnd('\u0000')
        
        val timeframe = mappedBuffer.int
        val count = mappedBuffer.int
        
        return HeaderData(magic, version, symbol, timeframe, count)
    }
    
    val symbol: String get() = headerData.symbol
    val timeframe: Int get() = headerData.timeframe
    val count: Int get() = headerData.count
    
    /**
     * 获取指定索引的K线时间
     */
    fun getTime(index: Int): Long {
        require(index >= 0 && index < count) { "Index out of bounds: $index" }
        val offset = BinaryConstants.HEADER_SIZE + index * BinaryConstants.CANDLE_SIZE
        mappedBuffer.position(offset)
        return mappedBuffer.long
    }
    
    /**
     * 获取指定索引的开盘价
     */
    fun getOpen(index: Int): Float {
        require(index >= 0 && index < count) { "Index out of bounds: $index" }
        val offset = BinaryConstants.HEADER_SIZE + index * BinaryConstants.CANDLE_SIZE + 8
        mappedBuffer.position(offset)
        return mappedBuffer.float
    }
    
    /**
     * 获取指定索引的最高价
     */
    fun getHigh(index: Int): Float {
        require(index >= 0 && index < count) { "Index out of bounds: $index" }
        val offset = BinaryConstants.HEADER_SIZE + index * BinaryConstants.CANDLE_SIZE + 12
        mappedBuffer.position(offset)
        return mappedBuffer.float
    }
    
    /**
     * 获取指定索引的最低价
     */
    fun getLow(index: Int): Float {
        require(index >= 0 && index < count) { "Index out of bounds: $index" }
        val offset = BinaryConstants.HEADER_SIZE + index * BinaryConstants.CANDLE_SIZE + 16
        mappedBuffer.position(offset)
        return mappedBuffer.float
    }
    
    /**
     * 获取指定索引的收盘价
     */
    fun getClose(index: Int): Float {
        require(index >= 0 && index < count) { "Index out of bounds: $index" }
        val offset = BinaryConstants.HEADER_SIZE + index * BinaryConstants.CANDLE_SIZE + 20
        mappedBuffer.position(offset)
        return mappedBuffer.float
    }
    
    /**
     * 获取指定索引的成交量
     */
    fun getVolume(index: Int): Float {
        require(index >= 0 && index < count) { "Index out of bounds: $index" }
        val offset = BinaryConstants.HEADER_SIZE + index * BinaryConstants.CANDLE_SIZE + 24
        mappedBuffer.position(offset)
        return mappedBuffer.float
    }
    
    /**
     * 批量读取K线数据到缓冲区 (高性能)
     */
    fun readRange(startIndex: Int, endIndex: Int, buffer: com.trading.review.data.buffer.CandleBuffer) {
        require(startIndex >= 0 && endIndex <= count && startIndex < endIndex) {
            "Invalid range: [$startIndex, $endIndex), count=$count"
        }
        
        val readCount = minOf(endIndex - startIndex, buffer.time.size)
        
        for (i in 0 until readCount) {
            val index = startIndex + i
            val offset = BinaryConstants.HEADER_SIZE + index * BinaryConstants.CANDLE_SIZE
            
            mappedBuffer.position(offset)
            buffer.time[i] = mappedBuffer.long
            buffer.open[i] = mappedBuffer.float
            buffer.high[i] = mappedBuffer.float
            buffer.low[i] = mappedBuffer.float
            buffer.close[i] = mappedBuffer.float
            buffer.volume[i] = mappedBuffer.float
        }
        
        buffer.count = readCount
    }
    
    companion object {
        private const val TAG = "CandleFileReader"
    }
}
