-keep class com.hunnychiko.baekbunuil.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Firebase
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Firebase Realtime DB — data class 직렬화 보호
-keepclassmembers class com.hunnychiko.baekbunuil.data.model.** {
    <init>();
    <fields>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose
-dontwarn androidx.compose.**

# Google Play Core
-keep class com.google.android.play.core.** { *; }
