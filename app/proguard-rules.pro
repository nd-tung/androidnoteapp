# Add project specific ProGuard rules here.
# You can find more details about ProGuard in the official documentation:
# https://www.guardsquare.com/manual/configuration/index.html

# If you are using shrinking, you need to include the following rules for Compose:
-keepclassmembers class androidx.compose.runtime.Composer {
    <methods>;
}

-keep class androidx.compose.runtime.internal.ComposableLambda { *; }

-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep - Jetpack Compose runtime saving state
-keepclassmembers class androidx.compose.runtime.saveable.SaverKt {
    <methods>;
}
