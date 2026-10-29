package com.kampplus.hava.ui.city

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.R
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.EmptyView
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.component.ShimmerList

@Composable
fun CityRoute(
    onCityClick: (City) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CityViewModel = viewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CityScreen(
        query = query,
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::search,
        onCityClick = onCityClick,
        modifier = modifier
    )
}

@Composable
fun CityScreen(
    query: String,
    uiState: LoadState<List<City>>,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCityClick: (City) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF18354F), Color(0xFF071521))))
    ) {
        Column(Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
            Text("METARO  /  WEATHER", color = Color(0xFF78DEED), fontSize = 11.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(10.dp))
            Text("Dünyanın havası.", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Light)
            Text("Bir şehir seç, gününü planla.", color = Color(0xFFB1C5D9), fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
        }
        SearchBar(query, onQueryChange, onSearch, Modifier.padding(horizontal = 18.dp))
        Row(Modifier.fillMaxWidth().padding(start = 22.dp, end = 18.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("ŞEHİRLER", color = Color(0xFFB1C5D9), fontSize = 11.sp, letterSpacing = 1.5.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = onSearch) { Text("Ara", color = Color(0xFF78DEED)) }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (uiState) {
                LoadState.Loading -> ShimmerList()
                is LoadState.Success -> CityList(cities = uiState.data, onCityClick = onCityClick)
                LoadState.Empty -> EmptyView(title = "Şehir bulunamadı", message = "Yazımı kontrol et ya da başka bir şehir dene.")
                is LoadState.Error -> ErrorView(message = uiState.message, onRetry = onSearch)
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.action_clear))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onSearch()
            }
        )
    )
}

@Composable
private fun CityList(
    cities: List<City>,
    onCityClick: (City) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = cities, key = { it.id }) { city ->
            CityCard(city = city, onClick = { onCityClick(city) })
        }
    }
}
