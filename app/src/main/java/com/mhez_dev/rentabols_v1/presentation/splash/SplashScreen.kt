package com.mhez_dev.rentabols_v1.presentation.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhez_dev.rentabols_v1.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    // Animation states
    var startLogoAnimation by remember { mutableStateOf(false) }
    var startTitleAnimation by remember { mutableStateOf(false) }
    var startTaglineAnimation by remember { mutableStateOf(false) }
    
    // Logo animations
    val logoScale by animateFloatAsState(
        targetValue = if (startLogoAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )
    
    val logoAlpha by animateFloatAsState(
        targetValue = if (startLogoAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )
    
    // Gradient background colors - matching auth screen
    val gradientColors = listOf(
        Color(0xFFFFC107), // amber primary
        Color(0xFFd6a000)  // darker amber
    )
    
    // Start animations sequentially
    LaunchedEffect(key1 = true) {
        startLogoAnimation = true
        delay(800)
        startTitleAnimation = true
        delay(500)
        startTaglineAnimation = true
        delay(1500)
        onSplashComplete()
    }
    
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = gradientColors
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated logo
                Card(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(logoScale)
                        .alpha(logoAlpha),
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 12.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_rentabols),
                            contentDescription = "Rentabols Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Animated app name
                AnimatedVisibility(
                    visible = startTitleAnimation,
                    enter = fadeIn(animationSpec = tween(1000)) + 
                            slideInVertically(
                                animationSpec = tween(1000, easing = LinearOutSlowInEasing),
                                initialOffsetY = { it / 2 }
                            )
                ) {
                    Text(
                        text = "Rentabols",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Animated tagline
                AnimatedVisibility(
                    visible = startTaglineAnimation,
                    enter = fadeIn(animationSpec = tween(1000)) + 
                            slideInVertically(
                                animationSpec = tween(1000, easing = LinearOutSlowInEasing),
                                initialOffsetY = { it / 2 }
                            )
                ) {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 32.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.9f)
                        )
                    ) {
                        Text(
                            text = "Rent. Share. Earn.",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFFC107),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp)
                        )
                    }
                }
            }
        }
    }
} 