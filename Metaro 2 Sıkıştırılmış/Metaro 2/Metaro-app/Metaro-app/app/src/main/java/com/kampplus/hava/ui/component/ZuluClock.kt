package com.kampplus.hava.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.*

private val ZuluCyan = Color(0xFF78E6DB)
private val ZuluMuted = Color(0xFF91ABBD)
private val ZuluTimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US)
private val HubTimeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

@Composable
private fun rememberClock(): Instant {
    var now by remember { mutableStateOf(Instant.now()) }
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(owner) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) { now = Instant.now(); delay(1000) }
        }
    }
    return now
}

@Composable
fun ZuluClock(modifier: Modifier = Modifier) {
    val now = rememberClock()
    Surface(modifier, color = Color(0xFF112D3B), shape = RoundedCornerShape(12.dp)) {
        Text("${ZuluTimeFormat.format(now.atOffset(ZoneOffset.UTC))} Z", color = ZuluCyan,
            fontFamily = FontFamily.Monospace, fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
    }
}

@Composable
fun FullZuluDashboardScreen(modifier: Modifier = Modifier) {
    val now = rememberClock()
    val utc = now.atOffset(ZoneOffset.UTC)
    Column(
        modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF102B3C), Color(0xFF06131F))))
            .verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("DÜNYA SAATİ", color = ZuluCyan, fontSize = 11.sp, letterSpacing = 2.sp)
                Text("Tek gökyüzü. Tek zaman.", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Medium)
            }
            Text("UTC ±0", color = ZuluMuted, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(utc.format(DateTimeFormatter.ofPattern("dd MMMM yyyy · EEEE", Locale.forLanguageTag("tr"))), color = ZuluMuted, fontSize = 12.sp)
            Text(ZuluTimeFormat.format(utc), color = Color.White, fontFamily = FontFamily.Monospace,
                fontSize = 44.sp, fontWeight = FontWeight.Light, letterSpacing = (-1).sp)
            Text("ZULU SAATİ", color = ZuluCyan, fontSize = 10.sp, letterSpacing = 2.sp)
        }
        OrbitalGlobe(Modifier.fillMaxWidth().aspectRatio(1.26f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("KÜRESEL MERKEZLER", color = ZuluMuted, fontSize = 10.sp, letterSpacing = 1.5.sp)
            Text("Yerel saatler", color = ZuluCyan, fontSize = 11.sp)
        }
        val hubs = listOf(
            Triple("LON", "Londra", "Europe/London"), Triple("IST", "İstanbul", "Europe/Istanbul"),
            Triple("JFK", "New York", "America/New_York"), Triple("HND", "Tokyo", "Asia/Tokyo"),
            Triple("DXB", "Dubai", "Asia/Dubai"), Triple("SYD", "Sidney", "Australia/Sydney")
        )
        hubs.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { (code, city, zone) ->
                    val local = now.atZone(ZoneId.of(zone))
                    val offset = local.offset.id.let { if (it == "Z") "+00:00" else it }
                    Surface(Modifier.weight(1f), color = Color.White.copy(alpha = .045f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = .08f)), shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(code, color = ZuluCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("UTC$offset", color = ZuluMuted, fontSize = 10.sp)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(city, color = Color.White, fontSize = 12.sp)
                                Text(HubTimeFormat.format(local), color = Color.White, fontSize = 21.sp, fontFamily = FontFamily.Monospace)
                            }
                            Text(local.format(DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag("tr"))), color = ZuluMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        Text("Saatler cihaz saatinden hesaplanır; yaz saati farkları otomatik uygulanır.", color = ZuluMuted, fontSize = 11.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun OrbitalGlobe(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "world")
    val rotation by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(120000, easing = LinearEasing)), label = "earthRotation")
    val orbit by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(18000, easing = LinearEasing)), label = "airplaneOrbit")
    Canvas(modifier.semantics { contentDescription = "Dönen yuvarlak dünya ve yörüngesinde uçak animasyonu" }) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) * .40f
        drawCircle(Brush.radialGradient(listOf(ZuluCyan.copy(alpha = .12f), Color.Transparent), center, radius * 1.25f), radius * 1.25f, center)
        drawCircle(Brush.radialGradient(listOf(Color(0xFF24566A), Color(0xFF081B2D)), center - Offset(radius * .35f, radius * .4f), radius * 1.7f), radius, center)
        fun project(lat: Double, lon: Double): Pair<Offset, Boolean> {
            val phi = Math.toRadians(lat)
            val theta = Math.toRadians(lon + rotation)
            return Offset(center.x + radius * (cos(phi) * sin(theta)).toFloat(), center.y - radius * sin(phi).toFloat()) to (cos(phi) * cos(theta) >= 0)
        }
        fun line(points: List<Pair<Double, Double>>) {
            val path = Path(); var active = false
            points.forEach { (lat, lon) ->
                val (p, visible) = project(lat, lon)
                if (visible) { if (!active) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y) }
                active = visible
            }
            drawPath(path, ZuluCyan.copy(alpha = .14f), style = Stroke(.6.dp.toPx()))
        }
        for (lat in -60..60 step 30) line((-180..180 step 3).map { lat.toDouble() to it.toDouble() })
        for (lon in -180..150 step 30) line((-90..90 step 3).map { it.toDouble() to lon.toDouble() })
        GlobeLandDots.forEach { (lat, lon) ->
            val (point, visible) = project(lat, lon)
            if (visible) drawCircle(ZuluCyan.copy(alpha = .65f), 1.15.dp.toPx(), point)
        }
        drawCircle(ZuluCyan.copy(alpha = .5f), radius, center, style = Stroke(.8.dp.toPx()))
        val orbitRadius = radius * 1.17f
        drawOval(ZuluCyan.copy(alpha = .18f), Offset(center.x - orbitRadius, center.y - radius * .44f),
            androidx.compose.ui.geometry.Size(orbitRadius * 2, radius * .88f), style = Stroke(1.dp.toPx()))
        val angle = Math.toRadians(orbit.toDouble())
        val plane = Offset(center.x + orbitRadius * cos(angle).toFloat(), center.y + radius * .44f * sin(angle).toFloat())
        val heading = Math.toDegrees(atan2(.44 * radius * cos(angle), -orbitRadius * sin(angle))).toFloat() + 90
        drawCircle(ZuluCyan.copy(alpha = .10f), 17.dp.toPx(), plane)
        rotate(heading, plane) {
            val unit = 1.dp.toPx()
            val path = Path().apply {
                moveTo(plane.x, plane.y - 12 * unit)
                lineTo(plane.x + 2 * unit, plane.y - 2 * unit)
                lineTo(plane.x + 10 * unit, plane.y + 4 * unit)
                lineTo(plane.x + 10 * unit, plane.y + 6 * unit)
                lineTo(plane.x + 2 * unit, plane.y + 3 * unit)
                lineTo(plane.x + 2 * unit, plane.y + 9 * unit)
                lineTo(plane.x + 5 * unit, plane.y + 11 * unit)
                lineTo(plane.x - 5 * unit, plane.y + 11 * unit)
                lineTo(plane.x - 2 * unit, plane.y + 9 * unit)
                lineTo(plane.x - 2 * unit, plane.y + 3 * unit)
                lineTo(plane.x - 10 * unit, plane.y + 6 * unit)
                lineTo(plane.x - 10 * unit, plane.y + 4 * unit)
                lineTo(plane.x - 2 * unit, plane.y - 2 * unit)
                close()
            }
            drawPath(path, Color.White)
        }
    }
}

