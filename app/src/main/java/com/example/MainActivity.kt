package com.example

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PomodoroSession
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val weatherCode = viewModel.currentWeather.collectAsState().value?.weatherCode

    Box(modifier = Modifier.fillMaxSize()) {
        // Dynamic weather atmosphere backdrop
        WeatherGradientBackground(weatherCode = weatherCode)

        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut()
                )
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                "SPLASH" -> OpeningSplashScreen(viewModel = viewModel)
                else -> WorkspaceScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun WeatherGradientBackground(weatherCode: Int?) {
    val gradientColors = when (weatherCode) {
        0 -> listOf(Color(0xFFFAF9F6), Color(0xFFF7F1DF), Color(0xFFEBDCB9)) // Sunny: Warm Oat / Wheat Sand
        1, 2, 3 -> listOf(Color(0xFFFAF9F6), Color(0xFFEBECE7), Color(0xFFDFE2DA)) // Cloudy: Sage Moss Mist
        45, 48 -> listOf(Color(0xFFFAF9F6), Color(0xFFEAEAE3), Color(0xFFD6D5C7)) // Foggy: Linen Stone Clay
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> listOf(Color(0xFFFAF9F6), Color(0xFFDFE6DF), Color(0xFFC7D3C5)) // Rain: River Slate Moss
        71, 73, 75, 77, 85, 86 -> listOf(Color(0xFFFAF9F6), Color(0xFFEEF2EC), Color(0xFFDBE4D7)) // Snowy: Soft Birch Frost
        95, 96, 99 -> listOf(Color(0xFFFAF9F6), Color(0xFFE0E3D8), Color(0xFFC3C7B6)) // Stormy: Loamy Forest Clay
        else -> listOf(Color(0xFFFAF9F6), Color(0xFFF1F0EB), Color(0xFFFAF9F6)) // Default: Alabaster
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = gradientColors))
    )
}

@Composable
fun OpeningSplashScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val liveTime by viewModel.liveTime.collectAsState()
    val liveDate by viewModel.liveDate.collectAsState()
    val weatherState by viewModel.currentWeather.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()
    val currentCountry by viewModel.currentRegionState.collectAsState()
    val isWeatherLoading by viewModel.isWeatherLoading.collectAsState()

    val greeting = remember { getDayGreeting() }
    val (weatherDesc, weatherEmoji) = getWeatherInfo(weatherState?.weatherCode)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Identity Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(
                text = "PomoWeather",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4F6B52), // Elegant calm sage green
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "TEMPO OF THE DAY",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5F6059).copy(alpha = 0.7f), // Olive-gray tint
                letterSpacing = 2.sp
            )
        }

        // Clock and Greeting Section (LIVE TIME)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "$greeting, Ready to Focus?",
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif, // Sophisticated serif typeface for titles
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C17), // Rich charcoal
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = liveTime,
                fontSize = 52.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light, // Slim, high-contrast display layout
                color = Color(0xFF1C1C17),
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("splash_live_clock")
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = liveDate,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5F6059),
                textAlign = TextAlign.Center
            )
        }

        // Live Weather Status Card in Natural Tones theme
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6E1D3)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Location",
                        tint = Color(0xFF4F6B52), // Sage green accent pin
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$currentCity, $currentCountry",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C17)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (isWeatherLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF4F6B52),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = weatherEmoji,
                            fontSize = 48.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = "${weatherState?.temperature ?: "--"}°C",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C17)
                            )
                            Text(
                                text = weatherDesc,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF5F6059)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE6E1D3))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        WeatherMetricItem(
                            label = "Feels Like",
                            value = "${weatherState?.apparentTemperature ?: "--"}°C"
                        )
                        WeatherMetricItem(
                            label = "Humidity",
                            value = "${weatherState?.humidity ?: "--"}%"
                        )
                        WeatherMetricItem(
                            label = "Wind Speed",
                            value = "${weatherState?.windSpeed ?: "--"} km/h"
                        )
                    }
                }
            }
        }

        // CTA Enter Focus Workspace Button - Styled with Sage green and organic depth
        Button(
            onClick = {
                viewModel.currentScreen.value = "TIMER"
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F6B52)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("enter_workspace_button"),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ENTER TIMER WORKSPACE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "ForwardArrow",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun WeatherMetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C17)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF5F6059)
        )
    }
}

