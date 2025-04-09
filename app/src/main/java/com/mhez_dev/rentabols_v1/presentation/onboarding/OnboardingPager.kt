package com.mhez_dev.rentabols_v1.presentation.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mhez_dev.rentabols_v1.ui.components.OnboardingScreen
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun OnboardingPager(
    pages: List<OnboardingPage>,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val page = pages[pageIndex]
            
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(600)) +
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(600)
                        ),
                exit = fadeOut(animationSpec = tween(600)) +
                        slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(600)
                        )
            ) {
                OnboardingScreen(
                    title = page.title,
                    description = page.description,
                    imageRes = page.imageRes,
                    pageIndex = pageIndex,
                    pageCount = pages.size,
                    onNextClicked = {
                        coroutineScope.launch {
                            if (pageIndex < pages.size - 1) {
                                pagerState.animateScrollToPage(
                                    pageIndex + 1,
                                    animationSpec = tween(600)
                                )
                            }
                        }
                    },
                    onSkipClicked = onFinished,
                    onGetStartedClicked = onFinished
                )
            }
        }
    }
} 