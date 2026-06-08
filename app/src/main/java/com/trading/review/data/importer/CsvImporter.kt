package com.trading.review.data.importer

import com.trading.review.data.buffer.CandleBuffer
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

/**
 * CSV文件导入器
 * 支持标准MT4/MT5 CSV格式: Time,Open,High,Low,Close,Volume
 */
class CsvImporter {
    
    /**
     * 从CSV文件导入K线数据
     * @param csvFile CSV文件
     * @param dateFormat 日期格式 (可选)
     */
    fun import(csvFile: File, bufferSize: Int = 100000): CandleBuffer {
        val buffer = CandleBuffer(bufferSize)
        
        BufferedReader(FileReader(csvFile)).use { reader ->
            var line: String?
            var isFirstLine = true
            
            while (reader.readLine().also { line = it } != null) {
                // 跳过标题行
                if (isFirstLine) {
                    isFirstLine = false
                    if (line?.contains("Time", ignoreCase = true) == true ||
                        line?.contains("Date", ignoreCase = true) == true) {
                        continue
                    }
                }
                
                val parts = line?.split(",", ";", "\t") ?: continue
                
                if (parts.size < 5) continue
                
                try {
                    val time = parseTime(parts[0].trim())
                    val open = parts[1].trim().toFloat()
                    val high = parts[2].trim().toFloat()
                    val low = parts[3].trim().toFloat()
                    val close = parts[4].trim().toFloat()
                    val volume = if (parts.size > 5) parts[5].trim().toFloat() else 0f
                    
                    buffer.add(time, open, high, low, close, volume)
                } catch (e: Exception) {
                    // 跳过无效行
                    continue
                }
            }
        }
        
        return buffer
    }
    
    /**
     * 解析时间字符串为时间戳
     * 支持多种常见格式
     */
    private fun parseTime(timeStr: String): Long {
        // 尝试解析常见格式
        val formats = listOf(
            "yyyy.MM.dd HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "dd.MM.yyyy HH:mm:ss",
            "MM/dd/yyyy HH:mm:ss"
        )
        
        for (format in formats) {
            try {
                val sdf = java.text.SimpleDateFormat(format, java.util.Locale.US)
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                return sdf.parse(timeStr)?.time ?: continue
            } catch (e: Exception) {
                continue
            }
        }
        
        // 如果都是数字，可能是Unix时间戳
        return timeStr.toLongOrNull() ?: System.currentTimeMillis()
    }
}
