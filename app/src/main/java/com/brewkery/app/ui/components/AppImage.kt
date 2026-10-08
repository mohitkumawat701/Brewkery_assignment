package com.brewkery.app.ui.components

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
import com.brewkery.app.ui.theme.BrewkeryTheme
import com.brewkery.app.ui.theme.Terracotta

/**
 * Reusable image component enforcing a strict 1:1 square display container.
 *
 * All displayed images across the application use this component or layout rule:
 * - Display area is always 1:1 square via [aspectRatio](1f).
 * - [ContentScale.Crop] is used for cards/thumbnails (filling 1:1 without distortion).
 * - [ContentScale.Fit] is used for product customizer/detail where complete image is shown inside 1:1 container.
 * - Loading placeholder and error states maintain the exact same 1:1 ratio to prevent layout shifts.
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

// ─────────────────────────────────────────────────────────────────────────────
// Jetpack Compose Previews
// ─────────────────────────────────────────────────────────────────────────────

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

@Preview(name = "2. Portrait Source Image (Crop)", showBackground = true)
@Composable
private fun AppImagePortraitCropPreview() {
    BrewkeryTheme {
        Box(Modifier.padding(16.dp)) {
            AppImage(
                model = "https://images.unsplash.com/photo-1517701604599-bb29b565090c?w=600",
                contentDescription = "Portrait image",
                contentScale = ContentScale.Crop,
                shape = RoundedCornerShape(12.dp),
            )
        }
    }
}

@Preview(name = "3. Landscape Source Image (Crop)", showBackground = true)
@Composable
private fun AppImageLandscapeCropPreview() {
    BrewkeryTheme {
        Box(Modifier.padding(16.dp)) {
            AppImage(
                model = "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=600",
                contentDescription = "Landscape image",
                contentScale = ContentScale.Crop,
                shape = RoundedCornerShape(12.dp),
            )
        }
    }
}

@Preview(name = "4. Very Tall Source Image (Crop)", showBackground = true)
@Composable
private fun AppImageVeryTallCropPreview() {
    BrewkeryTheme {
        Box(Modifier.padding(16.dp)) {
            AppImage(
                model = "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=600",
                contentDescription = "Very tall image",
                contentScale = ContentScale.Crop,
                shape = RoundedCornerShape(12.dp),
            )
        }
    }
}

@Preview(name = "5. Very Wide Source Image (Crop)", showBackground = true)
@Composable
private fun AppImageVeryWideCropPreview() {
    BrewkeryTheme {
        Box(Modifier.padding(16.dp)) {
            AppImage(
                model = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=600",
                contentDescription = "Very wide image",
                contentScale = ContentScale.Crop,
                shape = RoundedCornerShape(12.dp),
            )
        }
    }
}

@Preview(name = "6. Product Detail Image (Fit in 1:1)", showBackground = true)
@Composable
private fun AppImageFitPreview() {
    BrewkeryTheme {
        Box(Modifier.padding(16.dp)) {
            AppImage(
                model = "https://images.unsplash.com/photo-1570968915860-54d5c301fa9f?w=600",
                contentDescription = "Product detail image fit",
                contentScale = ContentScale.Fit,
                shape = RoundedCornerShape(16.dp),
            )
        }
    }
}

@Preview(name = "7. Loading State (1:1)", showBackground = true)
@Composable
private fun AppImageLoadingPreview() {
    BrewkeryTheme {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF7EBE1)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                color = Terracotta,
                strokeWidth = 2.dp,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}

@Preview(name = "8. Error State (1:1)", showBackground = true)
@Composable
private fun AppImageErrorPreview() {
    BrewkeryTheme {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF7EBE1)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "☕", fontSize = 28.sp)
        }
    }
}
