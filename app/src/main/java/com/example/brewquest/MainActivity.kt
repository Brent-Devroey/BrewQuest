package com.example.brewquest

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.brewquest.ui.theme.BrewQuestTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlin.random.Random
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.platform.LocalContext


//Interval between games
fun randomInterval(): Long = Random.nextLong(1000,1500)

fun getRandomMinigame(): String {
    Log.d("StateDebug", "Getting next game")
    val minigames = listOf("TappingGame", "SwipeGame", "VibrateGame")
    return minigames.random()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrewQuestTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent(){
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ){
        AppNavigation()
    }
}

// Home screen that shows the Welcome message and start brewing button
@Composable
fun WelcomeScreen(onStartClicked: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("BrewQuest", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(200.dp))
        Button(onClick = onStartClicked, modifier = Modifier.size(width = 200.dp, height = 60.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)) {
            Text("Start Brewing")
        }
    }
}

// Brewing screen where the user plays the game and the timer is kept
@Composable
fun BrewingScreen(onGameOver: () -> Unit) {
    var currentMinigame by remember { mutableStateOf(getRandomMinigame()) }
    var isGameActive by remember { mutableStateOf(true) }
    var showResult by remember { mutableStateOf(false) }
    var gameResult by remember { mutableStateOf(false) }

    LaunchedEffect(isGameActive) {
        Log.d("StateDebug", "currentMinigame: $currentMinigame, isGameActive: $isGameActive")
        if (!isGameActive) {
            kotlinx.coroutines.delay(randomInterval())
            currentMinigame = getRandomMinigame()
            isGameActive = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("The coffee is brewing!", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        if (showResult) {
            ResultScreen(
                success = gameResult,
                onNextGame = {
                    showResult = false
                    isGameActive = true
                },
                onGameOver = onGameOver
            )
        } else if (isGameActive) {
            when (currentMinigame) {
                "TappingGame" -> TappingGame(
                    onGameOver = {
                        gameResult = false
                        showResult = true
                        isGameActive = false
                    },
                    onNextGame = {
                        gameResult = true
                        showResult = true
                        isGameActive = false
                    }
                )
                "SwipeGame" -> SwipeGame(
                    onGameOver = {
                        gameResult = false
                        showResult = true
                        isGameActive = false
                    },
                    onNextGame = {
                        gameResult = true
                        showResult = true
                        isGameActive = false
                    }
                )
                "VibrateGame" -> VibrateGame(
                    onGameOver = {
                        gameResult = false
                        showResult = true
                        isGameActive = false
                    },
                    onNextGame = {
                        gameResult = true
                        showResult = true
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

//Tapping minigame
@Composable
fun TappingGame(onGameOver: () -> Unit, onNextGame: () -> Unit){
    var tapCount by remember { mutableIntStateOf(0) }
    var timeRemaining by remember { mutableIntStateOf(3)}
    var gameOver by remember { mutableStateOf(false) }

    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0 && !gameOver) {
            delay(1000)
            timeRemaining--
        }
    }

    LaunchedEffect(tapCount) {
        if (tapCount >= 10 && !gameOver) {
            gameOver = true
            onNextGame()
        }
    }

    LaunchedEffect(timeRemaining) {
        if (timeRemaining == 0 && tapCount < 10 && !gameOver) {
            gameOver = true
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
    }
}

//Swipe minigame
@Composable
fun SwipeGame(onGameOver: () -> Unit, onNextGame: () -> Unit){
    val directions = listOf("Left", "Right", "Up", "Down")
    val targetDirection = remember { directions.random() }
    var gameOver by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableIntStateOf(1) }
    var swipeMatched by remember { mutableStateOf(false) }

    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0 && !gameOver) {
            delay(1000)
            timeRemaining--
        }
    }

    LaunchedEffect(Unit) {
        delay(1000)
        if (!swipeMatched) {
            gameOver = true
            onGameOver()
        }
    }

    LaunchedEffect(gameOver) {
        if (gameOver) {
            if (swipeMatched) {
                onNextGame()
            } else {
                onGameOver()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                val (x, y) = dragAmount
                val direction = when {
                    x > 50 && x > kotlin.math.abs(y) -> "Right"
                    x < -50 && kotlin.math.abs(x) > kotlin.math.abs(y) -> "Left"
                    y > 50 && y > kotlin.math.abs(x) -> "Down"
                    y < -50 && kotlin.math.abs(y) > kotlin.math.abs(x) -> "Up"
                    else -> null
                }
                if (direction == targetDirection) {
                    swipeMatched = true
                    gameOver = true
                }
            }
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (!gameOver) {
            Text(
                "$timeRemaining seconds",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Swipe $targetDirection!", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun VibrateGame(onGameOver: () -> Unit, onNextGame: () -> Unit){
    var vibrated by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var tapTime by remember { mutableLongStateOf(0) }
    val requiredTime by remember { mutableLongStateOf(1000) }
    var canTap by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator


    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(Random.nextLong(1000, 3000))
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        vibrated = true
        tapTime = System.currentTimeMillis()
        kotlinx.coroutines.delay(requiredTime)
        canTap = true
        if (!gameOver) {
            gameOver = true
            onGameOver()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().clickable {
                if (vibrated && !gameOver) {
                    val currentTime = System.currentTimeMillis()

                        if (currentTime - tapTime <= requiredTime) {
                            gameOver = true
                            onNextGame()
                        } else {
                            gameOver = true
                            onGameOver()
                        }
                } else if (!canTap) {
                    gameOver = true
                    onGameOver()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (!gameOver) {
            Text(text = if (vibrated) "!!!" else "Wait...",  style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ResultScreen(success: Boolean, onNextGame: () -> Unit, onGameOver: () -> Unit) {
    val message = if (success) "Keep Brewing!" else "You Failed!"
    val color = if (success) Color.Green else Color.Red

    LaunchedEffect(Unit) {
        delay(2000)
        if (success) {
            onNextGame()
        } else {
            onGameOver()
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(message, style = MaterialTheme.typography.headlineLarge, color = color)
    }
}





