# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep the MainActivity class and its methods
-keep class com.pinehook.plus.MainActivity {
    public boolean argMethod(boolean);
    public int anotherMethod(int);
    public int patternMethod(int);
    public int patternMethodField();
    public boolean yetAnotherMethod();
    public java.lang.String beforeOnlyMethod(java.lang.String);
    public java.lang.String afterOnlyMethod(java.lang.String);
}
-keep class com.pinehook.plus.ConstructorClass {
    public <init>(boolean, java.lang.String, boolean, long, long, java.lang.String, java.lang.String, java.lang.String);
}

# Keep the Hook class and its methods
-keep class com.pinehook.plus.Hook {
    public static void doHook(java.util.Map);
}

# Keep the NativeLibLoader class and its methods
-keep class com.pinehook.plus.NativeLibLoader {
    public static void loadNativeLib(android.content.Context, java.lang.String);
    public static java.util.Map loadConfig(android.content.Context);
}

# Keep the JsonParser class and its methods
-keep class com.pinehook.plus.JsonParser {
    public static java.util.Map parseConfig(org.json.JSONObject);
}
