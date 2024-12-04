package com.example.brewquest

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.random.nextLong

//Interval between games
fun randomInterval(): Long = Random.nextLong(1000,3000)

fun getRandomMinigame(): String {
    Log.d("StateDebug", "Getting next game")
    val minigames = listOf("TappingGame", "SwipeGame")
    return minigames.random()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrewQuestTheme {
                AppNavigation()
            }
        }
    }
}

// Home screen that shows the Welcome message and start brewing button
@Composable
fun WelcomeScreen(onStartClicked: () -> Unit, modifier: Modifier = Modifier) {
    Log.d("ColorCheck", "Primary color: ${MaterialTheme.colorScheme.primary}")
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to BrewQuest!", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onStartClicked) {
            Text("Start Brewing")
        }
    }
}

// Brewing screen where the user plays the game and the timer is kept
@Composable
fun BrewingScreen(onGameOver: () -> Unit) {
    var currentMinigame by remember { mutableStateOf(getRandomMinigame()) }
    var isGameActive by remember { mutableStateOf(true) }
    var gameKey by remember { mutableStateOf(0) }

    LaunchedEffect(isGameActive) {
        Log.d("StateDebug", "currentMinigame: $currentMinigame, isGameActive: $isGameActive, gameKey: $gameKey")
        if (!isGameActive) {
            kotlinx.coroutines.delay(randomInterval())//wait for the interval
            currentMinigame = getRandomMinigame()
            isGameActive = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("The coffee is brewing!")
        if (isGameActive) {
            when (currentMinigame) {
                "TappingGame" -> TappingGame(
                    onGameOver = {
                        isGameActive = false
                        onGameOver()
                    },
                    onNextGame = {
                        currentMinigame = getRandomMinigame()
                        isGameActive = false
                    }
                )
                "SwipeGame" -> SwipeGame(
                    onGameOver = {
                        isGameActive = false
                        onGameOver()
                    },
                    onNextGame = {
                        currentMinigame = getRandomMinigame()
                        isGameActive = false
                    }
                )
            }
        } else {
            Text("Game loading...")
        }
    }
}

// Navigation for the app and the different screens
@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf("Welcome") }
    when (currentScreen) {
        "Welcome" -> WelcomeScreen(onStartClicked = { currentScreen = "Brewing" })
        "Brewing" -> BrewingScreen(onGameOver = { currentScreen = "Welcome" })
    }
}

//Tapping mini game
@Composable
fun TappingGame(onGameOver: () -> Unit, onNextGame: () -> Unit){
    var tapCount by remember { mutableIntStateOf(0) }
    var timeRemaining by remember { mutableIntStateOf(3)}
    var gameOver by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0 && !gameOver) {
            delay(1000)
            timeRemaining--
        }
    }

    LaunchedEffect(tapCount) {
        if (tapCount >= 10 && !gameOver) {
            gameOver = true
            showSuccessMessage = true
            delay(1000)
            onNextGame()
        }
    }

    LaunchedEffect(timeRemaining) {
        if (timeRemaining == 0 && tapCount < 10 && !gameOver) {
            gameOver = true
            showSuccessMessage = false
            delay(1000)
            onGameOver()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().clickable(enabled = !gameOver && tapCount < 10) { tapCount++ }.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("$timeRemaining seconds", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Taps: $tapCount", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        if(gameOver){
            if (showSuccessMessage) {
                Text(
                    "Good job!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Green
                )
            } else {
                Text(
                    "You Lose!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun SwipeGame(onGameOver: () -> Unit, onNextGame: () -> Unit){
    val directions = listOf("Left", "Right", "Up", "Down")
    val targetDirection = remember { directions.random() }
    var gameOver by remember {mutableStateOf(false)}
    var swipeMatched by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(3000)
        gameOver = true
    }

    LaunchedEffect(gameOver) {
        if (gameOver) {
            if (swipeMatched) {
                showSuccessMessage = true
                delay(1000)
                onNextGame()
            } else {
                onGameOver()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().pointerInput(Unit){
            detectDragGestures { change, dragAmount -> change.consume()
                val (x,y) = dragAmount
                val direction = when {
                    x > 50 && x > kotlin.math.abs(y) -> "Right"
                    x < -50 && kotlin.math.abs(x) > kotlin.math.abs(y) -> "Left"
                    y > 50 && y > kotlin.math.abs(x) -> "Down"
                    y < -50 && kotlin.math.abs(y) > kotlin.math.abs(x) -> "Up"
                    else -> null
                }
                if (direction == targetDirection){
                    swipeMatched = true
                    gameOver = true
                }
            }
        }
    ){
        if (showSuccessMessage) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Good Job!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Green
                )
            }
        } else {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Swipe $targetDirection!",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
    }
}

