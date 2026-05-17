package com.doruruma.black_screen.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Renders the full-screen black overlay containing the clock and dismiss controls.
 *
 * @param currentTime A string representation of the current live time to display.
 * @param onDismiss Invoked when the user triggers the "Hide Overlay" action.
 * @param modifier Modifier applied to the parent overlay container.
 */
@Composable
fun BlackScreenOverlay(
    currentTime: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var countdown by remember { mutableIntStateOf(10) }
    var visibility by remember { mutableStateOf(false) }
    var textColor by remember { mutableStateOf(Color.Gray) }
    val animatedColor by animateColorAsState(
        targetValue = textColor,
        animationSpec = tween(durationMillis = 500),
        label = "textColor"
    )

    // Darken text after 10s of inactivity
    LaunchedEffect(countdown) {
        if (countdown == 0) {
            textColor = Color.DarkGray
            return@LaunchedEffect
        }

        launch {
            delay(1_000)
            countdown -= 1
        }
    }

    DisposableEffect(Unit) {
        scope.launch {
            delay(10)
            visibility = true
        }

        onDispose {
            visibility = false
        }
    }

    AnimatedVisibility(
        visible = visibility,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(24.dp)
                .clickable(
                    enabled = true,
                    onClick = {
                        // Reset inactivity countdown & brighten text color
                        countdown = 10
                        textColor = Color.Gray
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Live clock displaying time in large, clean font
                Text(
                    text = currentTime,
                    color = animatedColor,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Premium Material 3 Button to dismiss/hide the overlay
                TextButton(
                    onClick = {
                        scope.launch {
                            visibility = false
                            delay(300)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = animatedColor
                    )
                ) {
                    Text(
                        text = "Hide Overlay",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}