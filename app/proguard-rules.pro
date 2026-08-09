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
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Gson uses reflection to (de)serialize these DTOs; keep field names intact.
-keep class com.ai.studyassistant.data.api.** { *; }
-keepclassmembers,allowobfuscation class com.ai.studyassistant.data.api.** {
    <fields>;
}

# data/model classes are also deserialized by Gson (e.g. LocalQuestionBank reads
# assets/questions.json into Question via TypeToken<Map<String, List<Question>>>).
# Without this, R8 renames/removes fields and Gson silently leaves them at their
# JVM defaults (null/0), which crashes the app the first time a quiz is opened.
-keep class com.ai.studyassistant.data.model.** { *; }
-keepclassmembers,allowobfuscation class com.ai.studyassistant.data.model.** {
    <fields>;
}

# Gson generic signatures / TypeToken support
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken