# Proguard rules for Saathi
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}
-keep class com.saathi.model.** { *; }
-keep class com.saathi.data.entity.** { *; }
-keep class com.saathi.data.dao.** { *; }
-keep class com.saathi.data.AppDatabase { *; }
