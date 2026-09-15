# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep data classes used for JSON backup/export (reflection-free, but keep names for readability)
-keepclassmembers class com.misgastos.app.data.** { *; }
-keepclassmembers class com.misgastos.app.domain.** { *; }
