package com.kampplus.hava.ui.forecast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.model.*
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.ErrorView
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

private val Ice = Color(0xFF78DEED)
private val Muted = Color(0xFFB1C5D9)

@Composable
fun ForecastRoute(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: ForecastViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ForecastScreen(viewModel.cityName, viewModel.subtitle, uiState, onBack, viewModel::onRetry, modifier)
}

@Composable
fun ForecastScreen(cityName: String, subtitle: String, uiState: LoadState<Forecast>, onBack: () -> Unit, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val code = (uiState as? LoadState.Success<Forecast>)?.data?.current?.weatherCode ?: 0
    Box(modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
        if (code >= 51) Color(0xFF304F63) else Color(0xFF193C61), Color(0xFF101D37), Color(0xFF071521)
    )))) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Brush.radialGradient(listOf(Color(0x227DDCF0), Color.Transparent), Offset(size.width * .8f, size.height * .18f), size.width * .8f), size.width * .8f, Offset(size.width * .8f, size.height * .18f))
            if (code >= 51) repeat(40) { i ->
                val x = ((i * 79) % 401) / 401f * size.width
                val y = ((i * 113) % 701) / 701f * size.height
                drawLine(Color.White.copy(alpha = .08f), Offset(x, y), Offset(x - 6, y + 22), 1.5f)
            }
        }
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Şehirlere dön", tint = Color.White) }
                Text("HAVA DURUMU", color = Muted, fontSize = 11.sp, letterSpacing = 2.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = onRetry) { Text("Yenile", color = Ice) }
            }
            when (uiState) {
                LoadState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Ice) }
                LoadState.Empty -> ErrorView(message = "Bu şehir için tahmin bulunamadı.", onRetry = onRetry)
                is LoadState.Error -> ErrorView(message = uiState.message, onRetry = onRetry)
                is LoadState.Success -> ForecastContent(cityName, subtitle, uiState.data)
            }
        }
    }
}

@Composable
private fun ForecastContent(cityName: String, subtitle: String, forecast: Forecast) {
    val current = forecast.current
    val condition = WeatherCondition.fromWeatherCode(current.weatherCode)
    // API timestamps are destination-local; comparing with the phone's timezone hides valid hours abroad.
    val upcoming = forecast.hourly.filter { !it.time.isBefore(current.observedAt.truncatedTo(ChronoUnit.HOURS)) }.take(24)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(cityName, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Light)
            if (subtitle.isNotBlank()) Text(subtitle, color = Muted, fontSize = 12.sp)
            BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val temperatureSize = minOf(104f, maxWidth.value / 3.5f / LocalDensity.current.fontScale)
                Text(degrees(current.temperatureC), color = Color.White, fontSize = temperatureSize.sp, fontWeight = FontWeight.ExtraLight, lineHeight = (temperatureSize * 1.1f).sp, maxLines = 1)
            }
            Text("${condition.emoji}  ${condition.label}", color = Color.White, fontSize = 18.sp)
            forecast.daily.firstOrNull()?.let { Text("En yüksek ${degrees(it.maxTemperatureC)}  ·  En düşük ${degrees(it.minTemperatureC)}", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp)) }
            Text("${current.observedAt.format(DateTimeFormatter.ofPattern("dd MMM · HH:mm", Locale.forLanguageTag("tr")))} yerel · Open-Meteo", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
        }
        GlassPanel {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Metric("Hissedilen", current.apparentTemperatureC?.let(::degrees) ?: "—", Modifier.weight(1f))
                Metric("Nem", current.humidityPercent?.let { "%$it" } ?: "—", Modifier.weight(1f))
                Metric("Rüzgâr", current.windSpeedKmh?.let { "${it.roundToInt()} km/sa" } ?: "—", Modifier.weight(1f))
            }
        }
        GlassPanel {
            PanelTitle("SAATLİK TAHMİN")
            Text("Saatler seçili şehrin yerel saatindedir.", color = Muted, fontSize = 11.sp)
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = .10f))
            if (upcoming.isEmpty()) Text("Saatlik tahmin henüz mevcut değil.", color = Muted, fontSize = 13.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                items(upcoming, key = { it.time.toString() }) { hour ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(hour.time.format(DateTimeFormatter.ofPattern("HH:mm")), color = Muted, fontSize = 12.sp)
                        Text(WeatherCondition.fromWeatherCode(hour.weatherCode).emoji, fontSize = 24.sp)
                        Text(degrees(hour.temperatureC), color = Color.White, fontSize = 20.sp)
                        Text(hour.precipitationProbability?.let { "%$it" } ?: "—", color = Ice, fontSize = 11.sp)
                    }
                }
            }
        }
        GlassPanel {
            PanelTitle("5 GÜNLÜK TAHMİN")
            if (forecast.daily.isEmpty()) Text("Günlük tahmin henüz mevcut değil.", color = Muted, fontSize = 13.sp)
            forecast.daily.take(5).forEachIndexed { index, day ->
                HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Color.White.copy(alpha = .09f))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(if (index == 0) "Bugün" else day.date.format(DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("tr"))).replaceFirstChar(Char::titlecase), color = Color.White, fontSize = 14.sp)
                        Text(day.precipitationProbability?.let { "Yağış %$it" } ?: "Yağış verisi yok", color = Ice, fontSize = 10.sp)
                    }
                    Text(WeatherCondition.fromWeatherCode(day.weatherCode).emoji, fontSize = 22.sp, modifier = Modifier.padding(end = 14.dp))
                    Text(degrees(day.minTemperatureC), color = Muted, fontSize = 16.sp, modifier = Modifier.width(39.dp))
                    Box(Modifier.width(30.dp).height(4.dp).background(Brush.horizontalGradient(listOf(Ice, Color(0xFFFFD28C))), RoundedCornerShape(4.dp)))
                    Text(degrees(day.maxTemperatureC), color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun GlassPanel(content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Color.White.copy(alpha = .065f), border = BorderStroke(1.dp, Color.White.copy(alpha = .10f)), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), content = content)
    }
}
@Composable private fun PanelTitle(text: String) { Text(text, color = Muted, fontSize = 11.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Medium) }
@Composable private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, color = Muted, fontSize = 11.sp)
        Text(value, color = Color.White, fontSize = 18.sp)
    }
}
private fun degrees(value: Double) = "${value.roundToInt()}°"