@Composable
fun WorkspaceScreen(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val liveTime by viewModel.liveTime.collectAsState()
    val weatherState by viewModel.currentWeather.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()

    val (_, weatherEmoji) = getWeatherInfo(weatherState?.weatherCode)

    Scaffold(
        containerColor = Color.Transparent, // Let dynamic weather background show
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button to Splash - Natural Tones Alabaster fill
                IconButton(
                    onClick = { viewModel.currentScreen.value = "SPLASH" },
                    modifier = Modifier
                        .border(1.dp, Color(0xFFE6E1D3), RoundedCornerShape(12.dp)),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFF1F0EB))
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Go Home Splash",
                        tint = Color(0xFF1C1C17)
                    )
                }

                // Mini Ticking Clocks
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = liveTime,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C17)
                    )
                    Text(
                        text = "LIVE TIME",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5F6059)
                    )
                }

                // Mini Weather Indicator - Organic container with custom frame
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F0EB))
                        .border(1.dp, Color(0xFFE6E1D3), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = weatherEmoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${weatherState?.temperature ?: "--"}°",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C17)
                    )
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(
                currentScreen = currentScreen,
                onTabSelected = { tab ->
                    viewModel.currentScreen.value = tab
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                },
                label = "WorkspaceTabTransition"
            ) { tab ->
                when (tab) {
                    "TIMER" -> TimerWorkspaceView(viewModel = viewModel)
                    "ANALYTICS" -> AnalyticsWorkspaceView(viewModel = viewModel)
                    "SETTINGS" -> SettingsWorkspaceView(viewModel = viewModel)
                    else -> TimerWorkspaceView(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(currentScreen: String, onTabSelected: (String) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFFF1F0EB),
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFFE6E1D3), RoundedCornerShape(24.dp))
    ) {
        NavigationBarItem(
            selected = currentScreen == "TIMER",
            onClick = { onTabSelected("TIMER") },
            icon = { Icon(imageVector = Icons.Default.Schedule, contentDescription = "Timer tab") },
            label = { Text("Timer") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color(0xFF1C1C17),
                indicatorColor = Color(0xFF4F6B52), // Beautiful active sage backdrop pill
                unselectedIconColor = Color(0xFF5F6059),
                unselectedTextColor = Color(0xFF5F6059)
            ),
            modifier = Modifier.testTag("timer_tab")
        )
        NavigationBarItem(
            selected = currentScreen == "ANALYTICS",
            onClick = { onTabSelected("ANALYTICS") },
            icon = { Icon(imageVector = Icons.Default.BarChart, contentDescription = "Analytics tab") },
            label = { Text("Analytics") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color(0xFF1C1C17),
                indicatorColor = Color(0xFF4F6B52), // Beautiful active sage backdrop pill
                unselectedIconColor = Color(0xFF5F6059),
                unselectedTextColor = Color(0xFF5F6059)
            ),
            modifier = Modifier.testTag("analytics_tab")
        )
        NavigationBarItem(
            selected = currentScreen == "SETTINGS",
            onClick = { onTabSelected("SETTINGS") },
            icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings tab") },
            label = { Text("Adjust") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color(0xFF1C1C17),
                indicatorColor = Color(0xFF4F6B52), // Beautiful active sage backdrop pill
                unselectedIconColor = Color(0xFF5F6059),
                unselectedTextColor = Color(0xFF5F6059)
            ),
            modifier = Modifier.testTag("settings_tab")
        )
    }
}

@Composable
fun TimerWorkspaceView(viewModel: MainViewModel) {
    val secondsRemaining by viewModel.timerSecondsRemaining.collectAsState()
    val isRunning by viewModel.timerIsRunning.collectAsState()
    val sessionType by viewModel.sessionType.collectAsState()
    val currentTaskName by viewModel.currentTaskName.collectAsState()
    
    val workMinutes by viewModel.workDurationMinutes.collectAsState()
    val shortMinutes by viewModel.shortBreakDurationMinutes.collectAsState()
    val longMinutes by viewModel.longBreakDurationMinutes.collectAsState()

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val totalDurationMinutes = when (sessionType) {
        "WORK" -> workMinutes
        "SHORT_BREAK" -> shortMinutes
        "LONG_BREAK" -> longMinutes
        else -> 25
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Interval Type Row under Natural Tones theme
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1F0EB))
                    .border(1.dp, Color(0xFFE6E1D3), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val types = listOf(
                    Triple("WORK", "Focus Session", Color(0xFF4F6B52)),
                    Triple("SHORT_BREAK", "Short Break", Color(0xFF8FA89B)),
                    Triple("LONG_BREAK", "Long Break", Color(0xFF5E807F))
                )
                types.forEach { (typeKey, label, accentColor) ->
                    val isSelected = sessionType == typeKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .border(
                                if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6E1D3))
                                else androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                context.apply {
                                    // Gentle haptic feedback
                                    (context as? ComponentActivity)?.window?.decorView?.performHapticFeedback(
                                        HapticFeedbackConstants.LONG_PRESS
                                    )
                                }
                                viewModel.setSessionType(typeKey)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF1C1C17) else Color(0xFF5F6059)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }

        // Core Timer Progress Rings with warm organic tones
        item {
            TimerCircle(
                secondsRemaining = secondsRemaining,
                totalDurationMinutes = totalDurationMinutes,
                isRunning = isRunning,
                sessionType = sessionType
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Play Pause Controls Row styled beautifully to match HTML design specs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button: full circle, alabaster surface with natural border
                IconButton(
                    onClick = {
                        viewModel.resetTimer()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F0EB))
                        .border(1.dp, Color(0xFFE6E1D3), CircleShape)
                        .testTag("reset_timer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset timer",
                        tint = Color(0xFF1C1C17)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Play / Pause Button: Large squircle in dominant theme Sage, active elevation shadows
                Button(
                    onClick = {
                        if (isRunning) {
                            viewModel.pauseTimer()
                        } else {
                            viewModel.startTimer()
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F6B52)),
                    modifier = Modifier
                        .size(76.dp)
                        .testTag("play_pause_button"),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause timer" else "Start timer",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Quick Forward Switch Complete trigger (helps with testing!)
                IconButton(
                    onClick = {
                        viewModel.timerSecondsRemaining.value = 2 // Skip down to complete
                        Toast.makeText(context, "Developer Mode: Skipped session to last seconds!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F0EB))
                        .border(1.dp, Color(0xFFE6E1D3), CircleShape)
                        .testTag("skip_timer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Complete session immediately",
                        tint = Color(0xFF4F6B52)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }

        // Custom Notes / Session Target Text Field - Organic borders
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6E1D3)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Current Focus Objective",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5F6059),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = currentTaskName,
                        onValueChange = { viewModel.currentTaskName.value = it },
                        placeholder = { Text("What are you building now?", color = Color(0xFF5F6059).copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1C1C17),
                            unfocusedTextColor = Color(0xFF1C1C17),
                            focusedContainerColor = Color(0xFFFAF9F6),
                            unfocusedContainerColor = Color(0xFFFAF9F6),
                            focusedBorderColor = Color(0xFF4F6B52),
                            unfocusedBorderColor = Color(0xFFE6E1D3)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("focus_objective_input")
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun TimerCircle(
    secondsRemaining: Int,
    totalDurationMinutes: Int,
    isRunning: Boolean,
    sessionType: String,
    modifier: Modifier = Modifier
) {
    val totalSeconds = totalDurationMinutes * 60
    val progress = if (totalSeconds > 0) secondsRemaining.toFloat() / totalSeconds else 0f
    
    val trackColor = Color(0xFFE6E1D3)
    val progressColor = when (sessionType) {
        "WORK" -> Color(0xFF4F6B52) // Sage green
        "SHORT_BREAK" -> Color(0xFF8FA89B) // Soft Moss
        "LONG_BREAK" -> Color(0xFF5E807F) // Eucalyptus loam
        else -> Color(0xFF4F6B52)
    }
    
    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeStr = String.format("%02d:%02d", minutes, seconds)
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(230.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.width - strokeWidth) / 2
            
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(progressColor.copy(alpha = 0.1f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when (sessionType) {
                        "WORK" -> "FOCUS"
                        "SHORT_BREAK" -> "SHORT BREAK"
                        "LONG_BREAK" -> "LONG BREAK"
                        else -> "FOCUS"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = progressColor,
                    letterSpacing = 2.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = timeStr,
                fontSize = 48.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light,
                color = Color(0xFF1C1C17),
                modifier = Modifier.testTag("timer_ticking_text")
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isRunning) "ACTIVE TIMER" else "PAUSED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5F6059),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun AnalyticsWorkspaceView(viewModel: MainViewModel) {
    val sessionsList by viewModel.sessionsFlow.collectAsState()
    val totalSessions = sessionsList.size
    val totalFocusTimeMinutes = sessionsList.filter { it.sessionType == "WORK" }.sumOf { it.durationMinutes }
    
    // Group metrics
    val breakCount = sessionsList.count { it.sessionType != "WORK" }
    val focusCount = sessionsList.count { it.sessionType == "WORK" }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Grid Metrics Info Cards styled under Natural Tones
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Completed Total focus time
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6E1D3)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Focus Duration",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5F6059)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${totalFocusTimeMinutes} Min",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1C1C17)
                        )
                    }
                }

                // Card 2: Slots ratio completed
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6E1D3)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Work Slots completed",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5F6059)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$focusCount Cycles",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1C1C17)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 3: Breaks logged
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6E1D3)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Breaks Logged",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5F6059)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$breakCount Sessions",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1C1C17)
                        )
                    }
                }

                // Card 4: Streak calculations (simulated or real day intervals)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6E1D3)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Tempo Streak",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5F6059)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Daily Flame 🔥",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1C1C17)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Interactive Custom Canvas Bar Chart card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6E1D3)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    PomodoroBarChart(sessions = sessionsList)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Log History Entries Header and Clear Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Session Log History ($totalSessions)",
                    color = Color(0xFF1C1C17),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (totalSessions > 0) {
                    TextButton(onClick = { viewModel.clearAllHistory() }) {
                        Text("Reset All", color = Color(0xFF4F6B52), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (sessionsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Complete session clocks to populate history statistics logs.",
                        color = Color(0xFF5F6059),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(sessionsList) { session ->
                SessionItemRow(session = session, onDeleteClick = { viewModel.deleteSession(session) })
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun PomodoroBarChart(sessions: List<PomodoroSession>, modifier: Modifier = Modifier) {
    if (sessions.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Empty chart",
                    tint = Color(0xFF5F6059).copy(alpha = 0.5f),
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "No focus data recorded yet.",
                    color = Color(0xFF5F6059),
                    fontSize = 13.sp
                )
            }
        }
        return
    }

    // Isolate last 7 completed sessions
    val lastSessions = sessions.take(7).reversed()
    val maxVal = lastSessions.maxOfOrNull { it.durationMinutes }?.toFloat() ?: 25f
    
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Productivity Load (Last 7 Sessions)",
            color = Color(0xFF1C1C17),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            lastSessions.forEachIndexed { index, session ->
                val barProgress = if (maxVal > 0) session.durationMinutes.toFloat() / maxVal else 0f
                val barColor = if (session.sessionType == "WORK") Color(0xFF4F6B52) else Color(0xFF8FA89B)
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.85f)
                            .width(18.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Background track bar slot
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE6E1D3).copy(alpha = 0.4f))
                        )
                        // Active color progress filled bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(barProgress)
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${session.durationMinutes}m",
                        color = Color(0xFF1C1C17),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            lastSessions.forEach { session ->
                Text(
                    text = if (session.sessionType == "WORK") "Work" else "Break",
                    color = Color(0xFF5F6059),
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SessionItemRow(session: PomodoroSession, onDeleteClick: () -> Unit) {
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(session.timestamp) { formatter.format(session.timestamp) }
    val colorAccent = if (session.sessionType == "WORK") Color(0xFF4F6B52) else Color(0xFF8FA89B)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6E1D3)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Colored Indicator Dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colorAccent)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = session.taskName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C17),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (session.sessionType == "WORK") "Focus block" else "Break rest",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF5F6059)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = Color(0xFFE6E1D3)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedTime,
                            fontSize = 11.sp,
                            color = Color(0xFF5F6059)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${session.durationMinutes}m",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C17),
                    modifier = Modifier.padding(end = 8.dp)
                )
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete item",
                        tint = Color(0xFF5F6059).copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsWorkspaceView(viewModel: MainViewModel) {
    val workVal by viewModel.workDurationMinutes.collectAsState()
    val shortVal by viewModel.shortBreakDurationMinutes.collectAsState()
    val longVal by viewModel.longBreakDurationMinutes.collectAsState()

    val context = LocalContext.current
    var textSearchCity by remember { mutableStateFlowOf(viewModel.searchQuery.value) }
    val searchResults by viewModel.geocodingResults.collectAsState()
    val weatherLoading by viewModel.isWeatherLoading.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Interval Editor Heading
        item {
            Text(
                text = "Adjust Session Intervals",
                color = Color(0xFF1C1C17),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Adjustors Custom Row under Natural Tones theme
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6E1D3)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    IntervalIncrementer(
                        label = "Focus Session (Minutes)",
                        value = workVal,
                        color = Color(0xFF4F6B52), // Sage Green
                        onChanged = { viewModel.updateDurations(it, shortVal, longVal) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFE6E1D3))
                    Spacer(modifier = Modifier.height(14.dp))
                    IntervalIncrementer(
                        label = "Short Break (Minutes)",
                        value = shortVal,
                        color = Color(0xFF8FA89B), // Soft Moss
                        onChanged = { viewModel.updateDurations(workVal, it, longVal) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFE6E1D3))
                    Spacer(modifier = Modifier.height(14.dp))
                    IntervalIncrementer(
                        label = "Long Break (Minutes)",
                        value = longVal,
                        color = Color(0xFF5E807F), // Eucalyptus loamy teal
                        onChanged = { viewModel.updateDurations(workVal, shortVal, it) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }

        // Weather Locator Geosearch Heading
        item {
            Text(
                text = "Change Live City Atmosphere",
                color = Color(0xFF1C1C17),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Type and pick a global city to sync target weather coordinates and morph background aesthetics.",
                color = Color(0xFF5F6059),
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Weather search input
        item {
            OutlinedTextField(
                value = textSearchCity,
                onValueChange = {
                    textSearchCity = it
                    viewModel.searchCity(it)
                },
                placeholder = { Text("Search city (e.g. London, Tokyo, Paris)...", color = Color(0xFF5F6059).copy(alpha = 0.5f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF1C1C17),
                    unfocusedTextColor = Color(0xFF1C1C17),
                    focusedContainerColor = Color(0xFFFAF9F6),
                    unfocusedContainerColor = Color(0xFFFAF9F6),
                    focusedBorderColor = Color(0xFF4F6B52),
                    unfocusedBorderColor = Color(0xFFE6E1D3)
                ),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon", tint = Color(0xFF5F6059))
                },
                trailingIcon = {
                    if (weatherLoading) {
                        CircularProgressIndicator(color = Color(0xFF4F6B52), modifier = Modifier.size(18.dp))
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weather_city_search")
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Live Geocoder search response cards - Styled beautifully
        if (searchResults.isNotEmpty()) {
            items(searchResults) { city ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE6E1D3), RoundedCornerShape(16.dp))
                        .clickable {
                            viewModel.fetchWeather(
                                latitude = city.latitude,
                                longitude = city.longitude,
                                cityName = city.name,
                                region = city.country ?: "Global"
                            )
                            Toast
                                .makeText(
                                    context,
                                    "Location updated to ${city.name}!",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = city.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C17)
                        )
                        Text(
                            text = "${city.admin1 ?: ""} ${city.country ?: ""}",
                            fontSize = 11.sp,
                            color = Color(0xFF5F6059)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Select City pinpoint",
                        tint = Color(0xFF4F6B52)
                    )
                }
            }
        } else if (textSearchCity.trim().length >= 2) {
            item {
                Text(
                    text = "Searching online coordinate indexes...",
                    color = Color(0xFF5F6059),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun IntervalIncrementer(label: String, value: Int, color: Color, onChanged: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C17)
            )
            Text(
                text = "$value Minutes",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Decrement button
            IconButton(
                onClick = { if (value > 1) onChanged(value - 1) },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFF1F0EB)),
                modifier = Modifier.border(1.dp, Color(0xFFE6E1D3), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease length", tint = Color(0xFF1C1C17))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Increment button
            IconButton(
                onClick = { if (value < 120) onChanged(value + 1) },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFF1F0EB)),
                modifier = Modifier.border(1.dp, Color(0xFFE6E1D3), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Increase length", tint = Color(0xFF1C1C17))
            }
        }
    }
}

// Helper to provide a state mutable mapping bypass for standard Compose setups
fun <T> mutableStateFlowOf(initialValue: T): MutableState<T> {
    return mutableStateOf(initialValue)
}

fun getDayGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..21 -> "Good Evening"
        else -> "Good Night"
    }
}

fun getWeatherInfo(code: Int?): Pair<String, String> {
    return when (code) {
        0 -> Pair("Clear Skies", "☀️")
        1, 2 -> Pair("Partly Cloudy", "⛅")
        3 -> Pair("Overcast", "☁️")
        45, 48 -> Pair("Foggy Weather", "🌫️")
        51, 53, 55 -> Pair("Light Drizzle", "🌦️")
        56, 57 -> Pair("Freezing Drizzle", "🌧️❄️")
        61, 63, 65 -> Pair("Rainy Day", "🌧️")
        66, 67 -> Pair("Freezing Rain", "🌧️❄️")
        71, 73, 75 -> Pair("Snowfall", "🌨️")
        77 -> Pair("Snow Grains", "🌨️")
        80, 81, 82 -> Pair("Heavy Showers", "🌧️")
        85, 86 -> Pair("Snow Showers", "🌨️")
        95, 96, 99 -> Pair("Thunderstorm", "⛈️")
        else -> Pair("Productive Day", "✨")
    }
}
