# Add project specific ProGuard rules here.

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.stremio.glass.data.model.**$$serializer { *; }
-keepclassmembers class com.stremio.glass.data.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.stremio.glass.data.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt
-dontwarn dagger.hilt.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Ktor
-dontwarn io.ktor.**

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
