package com.kampplus.hava.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BarBackground = Color(0xFF091B29)
private val Cyan = Color(0xFF55E6FF)
private val Muted = Color(0xFF91A9BA)

internal enum class MainTab { WEATHER, METAR, ZULU, MAP }

@Composable
internal fun MetaroBottomBar(selectedTab: MainTab, onTabSelected: (MainTab) -> Unit) {
    Surface(color = BarBackground, shadowElevation = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab("Hava", MainTab.WEATHER, selectedTab, "☼", Modifier.weight(1f), onTabSelected)
            BottomTab("METAR", MainTab.METAR, selectedTab, "≋", Modifier.weight(1f), onTabSelected)
            BottomTab("Zulu", MainTab.ZULU, selectedTab, "◷", Modifier.weight(1f), onTabSelected)
            BottomTab("Harita", MainTab.MAP, selectedTab, "⌖", Modifier.weight(1f), onTabSelected)
        }
    }
}

@Composable
private fun BottomTab(
    label: String,
    tab: MainTab,
    selectedTab: MainTab,
    icon: String,
    tabModifier: Modifier = Modifier,
    onTabSelected: (MainTab) -> Unit
) {
    val selected = tab == selectedTab
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val emphasized = selected || hovered
    val tint by animateColorAsState(if (emphasized) Cyan else Muted, label = "tabTint")
    val glow by animateFloatAsState(if (emphasized) 1f else 0f, label = "tabGlow")

    Column(
        modifier = tabModifier
            .height(62.dp)
            .clickable(interactionSource = interaction, indication = null) { onTabSelected(tab) }
            .drawBehind {
                if (glow > 0f) {
                    drawRoundRect(
                        color = Cyan.copy(alpha = 0.07f * glow),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx())
                    )
                }
            }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(width = 52.dp, height = 34.dp)) {
            if (selected) {
                CloudFlightMark(Modifier.size(50.dp, 32.dp))
            } else {
                Text(icon, color = tint, fontSize = 23.sp, modifier = Modifier.scale(if (hovered) 1.08f else 1f))
            }
        }
        Text(
            text = label,
            color = tint,
            fontSize = 11.sp,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.drawBehind {
                if (glow > 0f) {
                    drawLine(
                        color = Cyan.copy(alpha = 0.6f * glow),
                        start = Offset(0f, size.height + 1.dp.toPx()),
                        end = Offset(size.width, size.height + 1.dp.toPx()),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        )
    }
}

@Composable
private fun CloudFlightMark(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val cloud = Path().apply {
                moveTo(w * .15f, h * .70f)
                cubicTo(w * .02f, h * .68f, w * .04f, h * .43f, w * .20f, h * .42f)
                cubicTo(w * .22f, h * .16f, w * .48f, h * .10f, w * .58f, h * .34f)
                cubicTo(w * .78f, h * .24f, w * .91f, h * .42f, w * .84f, h * .58f)
                cubicTo(w * .96f, h * .66f, w * .86f, h * .78f, w * .72f, h * .76f)
                lineTo(w * .20f, h * .76f)
                cubicTo(w * .17f, h * .76f, w * .15f, h * .74f, w * .15f, h * .70f)
            }
            drawPath(cloud, Cyan.copy(alpha = .18f), style = Stroke(width = 1.5.dp.toPx()))
            for (i in 0..2) {
                val y = h * (0.38f + i * 0.12f)
                drawLine(
                    color = Cyan.copy(alpha = 0.8f - i * 0.16f),
                    start = Offset(w * .02f, y),
                    end = Offset(w * .24f, y),
                    strokeWidth = 1.1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
        Text("✈", color = Cyan, fontSize = 15.sp, modifier = Modifier.padding(start = 3.dp))
    }
}