private val GlobeLandDots: List<Pair<Double, Double>> = run {
    val polygons = listOf(
        listOf(-168.0 to 70.0, -130.0 to 72.0, -105.0 to 80.0, -52.0 to 48.0, -82.0 to 25.0, -96.0 to 16.0, -116.0 to 30.0, -130.0 to 53.0),
        listOf(-81.0 to 12.0, -62.0 to 9.0, -35.0 to -5.0, -46.0 to -25.0, -68.0 to -55.0, -76.0 to -25.0),
        listOf(-17.0 to 35.0, 10.0 to 37.0, 33.0 to 31.0, 50.0 to 12.0, 33.0 to -30.0, 18.0 to -35.0, 10.0 to -5.0, -17.0 to 15.0),
        listOf(-10.0 to 36.0, -10.0 to 58.0, 30.0 to 72.0, 90.0 to 77.0, 176.0 to 62.0, 140.0 to 35.0, 120.0 to 5.0, 100.0 to 0.0, 80.0 to 22.0, 72.0 to 8.0, 55.0 to 25.0, 36.0 to 35.0),
        listOf(113.0 to -22.0, 130.0 to -12.0, 143.0 to -11.0, 154.0 to -29.0, 146.0 to -39.0, 116.0 to -35.0),
        listOf(-54.0 to 59.0, -22.0 to 70.0, -30.0 to 82.0, -60.0 to 82.0)
    )
    buildList {
        for (lat in -56..80 step 3) for (lon in -177..177 step 3) {
            if (polygons.any { polygon ->
                var inside = false; var previous = polygon.last()
                for (point in polygon) {
                    if ((point.second > lat) != (previous.second > lat) &&
                        lon < (previous.first - point.first) * (lat - point.second) / (previous.second - point.second) + point.first) inside = !inside
                    previous = point
                }
                inside
            }) add(lat.toDouble() to lon.toDouble())
        }
    }
}
