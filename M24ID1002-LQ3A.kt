package com.example.multipageapplicationandstatedrivenfeatures

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ----------------------------------------------------------------------
// 1. THEME (Usually in a separate ui/theme/Theme.kt file)
// ----------------------------------------------------------------------

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF3700B3)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF3700B3)
)

@Composable
fun MultiPageApplicationandStateDrivenFeaturesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

// ----------------------------------------------------------------------
// 2. DATA MODELS & API SERVICE (For Weather Screen)
// ----------------------------------------------------------------------

data class WeatherResponse(val name: String?, val main: Main?)
data class Main(val temp: Double?)

interface WeatherApiService {
    @GET("weather")
    suspend fun getWeather(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>
}

object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    val instance: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}

// ----------------------------------------------------------------------
// 3. VIEWMODELS (State Management for each screen)
// ----------------------------------------------------------------------

data class WeatherState(
    val city: String = "London",
    val weatherInfo: String = "Awaiting input...",
    val isLoading: Boolean = false
)

class WeatherViewModel : ViewModel() {
    private val _state = MutableStateFlow(WeatherState())
    val state = _state.asStateFlow()
    private val apiKey = "2231ebfb9325c03bddac0f095ba0b037" // Use a valid key

    fun onCityChange(newCity: String) {
        _state.update { it.copy(city = newCity) }
    }

    fun fetchWeather() {
        if (apiKey.contains("YOUR_API_KEY")) {
            _state.update { it.copy(weatherInfo = "Error: Please set your API key in WeatherViewModel.") }
            return
        }
        _state.update { it.copy(isLoading = true, weatherInfo = "Fetching weather...") }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getWeather(_state.value.city, apiKey)
                if (response.isSuccessful && response.body() != null) {
                    val weather = response.body()!!
                    val info = "City: ${weather.name}\nTemperature: ${weather.main?.temp}°C"
                    _state.update { it.copy(weatherInfo = info, isLoading = false) }
                } else {
                    _state.update { it.copy(weatherInfo = "Error: City not found or API error.", isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(weatherInfo = "Error: Network request failed.", isLoading = false) }
            }
        }
    }
}

data class CalculatorState(
    val inputA: String = "",
    val inputB: String = "",
    val result: String = ""
)

class CalculatorViewModel : ViewModel() {
    private val _state = MutableStateFlow(CalculatorState())
    val state = _state.asStateFlow()

    fun onInputAChange(newInput: String) { _state.update { it.copy(inputA = newInput) } }
    fun onInputBChange(newInput: String) { _state.update { it.copy(inputB = newInput) } }

    fun calculate(operator: Char) {
        val numA = _state.value.inputA.toDoubleOrNull()
        val numB = _state.value.inputB.toDoubleOrNull()
        if (numA == null || numB == null) {
            _state.update { it.copy(result = "Error: Invalid input") }
            return
        }
        val calculationResult = when (operator) {
            '+' -> numA + numB
            '-' -> numA - numB
            '*' -> numA * numB
            '/' -> if (numB != 0.0) numA / numB else {
                _state.update { it.copy(result = "Error: Div by Zero") }; return
            }
            else -> {
                _state.update { it.copy(result = "Error: Invalid op") }; return
            }
        }
        _state.update { it.copy(result = "Result: ${"%.2f".format(calculationResult)}") }
    }
}

// ----------------------------------------------------------------------
// 4. NAVIGATION SETUP
// ----------------------------------------------------------------------

sealed class Screen(val route: String) {
    data object Weather : Screen("weather")
    data object Calculator : Screen("calculator")
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar { // Use NavigationBar instead of BottomAppBar for Material3
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val items = listOf(Screen.Weather, Screen.Calculator)

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            if (screen.route == "weather") Icon(Icons.Filled.WbSunny, "Weather")
                            else Icon(Icons.Filled.Calculate, "Calculator")
                        },
                        label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Weather.route, Modifier.padding(innerPadding)) {
            composable(Screen.Weather.route) { WeatherScreen() }
            composable(Screen.Calculator.route) { CalculatorScreen() }
        }
    }
}

// ----------------------------------------------------------------------
// 5. SCREEN COMPOSABLES
// ----------------------------------------------------------------------

@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Weather Fetcher", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = state.city,
            onValueChange = viewModel::onCityChange,
            label = { Text("Enter City Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        ElevatedButton(
            onClick = viewModel::fetchWeather,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isLoading) "Fetching..." else "Fetch Weather")
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = state.weatherInfo,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Basic Calculator", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = state.inputA,
            onValueChange = viewModel::onInputAChange,
            label = { Text("Number A") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.inputB,
            onValueChange = viewModel::onInputBChange,
            label = { Text("Number B") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.calculate('+') }) { Text("+", fontSize = 20.sp) }
            Button(onClick = { viewModel.calculate('-') }) { Text("-", fontSize = 20.sp) }
            Button(onClick = { viewModel.calculate('*') }) { Text("*", fontSize = 20.sp) }
            Button(onClick = { viewModel.calculate('/') }) { Text("/", fontSize = 20.sp) }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = state.result,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (state.result.startsWith("Error")) Color.Red else MaterialTheme.colorScheme.onSurface
        )
    }
}

// ----------------------------------------------------------------------
// 6. COMPOSABLE PREVIEWS
// ----------------------------------------------------------------------

@Preview(showBackground = true, name = "Weather Screen Preview")
@Composable
fun WeatherScreenPreview() {
    MultiPageApplicationandStateDrivenFeaturesTheme {
        WeatherScreen() // Previewing the actual screen which uses a default ViewModel state
    }
}

@Preview(showBackground = true, name = "Calculator Screen Preview")
@Composable
fun CalculatorScreenPreview() {
    MultiPageApplicationandStateDrivenFeaturesTheme {
        CalculatorScreen() // Previewing the actual screen
    }
}

// ----------------------------------------------------------------------
// 7. MAIN ACTIVITY ENTRY POINT
// ----------------------------------------------------------------------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultiPageApplicationandStateDrivenFeaturesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainApp()
                }
            }
        }
    }
}
