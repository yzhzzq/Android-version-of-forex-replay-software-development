package com.trading.review.replay

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 复盘引擎
 * 控制K线回放进度，支持播放/暂停/速度调节
 */
class ReplayEngine(
    initialReplayIndex: Int = 0,
    private val totalCandleCount: Int
) {
    
    private val _replayIndex = MutableStateFlow(initialReplayIndex)
    val replayIndex: StateFlow<Int> = _replayIndex.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _speed = MutableStateFlow(1f)
    val speed: StateFlow<Float> = _speed.asStateFlow()
    
    /**
     * 当前回放索引 (可见的最后一根K线)
     */
    var currentReplayIndex: Int
        get() = _replayIndex.value
        set(value) {
            _replayIndex.value = value.coerceIn(0, totalCandleCount - 1)
        }
    
    /**
     * 是否正在播放
     */
    var playing: Boolean
        get() = _isPlaying.value
        set(value) {
            _isPlaying.value = value
        }
    
    /**
     * 播放速度 (1x, 2x, 4x, 8x, etc.)
     */
    var speedMultiplier: Float
        get() = _speed.value
        set(value) {
            _speed.value = value.coerceAtLeast(0.5f)
        }
    
    /**
     * 获取指定延迟 (毫秒)
     */
    fun getDelayMillis(): Long {
        return when (speedMultiplier) {
            in 0f..0.5f -> 2000L
            in 0.5f..1f -> 1000L
            in 1f..2f -> 500L
            in 2f..4f -> 250L
            else -> 100L
        }
    }
    
    /**
     * 前进一根K线
     */
    fun next() {
        if (currentReplayIndex < totalCandleCount - 1) {
            currentReplayIndex++
        } else {
            playing = false
        }
    }
    
    /**
     * 后退一根K线
     */
    fun previous() {
        if (currentReplayIndex > 0) {
            currentReplayIndex--
        }
    }
    
    /**
     * 快进到末尾
     */
    fun fastForward() {
        currentReplayIndex = totalCandleCount - 1
        playing = false
    }
    
    /**
     * 重置到开始
     */
    fun reset() {
        currentReplayIndex = 0
        playing = false
    }
    
    /**
     * 切换播放状态
     */
    fun togglePlay() {
        playing = !playing
    }
    
    /**
     * 检查索引是否在可见范围内 (未来数据不可见)
     */
    fun isVisible(index: Int): Boolean {
        return index <= currentReplayIndex
    }
}
