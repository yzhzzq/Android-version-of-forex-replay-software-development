# 专业级交易复盘系统 (Trading Review System)

一个基于 Android Jetpack Compose 开发的高性能外汇交易复盘软件，采用 TradingView 架构设计。

## 核心特性

- ✅ **TradingView 风格 K 线图** - 流畅的图表绘制体验
- ✅ **Replay 复盘功能** - 支持播放/暂停/速度调节
- ✅ **模拟交易系统** - 完整的开仓/平仓/止损止盈逻辑
- ✅ **多周期支持** - M1/M5/M15/H1/H4 等多时间框架
- ✅ **指标系统** - EMA/SMA/RSI/MACD 预计算缓存
- ✅ **百万 K 线不卡** - MemoryMap + SOA 数据结构
- ✅ **安卓原生高性能** - Canvas 直接绘制，无多余对象创建

## 技术架构

```
app/
├── core/              # 核心工具类
├── data/              # 数据引擎
│   ├── buffer/        # SOA 数据结构 (CandleBuffer)
│   ├── binary/        # Binary 文件读写 (MappedByteBuffer)
│   ├── importer/      # CSV 导入器
│   └── repository/    # 数据仓库
├── chart/             # 图表引擎
│   ├── renderer/      # K 线渲染器
│   ├── viewport/      # 视口管理
│   └── gesture/       # 手势处理 (缩放/拖动)
├── replay/            # 复盘引擎
├── indicators/        # 指标系统 (预计算缓存)
├── trading/           # 交易系统
├── persistence/       # 持久化 (Room Database)
└── ui/                # Compose UI
```

## 核心技术亮点

### 1. SOA 数据结构 (Structure Of Arrays)
```kotlin
class CandleBuffer(size: Int) {
    val time = LongArray(size)
    val open = FloatArray(size)
    val high = FloatArray(size)
    val low = FloatArray(size)
    val close = FloatArray(size)
    val volume = FloatArray(size)
}
```

### 2. MemoryMap 文件读取
```kotlin
val mappedBuffer = RandomAccessFile(file, "r")
    .channel
    .map(FileChannel.MapMode.READ_ONLY, 0, file.length())
```

### 3. Viewport 视口系统
```kotlin
data class Viewport(
    var startIndex: Int,
    var visibleCount: Int,
    var candleWidth: Float
)
```

### 4. 指标预计算
- EMA/SMA/RSI/MACD 提前计算并缓存
- 支持保存为 .bin 文件直接读取

## 构建说明

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34
- Kotlin 1.9.20

### 构建步骤
1. 打开项目到 Android Studio
2. 同步 Gradle 依赖
3. 运行 `./gradlew assembleDebug`
4. 生成的 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`

## 数据格式

### Binary 文件格式
```
文件头 (24 bytes):
- MAGIC: 4 bytes (0x54524144)
- VERSION: 4 bytes
- SYMBOL: 8 bytes
- TIMEFRAME: 4 bytes
- COUNT: 4 bytes

K 线数据 (每根 28 bytes):
- TIME: 8 bytes (Long)
- OPEN: 4 bytes (Float)
- HIGH: 4 bytes (Float)
- LOW: 4 bytes (Float)
- CLOSE: 4 bytes (Float)
- VOLUME: 4 bytes (Float)
```

### 目录结构
```
/market/
├── EURUSD/
│   ├── M1/
│   │   ├── data.bin
│   │   ├── ema20.bin
│   │   └── ema60.bin
│   ├── M5/
│   │   └── data.bin
│   └── H1/
│       └── data.bin
```

## 开发路线图

### 第一阶段 ✅ (已完成)
- [x] CandleBuffer SOA 数据结构
- [x] CSV Importer
- [x] Binary Writer/Reader
- [x] Viewport 系统
- [x] K 线 Renderer

### 第二阶段 ✅ (已完成)
- [x] Replay Engine
- [x] Indicator Buffer
- [x] Trading Engine
- [x] Gesture Handler
- [x] Compose UI

### 第三阶段 (待开发)
- [ ] 十字光标系统
- [ ] 画线工具 (趋势线/斐波那契)
- [ ] 多周期联动
- [ ] 回测统计报告
- [ ] 更多技术指标

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request!
