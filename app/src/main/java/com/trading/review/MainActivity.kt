package com.trading.review

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.trading.review.ui.ChartScreen
import java.io.File

/**
 * 主活动
 * 专业级交易复盘系统入口
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 示例：加载测试数据路径
                    val dataFile = File(filesDir, "market/EURUSD/M1/data.bin")
                    
                    ChartScreen(
                        dataFilePath = if (dataFile.exists()) dataFile.absolutePath else null
                    )
                }
            }
        }
    }
}
