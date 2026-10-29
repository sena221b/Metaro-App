package com.kampplus.hava.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
private val base = Typography()

val Typography = base.copy(
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.Bold)
)
