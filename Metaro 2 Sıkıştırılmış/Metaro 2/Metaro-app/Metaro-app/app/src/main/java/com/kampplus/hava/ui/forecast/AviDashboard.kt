package com.kampplus.hava.ui.forecast

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.model.*
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val AviationAccent = Color(0xFF71E8DB)
private val AviationMuted = Color(0xFF9FB3C6)

@Composable
fun AviationRoute(viewModel: AviationViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AviDashboard(state, viewModel::search)
}

@Composable
fun AviDashboard(state: AviationUiState, onSearchIcao: (String) -> Unit) {
    var input by rememberSaveable { mutableStateOf("LTFM") }
    var tafSelected by rememberSaveable { mutableStateOf(false) }
    var decoded by rememberSaveable { mutableStateOf(true) }
    val metar = remember(state.metar) { MetarDecoderEngine.decode(state.metar) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF122B3A), Color(0xFF07111F)))),
        contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("METARO / AVIATION", color = AviationAccent, fontSize = 11.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Text("Uçuş brifingi", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
            Text("METAR & TAF", color = AviationMuted, fontSize = 13.sp)
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = input, onValueChange = { input = it.uppercase(Locale.ROOT).filter(Char::isLetterOrDigit).take(4) },
                    modifier = Modifier.weight(1f), singleLine = true, label = { Text("ICAO / IATA") },
                    shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = AviationAccent, unfocusedBorderColor = AviationMuted,
                        focusedLabelColor = AviationAccent, unfocusedLabelColor = AviationMuted))
                Button(onClick = { onSearchIcao(input) }, enabled = input.length in 3..4 && !state.loading,
                    colors = ButtonDefaults.buttonColors(containerColor = AviationAccent, contentColor = Color(0xFF07111F)),
                    shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(16.dp)) { Text("Getir") }
            }
        }
        if (state.loading) item {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AviationAccent)
            Text("Meydan ve güncel raporlar alınıyor…", color = AviationMuted, modifier = Modifier.padding(top = 8.dp))
        }
        state.error?.let { error -> item { AviationCard { Text(error, color = Color(0xFFFFC4BD)) } } }
        if (state.airport == null && !state.loading && state.error == null) item {
            AviationCard {
                Text("Bir meydan seçiniz", color = Color.White, fontSize = 20.sp)
                Text("LTFM / IST, KJFK / JFK veya EGLL / LHR ile başlayın. İlk versiyonda yalnızca servisin yayımladığı raporlar gösterilmektedir.", color = AviationMuted)
            }
        }
        state.airport?.let { airport ->
            item {
                AviationCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(airport.icao, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        CategoryBadge(metar.flightCategory)
                    }
                    Text(airport.name, color = AviationMuted, fontSize = 12.sp)
                    if (metar.observationTime.isNotBlank()) Text("Gözlem · ${metar.observationTime}", color = AviationAccent, fontSize = 12.sp)
                    state.fetchedAt?.let { Text("Alındı · ${DateTimeFormatter.ofPattern("HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC).format(it)}", color = AviationMuted, fontSize = 11.sp) }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !tafSelected, onClick = { tafSelected = false }, label = { Text("METAR") })
                    FilterChip(selected = tafSelected, onClick = { tafSelected = true }, label = { Text("TAF") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !decoded, onClick = { decoded = false }, label = { Text("Coded") })
                    FilterChip(selected = decoded, onClick = { decoded = true }, label = { Text("Decoded") })
                }
            }
            val raw = if (tafSelected) state.taf else state.metar
            val error = if (tafSelected) state.tafError else state.metarError
            if (error != null || raw.isBlank()) item {
                AviationCard { Text(error ?: "Bu meydan için güncel ${if (tafSelected) "TAF" else "METAR"} yayımlanmamış.", color = AviationMuted) }
            } else if (!decoded) item {
                AviationCard {
                    Text(if (tafSelected) "HAM TAF" else "Ana METAR", color = AviationAccent, fontSize = 11.sp, letterSpacing = 2.sp)
                    SelectionContainer { Text(raw, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 15.sp, lineHeight = 25.sp) }
                }
            } else if (!tafSelected) item { AviationCard { DecodedConditions(metar, true) } }
            else {
                item { Text("Saatler UTC • DDHH/DDHH = gün+saat aralığı. Her grubun yalnızca bildirdiği alanlar gösterilir; boş alanlar ana tahminle birlikte okunur.", color = AviationMuted, fontSize = 12.sp) }
                TafDecoderEngine.decode(raw).forEach { period -> item {
                    AviationCard {
                        Text(period.title, color = AviationAccent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        DecodedConditions(period.conditions, false)
                        Text(period.raw, color = AviationMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                } }
            }
            item { Text("Kaynak: NOAA Aviation Weather Center. Güncellik raporun gözlem/yayım zamanına bağlıdır. Eğitim ve ön izleme içindir:)", color = AviationMuted, fontSize = 11.sp, lineHeight = 17.sp) }
        }
    }
}

@Composable
private fun AviationCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = Color(0xBB182C3D), border = BorderStroke(1.dp, Color(0x224DC7D6))) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun DecodedConditions(data: DecodedMetar, observation: Boolean) {
    MetricLine("Rüzgâr", data.wind.display)
    MetricLine("Görüş", data.visibilityKm)
    MetricLine("Bulut", data.clouds)
    data.ceilingFeet?.let { MetricLine("Tavan", "$it ft AGL") }
    MetricLine("Olay", data.weather)
    if (observation) {
        HorizontalDivider(color = Color(0x223E7C8F))
        MetricLine("Sıcaklık / çiğ", "${data.temperatureC?.let { "$it°C" } ?: "—"} / ${data.dewPointC?.let { "$it°C" } ?: "—"}")
        MetricLine("QNH", data.altimeterQnh)
    }
}

@Composable
private fun MetricLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(label, color = AviationMuted, fontSize = 13.sp, modifier = Modifier.width(84.dp))
        Text(value, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CategoryBadge(category: FlightCategory) {
    val color = when (category) { FlightCategory.VFR -> Color(0xFF71E8B1); FlightCategory.MVFR -> Color(0xFF83BAFF); FlightCategory.IFR -> Color(0xFFFF8E8E); FlightCategory.LIFR -> Color(0xFFD4A1FF); FlightCategory.UNKNOWN -> AviationMuted }
    Surface(color = color.copy(alpha = .15f), shape = RoundedCornerShape(50)) {
        Text(if (category == FlightCategory.UNKNOWN) "BELİRSİZ" else category.name, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
    }
}
