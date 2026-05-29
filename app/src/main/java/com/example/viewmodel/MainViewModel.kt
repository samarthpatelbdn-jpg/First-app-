package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PomodoroRepository
import com.example.data.PomodoroSession
import com.example.network.WeatherApiClient
import com.example.network.CurrentWeather
import com.example.network.GeocodingResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = PomodoroRepository(database.pomodoroDao())
    
    // Direct Flow stream from Room Database
    val sessionsFlow: StateFlow<List<PomodoroSession>> = repository.allSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Live Clock dates and times
    val liveTime = MutableStateFlow("00:00:00")
    val liveDate = MutableStateFlow("")
    
    // Core Pomodoro Timer states
    val timerSecondsRemaining = MutableStateFlow(25 * 60)
    val timerIsRunning = MutableStateFlow(false)
    val currentTaskName = MutableStateFlow("Productive Session")
    val sessionType = MutableStateFlow("WORK") // "WORK", "SHORT_BREAK", "LONG_BREAK"
    
    // Configurable interval lengths (in minutes)
    val workDurationMinutes = MutableStateFlow(25)
    val shortBreakDurationMinutes = MutableStateFlow(5)
    val longBreakDurationMinutes = MutableStateFlow(15)
    
    // Live Weather tracking states
    val currentWeather = MutableStateFlow<CurrentWeather?>(null)
    val currentCity = MutableStateFlow("Mumbai")
    val currentRegionState = MutableStateFlow("India")
    val isWeatherLoading = MutableStateFlow(false)
    val weatherError = MutableStateFlow<String?>(null)
    val geocodingResults = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val searchQuery = MutableStateFlow("")
    
    // Navigation flow inside Single-View structure (SPLASH / WORKSPACE)
    val currentScreen = MutableStateFlow("SPLASH") // "SPLASH", "TIMER", "ANALYTICS", "SETTINGS"
    
    private var timerJob: Job? = null
    
    init {
        // Start running high-resolution ticking clock
        startLiveClock()
        // Load default weather on opening
        fetchWeather(latitude = 19.0760, longitude = 72.8777, cityName = "Mumbai", region = "India")
    }
    
    private fun startLiveClock() {
        viewModelScope.launch {
            val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
            while (true) {
                val calendar = Calendar.getInstance()
                liveTime.value = timeFormat.format(calendar.time)
                liveDate.value = dateFormat.format(calendar.time)
                delay(1000)
            }
        }
    }
    
    // Timer operations
    fun startTimer() {
        if (timerIsRunning.value) return
        timerIsRunning.value = true
        timerJob = viewModelScope.launch {
            while (timerSecondsRemaining.value > 0) {
                delay(1000)
                timerSecondsRemaining.value = timerSecondsRemaining.value - 1
            }
            onTimerFinished()
        }
    }
    
    fun pauseTimer() {
        timerIsRunning.value = false
        timerJob?.cancel()
    }
    
    fun resetTimer() {
        pauseTimer()
        timerSecondsRemaining.value = getDurationMinutesSelected(sessionType.value) * 60
    }
    
    fun setSessionType(type: String) {
        pauseTimer()
        sessionType.value = type
        timerSecondsRemaining.value = getDurationMinutesSelected(type) * 60
    }
    
    fun updateDurations(work: Int, short: Int, long: Int) {
        workDurationMinutes.value = work
        shortBreakDurationMinutes.value = short
        longBreakDurationMinutes.value = long
        
        // Refresh timer value if stopped or apply to corresponding active type
        if (!timerIsRunning.value) {
            timerSecondsRemaining.value = getDurationMinutesSelected(sessionType.value) * 60
        }
    }
    
    private fun getDurationMinutesSelected(type: String): Int {
        return when (type) {
            "WORK" -> workDurationMinutes.value
            "SHORT_BREAK" -> shortBreakDurationMinutes.value
            "LONG_BREAK" -> longBreakDurationMinutes.value
            else -> 25
        }
    }
    
    private fun onTimerFinished() {
        timerIsRunning.value = false
        val type = sessionType.value
        val duration = getDurationMinutesSelected(type)
        val taskNameText = if (type == "WORK") currentTaskName.value else "Take a Break"
        
        viewModelScope.launch {
            // Save log to Room database
            repository.insertSession(
                PomodoroSession(
                    taskName = taskNameText,
                    durationMinutes = duration,
                    sessionType = type,
                    completed = true
                )
            )
            
            // Auto transition to break or work code
            if (type == "WORK") {
                sessionType.value = "SHORT_BREAK"
                timerSecondsRemaining.value = shortBreakDurationMinutes.value * 60
            } else {
                sessionType.value = "WORK"
                timerSecondsRemaining.value = workDurationMinutes.value * 60
            }
            
            // Re-trigger visual finish alert
        }
    }
    
    // For logging custom manually completed sessions if needed
    fun logManualSession(task: String, minutes: Int, type: String) {
        viewModelScope.launch {
            repository.insertSession(
                PomodoroSession(
                    taskName = task,
                    durationMinutes = minutes,
                    sessionType = type,
                    completed = true
                )
            )
        }
    }
    
    // Delete single log
    fun deleteSession(session: PomodoroSession) {
        viewModelScope.launch {
            repository.deleteSessionById(session.id)
        }
    }
    
    // Clear all history
    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllSessions()
        }
    }
    
    // Fetch live weather data
    fun fetchWeather(latitude: Double, longitude: Double, cityName: String, region: String) {
        viewModelScope.launch {
            isWeatherLoading.value = true
            weatherError.value = null
            try {
                val response = WeatherApiClient.api.getWeather(
                    url = "https://api.open-meteo.com/v1/forecast",
                    latitude = latitude,
                    longitude = longitude,
                    current = "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m"
                )
                currentWeather.value = response.current
                currentCity.value = cityName
                currentRegionState.value = region
            } catch (e: Exception) {
                weatherError.value = "Unable to fetch location weather: ${e.localizedMessage}"
            } finally {
                isWeatherLoading.value = false
            }
        }
    }
    
    // Geocoding query city search
    fun searchCity(name: String) {
        searchQuery.value = name
        if (name.trim().length < 2) {
            geocodingResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val response = WeatherApiClient.api.getGeocoding(
                    url = "https://geocoding-api.open-meteo.com/v1/search",
                    cityName = name,
                    count = 6,
                    language = "en",
                    format = "json"
                )
                geocodingResults.value = response.results ?: emptyList()
            } catch (e: Exception) {
                // geocoder search fails silently/retry
            }
        }
    }
}
