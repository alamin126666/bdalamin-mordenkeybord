# ModernKey Proguard Rules

# Keep IME service
-keep class com.modernkey.keyboard.ime.** { *; }

# Keep Room entities and DAOs
-keep class com.modernkey.keyboard.clipboard.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }

# Keep data classes
-keepclassmembers class com.modernkey.keyboard.** {
    public <init>(...);
}

# Keep enum classes
-keepclassmembers enum com.modernkey.keyboard.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# AndroidX
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# General Android
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
