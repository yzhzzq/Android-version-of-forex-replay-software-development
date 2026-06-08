package com.trading.review.ui

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.trading.review.chart.gesture.ChartGestureHandler
import com.trading.review.chart.renderer.CandleRenderer
import com.trading.review.chart.viewport.Viewport
import com.trading.review.data.binary.CandleFileReader
import com.trading.review.replay.ReplayEngine
import kotlinx.coroutines.launch
import java.io.File

/**
 * 图表状态数据类
 * 仅包含必要的状态字段，避免大数据对象
 */
data class ChartState(
    val startIndex: Int = 0,
    val candleWidth: Float = 10f,
    val replayIndex: Int = 0,
    val isPlaying: Boolean = false
)

/**
 * K线图表画布组件
 * 使用 Canvas 进行高性能绘制
 */
@Composable
fun ChartCanvas(
    reader: CandleFileReader?,
    viewport: Viewport,
    replayEngine: ReplayEngine?,
    modifier: Modifier = Modifier
) {
    val renderer = remember { CandleRenderer() }
    val gestureHandler = remember(viewport) {
        ChartGestureHandler(viewport) {
            // Viewport changed
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        if (reader != null) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .then(gestureHandler.createModifier())
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                // Handle tap for crosshair
                            }
                        )
                    }
            ) {
                val width = size.width.toInt()
                val height = size.height.toInt()
                
                renderer.draw(
                    canvas = this.drawContext.canvas,
                    reader = reader,
                    viewport = viewport,
                    width = width,
                    height = height
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Data Loaded",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

/**
 * 复盘控制栏组件
 */
@Composable
fun ReplayControls(
    replayEngine: ReplayEngine,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onReset: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val isPlaying by replayEngine.isPlaying.collectAsState()
    val speed by replayEngine.speed.collectAsState()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                contentDescription = "Previous",
                tint = Color.White
            )
        }
        
        Button(
            onClick = onPlayPause,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlaying) Color.Red else Color.Green
            )
        ) {
            Text(if (isPlaying) "Pause" else "Play")
        }
        
        IconButton(onClick = onNext) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ArrowForward,
                contentDescription = "Next",
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Speed selector
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Speed:", color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            
            listOf(0.5f, 1f, 2f, 4f, 8f).forEach { s ->
                FilterChip(
                    selected = speed == s,
                    onClick = { onSpeedChange(s) },
                    label = { Text("${s}x") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.Blue
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("Reset")
        }
    }
}

/**
 * 交易面板组件
 */
@Composable
fun TradePanel(
    currentPrice: Float,
    onBuy: () -> Unit,
    onSell: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = String.format("Current Price: %.5f", currentPrice),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBuy,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                ) {
                    Text("BUY", color = Color.White)
                }
                
                Button(
                    onClick = onSell,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("SELL", color = Color.White)
                }
            }
        }
    }
}

/**
 * 主图表屏幕
 */
@Composable
fun ChartScreen(
    dataFilePath: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var reader by remember { mutableStateOf<CandleFileReader?>(null) }
    var viewport by remember { mutableStateOf(Viewport()) }
    var replayEngine by remember { mutableStateOf<ReplayEngine?>(null) }
    var chartState by remember { mutableStateOf(ChartState()) }
    
    LaunchedEffect(dataFilePath) {
        dataFilePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                reader = CandleFileReader(file)
                replayEngine = ReplayEngine(
                    initialReplayIndex = reader!!.count - 1,
                    totalCandleCount = reader!!.count
                )
                
                // Update viewport max index
                viewport.clampStartIndex(reader!!.count)
                
                // Update chart state
                chartState = chartState.copy(
                    replayIndex = replayEngine!!.currentReplayIndex
                )
            }
        }
    }
    
    // Auto-play coroutine
    LaunchedEffect(replayEngine) {
        replayEngine?.let { engine ->
            snapshotFlow { engine.playing }.collect { isPlaying ->
                if (isPlaying) {
                    while (engine.playing && engine.currentReplayIndex < engine.replayIndex.value) {
                        engine.next()
                        kotlinx.coroutines.delay(engine.getDelayMillis())
                    }
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Chart canvas
        ChartCanvas(
            reader = reader,
            viewport = viewport,
            replayEngine = replayEngine,
            modifier = Modifier.weight(1f)
        )
        
        // Replay controls
        if (replayEngine != null) {
            ReplayControls(
                replayEngine = replayEngine!!,
                onPlayPause = { replayEngine!!.togglePlay() },
                onNext = { replayEngine!!.next() },
                onPrevious = { replayEngine!!.previous() },
                onReset = { replayEngine!!.reset() },
                onSpeedChange = { speed -> replayEngine!!.speedMultiplier = speed }
            )
        }
        
        // Trade panel
        val currentPrice = reader?.let {
            if (replayEngine != null) {
                it.getClose(replayEngine!!.currentReplayIndex)
            } else {
                it.getClose(it.count - 1)
            }
        } ?: 0f
        
        TradePanel(
            currentPrice = currentPrice,
            onBuy = { /* Handle buy */ },
            onSell = { /* Handle sell */ }
        )
    }
}

/**
 * 加载示例数据并启动应用
 */
object TradingApp {
    fun loadData(context: Context, symbol: String, timeframe: String): File {
        val dataDir = File(context.filesDir, "market/$symbol/$timeframe")
        dataDir.mkdirs()
        return File(dataDir, "data.bin")
    }
}
