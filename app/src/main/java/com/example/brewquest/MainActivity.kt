package com.example.brewquest

import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brewquest.ui.theme.BrewQuestTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import kotlin.random.Random





class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrewQuestTheme {
                AppNavigation() // Now it's above and should work correctly
            }
        }
    }
}

// Home screen that shows the Welcome message and start brewing button
@Composable
fun WelcomeScreen(onStartClicked: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to BrewQuest!", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onStartClicked) {
            Text("Start Brewing")
        }
    }
}

// Brewing screen where the user plays the game and the timer is kept
@Composable
fun BrewingScreen(onFinish: () -> Unit) {
    var timeRemaining by remember { mutableStateOf(4) } // State to hold the time remaining

    // Start countdown timer
    LaunchedEffect(Unit) {
        object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = (millisUntilFinished / 1000).toInt()
            }
            override fun onFinish() {
                onFinish() // Trigger the finish action when the timer ends
            }
        }.start()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "$timeRemaining seconds", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Navigation for the app and the different screens
@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf("Welcome") }
    when (currentScreen) {
        "Welcome" -> WelcomeScreen(onStartClicked = { currentScreen = "Brewing" })
        "Brewing" -> BrewingScreen(onFinish = { currentScreen = "Welcome" })
    }
}