# Release derlemesinde R8 kullanılmayan kodu siler ve isimleri kısaltır.
# Aşagıdaki kurallar, çalışma anında yansıma (reflection) ile erişilen şeyleri korur.

# --- Gson & Model Classes ---
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.kampplus.hava.model.** { *; }
-keep class com.kampplus.hava.data.** { *; }

# @SerializedName ile işaretlenmiş alanlar hiçbir koşulda yeniden adlandırılmamalı.
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
