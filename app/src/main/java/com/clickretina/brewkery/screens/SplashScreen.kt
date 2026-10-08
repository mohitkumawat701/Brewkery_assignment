package com.clickretina.brewkery.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clickretina.brewkery.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    menuViewModel: MenuViewModel,
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by menuViewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        val minSplashDelay = 1200L
        val startTime = System.currentTimeMillis()

        while (uiState is MenuUiState.Loading) {
            delay(100)
        }

        val elapsedTime = System.currentTimeMillis() - startTime
        if (elapsedTime < minSplashDelay) {
            delay(minSplashDelay - elapsedTime)
        }

        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAF2E2)),
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_bg),
            contentDescription = "Brewkery Splash",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(30.dp),
                color = Color(0xFF5C2B14),
                trackColor = Color(0xFFE8D7C8),
                strokeWidth = 2.5.dp,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Loading...",
                color = Color(0xFF6E391C),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp,
            )
        }
    }
}
