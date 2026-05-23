# ProGuard rules for TradeYourPlan

# Keep data models - use keepclassmembers for more granular control
-keepclassmembers class com.tradeyourplan.data.model.** { *; }
-keepclassmembers class com.tradeyourplan.domain.model.** { *; }
-keepclassmembers class com.tradeyourplan.data.local.entity.** { *; }

# Keep Room entities
-keep @androidx.room.Entity class *
-keep class * implements androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep generic signatures for Gson and Room
-keepattributes Signature
-keepattributes InnerClass
-keepattributes EnclosingMethod

# Gson
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep class com.google.gson.reflect.TypeToken {
    <fields>;
}
-dontwarn com.google.gson.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
