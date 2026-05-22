# ProGuard rules for TradeYourPlan

# Keep data models
-keep class com.tradeyourplan.data.model.** { *; }
-keep class com.tradeyourplan.domain.model.** { *; }
-keep class com.tradeyourplan.data.local.entity.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
