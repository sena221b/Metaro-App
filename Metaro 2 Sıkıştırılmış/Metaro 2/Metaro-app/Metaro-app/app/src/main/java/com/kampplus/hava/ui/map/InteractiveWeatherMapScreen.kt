package com.kampplus.hava.ui.map

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.kampplus.hava.model.MapCoordinate
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.model.destinationAdvice
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val MapNight = Color(0xFF081721)
private val MapPanel = Color(0xFF102630)
private val MapMint = Color(0xFFA4EAD7)
private val MapMuted = Color(0xFF9AAFB9)

/** Only bundled, app-owned JavaScript can access this read-only coordinate bridge. */
private class MapBridge(
    private val onReady: () -> Unit,
    private val onPoint: (Double, Double) -> Unit,
    private val onViewport: (List<MapCoordinate>) -> Unit,
    private val onEndpoint: (Boolean) -> Unit
) {
    @JavascriptInterface fun ready() = onReady()
    @JavascriptInterface fun selectPoint(lat: Double, lon: Double) = onPoint(lat, lon)
    @JavascriptInterface fun selectEndpoint(departure: Boolean) = onEndpoint(departure)
    @JavascriptInterface fun viewport(json: String) {
        if (json.length > 2000) return
        runCatching { Gson().fromJson(json, Array<MapCoordinate>::class.java).toList() }.getOrNull()?.let(onViewport)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InteractiveWeatherMapScreen(modifier: Modifier = Modifier, viewModel: MapViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var departure by rememberSaveable { mutableStateOf("LTFM") }
    var arrival by rememberSaveable { mutableStateOf("EGLL") }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var ready by remember { mutableStateOf(false) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val gson = remember { Gson() }
    val bridge = remember(viewModel) { MapBridge(
        onReady = { scope.launch { ready = true } },
        onPoint = { lat, lon -> scope.launch { viewModel.selectPoint(lat, lon) } },
        onViewport = { points -> scope.launch { viewModel.viewport(points) } },
        onEndpoint = { dep -> scope.launch { viewModel.selectEndpoint(dep) } }
    ) }
    LaunchedEffect(ready, state.route, webView) {
        if (ready) webView?.evaluateJavascript("window.setRoute(${gson.toJson(state.route)});", null)
    }
    LaunchedEffect(ready, state.markers, webView) {
        if (ready) webView?.evaluateJavascript("window.setWeather(${gson.toJson(state.markers)});", null)
    }
    DisposableEffect(Unit) {
        onDispose { webView?.apply { removeJavascriptInterface("Metaro"); stopLoading(); destroy() } }
    }
    Column(modifier.fillMaxSize().background(MapNight)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("DÜNYAYI KEŞFET", color = MapMint, fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                if (state.route != null || state.routeLoading) TextButton(onClick = viewModel::clearRoute, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(28.dp)) {
                    Text("Rotayı temizle", color = MapMuted, fontSize = 11.sp)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AirportField(departure, { departure = it }, "Kalkış ICAO / IATA", Modifier.weight(1f))
                Text("→", color = MapMint, fontSize = 20.sp)
                AirportField(arrival, { arrival = it }, "Varış ICAO / IATA", Modifier.weight(1f))
                FilledTonalButton(onClick = { expanded = false; viewModel.route(departure, arrival) }, enabled = !state.routeLoading && departure.length in 3..4 && arrival.length in 3..4,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = MapMint, contentColor = MapNight),
                    contentPadding = PaddingValues(horizontal = 12.dp), shape = RoundedCornerShape(14.dp), modifier = Modifier.height(54.dp)) {
                    if (state.routeLoading) CircularProgressIndicator(Modifier.size(18.dp), color = MapMint, strokeWidth = 2.dp)
                    else Text("Git", fontWeight = FontWeight.Bold)
                }
            }
            state.routeError?.let { Text(it, color = Color(0xFFFFB5A7), fontSize = 11.sp) }
        }
        Box(Modifier.fillMaxWidth().weight(1f)) {
            AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
                WebView(context).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.rgb(8, 23, 33))
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    settings.setSupportMultipleWindows(false)
                    addJavascriptInterface(bridge, "Metaro")
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false
                    }
                    webChromeClient = object : android.webkit.WebChromeClient() {
                        override fun onConsoleMessage(message: android.webkit.ConsoleMessage?): Boolean {
                            android.util.Log.d("MetaroMap", "JS: ${message?.message()}")
                            return true
                        }
                    }
                    loadUrl("file:///android_asset/map/index.html")
                    webView = this
                }
            })
            if (!ready) CircularProgressIndicator(Modifier.align(Alignment.Center), color = MapMint)
            state.markerError?.let { Text(it, color = Color(0xFFFFD7A8), fontSize = 10.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 22.dp, start = 12.dp, end = 12.dp).background(MapNight.copy(alpha = .9f), RoundedCornerShape(8.dp)).padding(8.dp)) }
        }
        val selection = state.selected
        if (selection == null) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Bir noktaya dokun, havayı keşfet.", color = Color.White, fontSize = 14.sp)
                Text("Yakınlaştırdıkça tahmin noktaları ayrıntılanır. Uydu görüntüsü canlı değildir.", color = MapMuted, fontSize = 11.sp)
                if (state.route != null) TextButton(onClick = { viewModel.selectEndpoint(false) }, contentPadding = PaddingValues(0.dp)) { Text("Rota hava durumunu göster", color = MapMint) }
            }
        } else {
            Surface(color = MapPanel, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), border = BorderStroke(1.dp, Color(0xFF29434C))) {
                Column(Modifier.fillMaxWidth().heightIn(max = if (expanded) 340.dp else 240.dp).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(selection.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        TextButton(onClick = viewModel::closeSelection, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(40.dp)) { Text("×", color = MapMuted, fontSize = 24.sp) }
                    }
                    if (state.route != null) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EndpointChip("Kalkış", state.departure, { viewModel.selectEndpoint(true) }, Modifier.weight(1f))
                        EndpointChip("Varış", state.arrival, { viewModel.selectEndpoint(false) }, Modifier.weight(1f))
                    }
                    if (selection.loading) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = MapMint, strokeWidth = 2.dp)
                        Text("Güncel tahmin alınıyor…", color = MapMuted, fontSize = 12.sp)
                    }
                    selection.error?.let { Text(it, color = Color(0xFFFFB5A7), fontSize = 12.sp); TextButton(onClick = viewModel::retrySelection) { Text("Tekrar dene", color = MapMint) } }
                    selection.forecast?.let { forecast ->
                        val now = forecast.now
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("${weatherIcon(now.code)} ${number(now.temperature)}°", fontSize = 34.sp, color = Color.White, fontWeight = FontWeight.Light)
                            Column(Modifier.weight(1f)) {
                                Text(now.code?.let { WeatherCondition.fromWeatherCode(it).label } ?: "Durum verisi yok", color = Color.White, fontSize = 12.sp)
                                Text("${now.time.replace('T', ' ')} · ${forecast.timezone}", color = MapMuted, fontSize = 9.sp)
                            }
                            TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(0.dp)) { Text(if (expanded) "Daralt ↑" else "5 gün ↓", color = MapMint, fontSize = 12.sp) }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            MapMetric("NEM", "${number(now.humidity)}%")
                            MapMetric("RÜZGÂR", "${number(now.wind)} km/sa")
                            MapMetric("BASINÇ", "${number(now.pressure)} hPa")
                            MapMetric("YAĞIŞ", "${now.precipitation?.toString() ?: "—"} mm")
                        }
                        if (expanded) {
                            HorizontalDivider(color = Color(0xFF29434C))
                            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                forecast.days.forEach { day ->
                                    Column(Modifier.width(91.dp).background(MapNight.copy(alpha = .6f), RoundedCornerShape(12.dp)).padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Text(runCatching { LocalDate.parse(day.date).format(DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag("tr"))) }.getOrDefault(day.date), color = MapMuted, fontSize = 11.sp)
                                        Text(weatherIcon(day.code), fontSize = 22.sp)
                                        Text("${number(day.minimum)}° / ${number(day.maximum)}°", color = Color.White, fontSize = 12.sp)
                                        Text("Nem %${number(day.humidityMean)}", color = MapMuted, fontSize = 10.sp)
                                        Text("${number(day.windMaximum)} km/sa", color = MapMint, fontSize = 10.sp)
                                        Text("${number(day.pressureMean)} hPa", color = MapMuted, fontSize = 10.sp)
                                    }
                                }
                            }
                            Text("Nem ve basınç: günlük ortalama · Rüzgâr: günlük en yüksek", color = MapMuted, fontSize = 9.sp)
                        }
                        Text(destinationAdvice(selection.airportCode ?: "Bu konum", now), color = Color(0xFFD3E5E6), fontSize = 11.sp, lineHeight = 16.sp)
                        Text("Open-Meteo · Güncel model tahmini · Havacılık raporu değildir", color = MapMuted, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AirportField(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier) {
    OutlinedTextField(value = value, onValueChange = { onChange(it.filter(Char::isLetterOrDigit).take(4).uppercase(Locale.ROOT)) },
        modifier = modifier, label = { Text(label, fontSize = 9.sp) }, singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
        shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = MapMint,
            unfocusedBorderColor = Color(0xFF2D4651), focusedLabelColor = MapMint, unfocusedLabelColor = MapMuted))
}

@Composable
private fun EndpointChip(label: String, selection: MapSelection?, onClick: () -> Unit, modifier: Modifier) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(10.dp), color = MapNight.copy(alpha = .65f)) {
        Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("$label · ${selection?.airportCode.orEmpty()}", color = MapMuted, fontSize = 10.sp)
            Text(when { selection?.loading == true -> "Yükleniyor…"; selection?.forecast != null -> "${weatherIcon(selection.forecast.now.code)} ${number(selection.forecast.now.temperature)}° · ${number(selection.forecast.now.wind)} km/sa"; else -> "Veri alınamadı" }, color = Color.White, fontSize = 11.sp)
        }
    }
}

@Composable
private fun MapMetric(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, color = MapMuted, fontSize = 8.sp, letterSpacing = 1.sp)
        Text(value, color = Color.White, fontSize = 11.sp)
    }
}
private fun number(value: Double?) = value?.takeIf { it.isFinite() }?.roundToInt()?.toString() ?: "—"
private fun weatherIcon(code: Int?) = code?.let { WeatherCondition.fromWeatherCode(it).emoji } ?: "—"
