# Keep Kotlinx Serialization models
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepattributes *Annotation*,InnerClasses,EnclosingMethod

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
