# Multi-Page-Weather-forecast-Application-with-State-Driven-Features-

# 📱 Multi-Page Android App with State-Driven Features

An Android application built using **Jetpack Compose (Kotlin)** that demonstrates modern Android development concepts including **multi-screen navigation**, **state-driven UI**, and **MVVM architecture**.

---

## 🚀 Project Overview

This app includes two main features:

- 🌦️ Weather Fetcher (OpenWeatherMap API integration)
- 🧮 Basic Calculator (Arithmetic operations with validation)

Both modules are connected using **Jetpack Navigation Compose** with a bottom navigation bar and fully reactive UI.

---

## 🏗️ Architecture (MVVM)

### UI Layer
- Jetpack Compose Screens
- `WeatherScreen`, `CalculatorScreen`
- Uses `collectAsState()` for reactive UI

### ViewModel Layer
- `WeatherViewModel`
- `CalculatorViewModel`
- State handled using `MutableStateFlow`

### Data Layer
- Retrofit API Client
- OpenWeatherMap REST API

---

## 🔄 State Management

- `MutableStateFlow` used for UI state
- `StateFlow` observed in UI using `collectAsState()`
- Ensures automatic recomposition on state updates

### Handles:
- Loading state
- Success state
- Error state (network/city not found)

---

## 🌦️ Weather Feature

### Flow:
1. User enters city name
2. Clicks **Fetch Weather**
3. ViewModel triggers coroutine (`viewModelScope.launch`)
4. Retrofit calls API:
