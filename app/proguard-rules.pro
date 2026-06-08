# Add project specific ProGuard rules here.
-keep class com.trading.review.data.buffer.CandleBuffer { *; }
-keep class com.trading.review.data.binary.** { *; }
-keep class com.trading.review.chart.** { *; }
-keep class com.trading.review.replay.** { *; }
-keep class com.trading.review.indicators.** { *; }
-keep class com.trading.review.trading.** { *; }

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes Metadata

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
