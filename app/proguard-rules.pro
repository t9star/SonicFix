# Proguard rules for SonicFix

# Google Play Billing
-keep class com.android.billingclient.api.** { *; }

# kotlinx.serialization rules
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepattributes *Annotation*,InnerClasses,EnclosingMethod

# androidx.work / androidx.room
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
