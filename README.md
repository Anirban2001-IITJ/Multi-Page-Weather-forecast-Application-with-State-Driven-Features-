# Multi-Page-Weather-forecast-Application-with-State-Driven-Features-

#Project: Multi-Page Application with State-Driven Features

What It Is
An Android app built with Jetpack Compose (Kotlin) that demonstrates two core Android development concepts — multi-screen navigation and state-driven UI — in a single, clean project.

Architecture Overview
The app follows MVVM (Model-View-ViewModel) pattern with these layers:

UI Layer → Composable screens (WeatherScreen, CalculatorScreen)
ViewModel Layer → WeatherViewModel, CalculatorViewModel (hold and manage UI state)
Data Layer → Retrofit API client calling OpenWeatherMap REST API

State is managed via MutableStateFlow inside ViewModels, collected in the UI using collectAsState() — making the UI reactive and automatically recomposing on data changes.

Screen 1: Weather Fetcher

User types a city name → taps Fetch Weather
ViewModel fires an async coroutine (viewModelScope.launch) calling Retrofit
Hits the OpenWeatherMap API (/data/2.5/weather) with city name and API key
Displays city name and temperature in °C
Handles loading state (button text changes to "Fetching...") and error states (city not found, network failure)

Screenshot shows: "China" entered → returns City: China, Temperature: 23.48°C

Screen 2: Basic Calculator

User enters Number A and Number B
Four operation buttons: +, −, *, /
ViewModel validates inputs (toDoubleOrNull()), handles division-by-zero, formats result to 2 decimal places
Result displayed in red for errors, normal color for valid output

Screenshot shows: 15 * 3 = Result: 45.00

Navigation

Bottom Navigation Bar with two tabs: Weather and Calculator
Uses Jetpack Navigation Compose (NavHost, composable, navController)
Navigation is set up with saveState = true and restoreState = true so each screen remembers its state when you switch tabs — a key detail worth mentioning


Key Technologies to Name-Drop in Interview
ConceptImplementationDeclarative UIJetpack ComposeState managementMutableStateFlow + StateFlowNavigationNavigation Compose, NavHostAsync/NetworkKotlin Coroutines + Retrofit2API parsingGson converterDesign systemMaterial 3 (NavigationBar, ElevatedButton, OutlinedTextField)ArchitectureMVVMThemeDynamic dark/light via isSystemInDarkTheme()
