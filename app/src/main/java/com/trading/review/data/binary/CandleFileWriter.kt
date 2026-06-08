package com.trading.review.data.binary

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * Binary文件格式常量
 */
object BinaryConstants {
    const val MAGIC = 0x54524144 // "TRAD"
    const val VERSION = 1
    const val HEADER_SIZE = 24 // MAGIC(4) + VERSION(4) + SYMBOL(8) + TIMEFRAME(4) + COUNT(4)
    const val CANDLE_SIZE = 28 // TIME(8) + OPEN(4) + HIGH(4) + LOW(4) + CLOSE(4) + VOLUME(4)
}

/**
 * Binary文件写入器
 * 将CandleBuffer数据写入二进制文件
 */
class CandleFileWriter {
    
    /**
     * 写入K线数据到二进制文件
     */
    fun write(file: File, buffer: CandleBuffer, symbol: String, timeframe: Int) {
        file.parentFile?.mkdirs()
        
        RandomAccessFile(file, "rw").use { raf ->
            val channel = raf.channel
            val bufferSize = BinaryConstants.HEADER_SIZE + buffer.count * BinaryConstants.CANDLE_SIZE
            val mappedBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, bufferSize.toLong())
            
            mappedBuffer.order(ByteOrder.LITTLE_ENDIAN)
            
            // 写入文件头
            mappedBuffer.putInt(BinaryConstants.MAGIC)
            mappedBuffer.putInt(BinaryConstants.VERSION)
            
            // 写入符号 (固定8字节)
            val symbolBytes = symbol.toByteArray().copyOf(8)
            mappedBuffer.put(symbolBytes)
            
            // 写入时间周期
            mappedBuffer.putInt(timeframe)
            
            // 写入K线数量
            mappedBuffer.putInt(buffer.count)
            
            // 写入K线数据
            for (i in 0 until buffer.count) {
                mappedBuffer.putLong(buffer.time[i])
                mappedBuffer.putFloat(buffer.open[i])
                mappedBuffer.putFloat(buffer.high[i])
                mappedBuffer.putFloat(buffer.low[i])
                mappedBuffer.putFloat(buffer.close[i])
                mappedBuffer.putFloat(buffer.volume[i])
            }
            
            mappedBuffer.force()
        }
    }
}
