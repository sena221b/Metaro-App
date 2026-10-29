package com.kampplus.hava.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos

@Composable
fun FlightTrackerDashboardCard(modifier: Modifier = Modifier) {
    var departureCode by remember { mutableStateOf("DUB") }
    var arrivalCode by remember { mutableStateOf("LHR") }

    val animationDuration = 30000
    val infiniteTransition = rememberInfiniteTransition(label = "flight_animation")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flight_progress"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0B132B), shape = RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFF48CAE4).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        // 1. Interactive Pilot Input Fields (Departure & Arrival ICAO/IATA)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = departureCode,
                onValueChange = { if (it.length <= 4) departureCode = it.uppercase() },
                label = { Text("Departure ICAO", color = Color(0xFF8D99AE), fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF48CAE4),
                    unfocusedBorderColor = Color(0xFF48CAE4).copy(alpha = 0.4f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1C2541).copy(alpha = 0.6f),
                    unfocusedContainerColor = Color(0xFF1C2541).copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = arrivalCode,
                onValueChange = { if (it.length <= 4) arrivalCode = it.uppercase() },
                label = { Text("Arrival ICAO", color = Color(0xFF8D99AE), fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF48CAE4),
                    unfocusedBorderColor = Color(0xFF48CAE4).copy(alpha = 0.4f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1C2541).copy(alpha = 0.6f),
                    unfocusedContainerColor = Color(0xFF1C2541).copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Main Content Row: Left Globe & Right Flight Briefing Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 2. Left Side: Globe Canvas & Animated Flight Path
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp)
                    .background(Color(0xFF0F1E29).copy(alpha = 0.8f), shape = RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0xFF48CAE4).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(12.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val center = Offset(width / 2f, height / 2f)
                    val radius = minOf(width, height) / 2f - 16f

                    // Atmosphere outer glow
                    drawCircle(
                        color = Color(0xFF48CAE4).copy(alpha = 0.12f),
                        radius = radius + 6f,
                        center = center
                    )

                    // Globe Body
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF1B3B6F), Color(0xFF071521)),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )

                    // Latitude & Longitude grid lines (3D globe effect)
                    val gridColor = Color(0xFF48CAE4).copy(alpha = 0.25f)
                    drawCircle(
                        color = Color(0xFF48CAE4).copy(alpha = 0.5f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )

                    // Parallels (Latitude arcs)
                    for (i in -2..2) {
                        if (i == 0) continue
                        val offsetY = center.y + (radius * 0.35f * i)
                        val arcWidth = radius * cos(Math.asin((i * 0.35f).toDouble())).toFloat()
                        drawArc(
                            color = gridColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(center.x - arcWidth, offsetY - radius * 0.15f),
                            size = Size(arcWidth * 2f, radius * 0.3f),
                            style = Stroke(width = 1f)
                        )
                    }

                    // Meridians (Longitude arcs)
                    for (i in -2..2) {
                        if (i == 0) continue
                        val offsetX = center.x + (radius * 0.35f * i)
                        val arcHeight = radius * cos(Math.asin((i * 0.35f).toDouble())).toFloat()
                        drawArc(
                            color = gridColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(offsetX - radius * 0.15f, center.y - arcHeight),
                            size = Size(radius * 0.3f, arcHeight * 2f),
                            style = Stroke(width = 1f)
                        )
                    }

                    // Flight Path (Bezier Curve connecting departure & arrival visual representation)
                    val startPoint = Offset(center.x - radius * 0.5f, center.y + radius * 0.2f)
                    val controlPoint = Offset(center.x, center.y - radius * 0.8f)
                    val endPoint = Offset(center.x + radius * 0.6f, center.y - radius * 0.1f)

                    val path = Path().apply {
                        moveTo(startPoint.x, startPoint.y)
                        quadraticTo(controlPoint.x, controlPoint.y, endPoint.x, endPoint.y)
                    }

                    // Draw route glow path
                    drawPath(
                        path = path,
                        color = Color(0xFF48CAE4).copy(alpha = 0.4f),
                        style = Stroke(width = 2.5f)
                    )

                    // Endpoints glowing dots
                    drawCircle(color = Color(0xFF00FFCC), radius = 4f, center = startPoint)
                    drawCircle(color = Color(0xFF55E6FF), radius = 6f, center = startPoint, style = Stroke(width = 1.5f))

                    drawCircle(color = Color(0xFF00FFCC), radius = 4f, center = endPoint)
                    drawCircle(color = Color(0xFF55E6FF), radius = 6f, center = endPoint, style = Stroke(width = 1.5f))

                    // Animate Airplane along Path using PathMeasure
                    val pathMeasure = PathMeasure().apply { setPath(path, false) }
                    val length = pathMeasure.length

                    if (length > 0f) {
                        val position = pathMeasure.getPosition(length * progress)
                        drawCircle(
                            color = Color.White,
                            radius = 5f,
                            center = position
                        )
                        drawCircle(
                            color = Color(0xFF55E6FF),
                            radius = 9f,
                            center = position,
                            style = Stroke(width = 2f)
                        )
                    }
                }

                // Bottom-left title
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                ) {
                    Text(
                        text = "Worldwide Routes",
                        color = Color(0xFF8D99AE),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }

            // Right Side: Flight Briefing Card & Live Zulu Badge
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 3. Top Right "Live Zulu" Badge
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0F1E29), shape = RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFF48CAE4).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF00FFCC).copy(alpha = pulseAlpha), shape = RoundedCornerShape(4.dp))
                            )
                            Text(
                                text = "LIVE ZULU",
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // 4. Right Side Card Structure (Flight Briefing Card)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F1E29).copy(alpha = 0.8f), shape = RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFF48CAE4).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Top Row: Flight Code & Status Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Flight",
                                    color = Color(0xFF8D99AE),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "UAE 643",
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF1C2541), shape = RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "On Time",
                                    color = Color(0xFF00FFCC),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Route Row: Departure -> Arrival with dynamic codes & times
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = departureCode,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "10:40 PM",
                                    color = Color(0xFF8D99AE),
                                    fontSize = 10.sp
                                )
                            }

                            // Center icon: glowing cyan circle with airplane
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFF48CAE4).copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFF48CAE4), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✈",
                                    color = Color(0xFF48CAE4),
                                    fontSize = 14.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = arrivalCode,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "08:00 PM",
                                    color = Color(0xFF8D99AE),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
