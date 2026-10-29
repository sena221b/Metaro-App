package com.kampplus.hava.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val NavyDark = Color(0xFF0B132B)
val NavySurface = Color(0xFF1C2541)     // Kartlar ve Panel Arka Planı
val AviationCyan = Color(0xFF48CAE4)    // Ana Vurgu / Siyan Mavisi
val TacticalAmber = Color(0xFFFFB703)   // Uyarılar / VFR-IFR Bilgileri
val AlertRed = Color(0xFFE63946)        // Tehlike / Fırtına Uyarısı
val ClearGreen = Color(0xFF38B000)      // VFR / Uygun Uçuş Şartları
val TextPrimary = Color(0xFFEDF2F4)     // Yüksek Okunabilirlik Metin
val TextSecondary = Color(0xFF8D99AE)   // İkincil Etiketler

private val DarkColorScheme = darkColorScheme(
    primary = AviationCyan,
    secondary = TacticalAmber,
    tertiary = ClearGreen,
    background = NavyDark,
    surface = NavySurface,
    onPrimary = NavyDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = Color(0xFFB1C5D9),
    surfaceVariant = Color(0xFF203349),
    primaryContainer = Color(0xFF173E51),
    onPrimaryContainer = Color(0xFFACEFFD),
    outline = Color(0xFF496075),
    error = AlertRed
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0077B6),
    secondary = TacticalAmber,
    tertiary = ClearGreen,
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF212529),
    onSurface = Color(0xFF212529),
    error = AlertRed
)

@Composable
fun HavaTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
