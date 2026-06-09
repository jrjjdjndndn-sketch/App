-keep class ai.onnxruntime.** { *; }
-keep class com.aicompanion.pro.ai.** { *; }
-keep class com.aicompanion.pro.data.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }
-keep,includedescriptorclasses class com.aicompanion.pro.**$$serializer { *; }
-keepclassmembers class com.aicompanion.pro.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature
-dontwarn org.slf4j.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
