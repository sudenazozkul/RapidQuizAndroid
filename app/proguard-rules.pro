# kotlinx.serialization: DTO'ların serializer'larını koru
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class com.rapidquiz.data.api.dto.** {
    *** Companion;
}
-keepclasseswithmembers class com.rapidquiz.data.api.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}
