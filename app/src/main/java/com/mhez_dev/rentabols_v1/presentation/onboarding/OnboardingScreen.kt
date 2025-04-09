package com.mhez_dev.rentabols_v1.presentation.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mhez_dev.rentabols_v1.R

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel
) {
    val shouldShowOnboarding by viewModel.shouldShowOnboarding.collectAsState()
    
    LaunchedEffect(shouldShowOnboarding) {
        if (!shouldShowOnboarding) {
            onFinished()
        }
    }
    
    if (shouldShowOnboarding) {
        // Define the onboarding pages with direct image references
        val pages = listOf(
            OnboardingPage(
                title = "Discover & Rent Items",
                description = "Find a wide variety of rentable items in your neighborhood. From tools to electronics, our platform connects you with what you need.",
                imageRes = R.drawable.image_259
            ),
            OnboardingPage(
                title = "Simple Booking Process",
                description = "Browse, select, and rent with just a few taps. Our secure booking system makes renting easy and worry-free.",
                imageRes = R.drawable.image_268
            ),
            OnboardingPage(
                title = "Earn From Your Items",
                description = "Turn your unused items into income. List your belongings and start earning money by renting them to others in your community.",
                imageRes = R.drawable.image_278
            )
        )
        
        OnboardingPager(
            pages = pages,
            onFinished = { viewModel.onOnboardingFinished() },
            modifier = Modifier.fillMaxSize()
        )
    }
} 