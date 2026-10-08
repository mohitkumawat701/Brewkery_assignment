package com.clickretina.brewkery.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.clickretina.brewkery.theme.BrewkeryTheme
import com.clickretina.brewkery.theme.Terracotta

/**
 * Reusable image component enforcing a strict 1:1 square display container by default.
 */
@Composable
fun AppImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RoundedCornerShape(0.dp),
    backgroundColor: Color = Color.Transparent,
    aspectRatio: Float? = 1f,
) {
    val context = LocalContext.current
    val imageRequest = ImageRequest.Builder(context)
        .data(model)
        .crossfade(true)
        .build()

    val boxModifier = if (aspectRatio != null) {
        modifier.aspectRatio(aspectRatio)
    } else {
        modifier
    }

    Box(
        modifier = boxModifier
            .clip(shape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = Terracotta,
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "☕", fontSize = 28.sp)
                }
            },
        )
    }
}

@Preview(name = "1. Square Source Image (Crop)", showBackground = true)
@Composable
private fun AppImageSquareCropPreview() {
    BrewkeryTheme {
        Box(Modifier.padding(16.dp)) {
            AppImage(
                model = "https://images.unsplash.com/photo-1570968915860-54d5c301fa9f?w=600",
                contentDescription = "Square image",
                contentScale = ContentScale.Crop,
                shape = RoundedCornerShape(12.dp),
            )
        }
    }
}
