# Add project specific ProGuard rules here.
# Aggressive optimization for minimal app size

# Remove line numbers for smaller size
-renamesourcefileattribute SourceFile
-repackageclasses ''

# Keep only what's necessary
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,*Annotation*,EnclosingMethod

# Keep Moshi
-keep class **JsonAdapter { *; }
-keep class com.squareup.moshi.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

## Keep KuroApi and Sapi
#-keep class com.thunder.kuroapi.** { *; }
#-keep class com.thunder.utils.** { *; }
#
## Keep cloneutils
#-keep class com.thunder.utils.cloneutils { *; }

# Remove unused code aggressively
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimize
-optimizationpasses 5
-dontpreverify
-verbose

# Remove debug code
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

## Keep SplashScreen API
#-keep class androidx.core.splashscreen.** { *; }
#
## Keep Compose runtime
#-keep class androidx.compose.runtime.** { *; }
#-keep class androidx.compose.ui.** { *; }