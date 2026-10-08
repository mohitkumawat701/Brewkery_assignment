@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.brewkery.app.ui.features.detail

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewkery.app.R
import com.brewkery.app.domain.model.CartLine
import com.brewkery.app.domain.model.CartLineKey
import com.brewkery.app.domain.model.ItemCustomizations
import com.brewkery.app.domain.model.MenuItem
import com.brewkery.app.domain.model.MilkOption
import com.brewkery.app.domain.model.SizeOption
import com.brewkery.app.domain.model.StoreMeta
import com.brewkery.app.domain.pricing.PriceCalculator
import com.brewkery.app.ui.components.AppImage
import com.brewkery.app.ui.features.cart.CartViewModel
import com.brewkery.app.ui.features.menu.MenuUiState
import com.brewkery.app.ui.features.menu.MenuViewModel
import com.brewkery.app.ui.theme.Amber
import com.brewkery.app.ui.theme.Border
import com.brewkery.app.ui.theme.BrewkeryTheme
import com.brewkery.app.ui.theme.DarkText
import com.brewkery.app.ui.theme.Espresso
import com.brewkery.app.ui.theme.MutedBrown
import com.brewkery.app.ui.theme.Terracotta

@Composable
fun ItemDetailScreen(
    itemId: Int,
    menuViewModel: MenuViewModel,
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit,
) {
    val uiState by menuViewModel.uiState.collectAsState()
    val meta = (uiState as? MenuUiState.Success)?.meta

    when {
        uiState is MenuUiState.Loading || meta == null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terracotta)
            }
        }
        else -> {
            val item = (uiState as MenuUiState.Success).items.find { it.id == itemId }
            if (item == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Item not found", color = MutedBrown)
                }
            } else {
                ItemDetailContent(
                    item = item,
                    meta = meta,
                    onAddToCart = cartViewModel::addToCart,
                    onBack = onBack,
                    onNavigateToCart = onNavigateToCart,
                )
            }
        }
    }
}

@Composable
private fun ItemDetailContent(
    item: MenuItem,
    meta: StoreMeta,
    onAddToCart: (CartLine) -> Unit,
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit,
) {
    val context = LocalContext.current
    val currencySymbol = meta.currencySymbol

    var chosenSize by remember(item.id) {
        mutableStateOf(item.customizations.sizes.firstOrNull() ?: SizeOption("", "", 0L))
    }
    var chosenMilk by remember(item.id) {
        mutableStateOf(item.customizations.milkOptions.firstOrNull() ?: MilkOption("", "", 0L))
    }
    var chosenSugar by remember(item.id) {
        mutableStateOf(item.customizations.sugarLevels.firstOrNull() ?: "")
    }
    var quantity by remember(item.id) { mutableIntStateOf(1) }
    var isFavorite by remember(item.id) { mutableStateOf(false) }

    val sugarExtraCents = PriceCalculator.parseSugarExtraCents(chosenSugar)
    val unitPriceCents = PriceCalculator.calculateUnitPriceCents(
        basePriceCents = item.basePriceCents,
        sizeExtraCents = chosenSize.extraPriceCents,
        milkExtraCents = chosenMilk.extraPriceCents,
        sugarExtraCents = sugarExtraCents,
    )
    val lineTotalCents = PriceCalculator.calculateLineTotalCents(unitPriceCents, quantity)

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            AddToCartBottomBar(
                quantity = quantity,
                lineTotalCents = lineTotalCents,
                currencySymbol = currencySymbol,
                onIncreaseQuantity = { quantity++ },
                onDecreaseQuantity = { if (quantity > 1) quantity-- },
                onAddToCartClick = {
                    val key = CartLineKey(
                        itemId = item.id,
                        sizeId = chosenSize.id,
                        milkOptionId = chosenMilk.id,
                        sugarLevel = chosenSugar,
                    )
                    onAddToCart(
                        CartLine(
                            key = key,
                            item = item,
                            chosenSize = chosenSize,
                            chosenMilk = chosenMilk,
                            chosenSugar = chosenSugar,
                            quantity = quantity,
                            unitPriceCents = unitPriceCents,
                        )
                    )
                    onNavigateToCart()
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item(key = "header") {
                ItemCustomizerHeader(
                    isFavorite = isFavorite,
                    onBack = onBack,
                    onFavoriteToggle = {
                        isFavorite = !isFavorite
                        Toast.makeText(
                            context,
                            if (isFavorite) "Item saved to favorites" else "Removed from favorites",
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                )
            }

            item(key = "hero") {
                ProductHeroSection(item = item)
            }

            item(key = "title_block") {
                ProductTitleSection(item = item, currencySymbol = currencySymbol)
            }

            if (item.ingredients.isNotEmpty()) {
                item(key = "ingredients") {
                    ProductIngredientsSection(ingredients = item.ingredients)
                }
            }

            if (item.customizations.sizes.isNotEmpty()) {
                item(key = "sizes") {
                    SizeSelectionSection(
                        sizes = item.customizations.sizes,
                        chosenSize = chosenSize,
                        currencySymbol = currencySymbol,
                        onSizeSelected = { chosenSize = it },
                    )
                }
            }

            if (item.customizations.milkOptions.isNotEmpty()) {
                item(key = "milk") {
                    MilkSelectionSection(
                        milkOptions = item.customizations.milkOptions,
                        chosenMilk = chosenMilk,
                        currencySymbol = currencySymbol,
                        onMilkSelected = { chosenMilk = it },
                    )
                }
            }

            if (item.customizations.sugarLevels.isNotEmpty()) {
                item(key = "sugar") {
                    SugarSelectionSection(
                        sugarLevels = item.customizations.sugarLevels,
                        chosenSugar = chosenSugar,
                        onSugarSelected = { chosenSugar = it },
                    )
                }
            }

            item(key = "bottom_space") {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ItemCustomizerHeader(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onFavoriteToggle: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Border),
                shadowElevation = 2.dp,
                onClick = onBack,
            ) {
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back_button),
                        tint = Espresso,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Text(
                text = "ITEM CUSTOMIZER",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Espresso,
                letterSpacing = 0.8.sp,
            )

            Surface(
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Border),
                shadowElevation = 2.dp,
                onClick = onFavoriteToggle,
            ) {
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isFavorite) "❤️" else "🤍",
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductHeroSection(item: MenuItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 6.dp)
            .aspectRatio(2.18f)
            .clip(RoundedCornerShape(16.dp)),
    ) {
        AppImage(
            model = item.imageUrl,
            contentDescription = stringResource(R.string.cd_hero_image),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            shape = RoundedCornerShape(16.dp),
            aspectRatio = null,
        )
        if (item.badge.isNotBlank()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(50),
                color = Espresso.copy(alpha = 0.85f),
            ) {
                Text(
                    text = item.badge.uppercase(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Amber,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp,
                )
            }
        }
    }
}

@Composable
private fun ProductTitleSection(item: MenuItem, currencySymbol: String) {
    Column(modifier = Modifier.padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.name,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.ExtraBold,
                color = DarkText,
                fontSize = 15.sp,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = PriceCalculator.formatPriceAsDollars(item.basePriceCents, currencySymbol),
                fontWeight = FontWeight.ExtraBold,
                color = Terracotta,
                fontSize = 15.sp,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.description,
            fontSize = 11.sp,
            color = MutedBrown,
            lineHeight = 16.sp,
        )
    }
}

@Composable
private fun ProductIngredientsSection(ingredients: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(
            text = "KEY INGREDIENTS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MutedBrown,
            letterSpacing = 0.8.sp,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ingredients.forEach { ingredient ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Border),
                ) {
                    Text(
                        text = ingredient,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedBrown,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SizeSelectionSection(
    sizes: List<SizeOption>,
    chosenSize: SizeOption,
    currencySymbol: String,
    onSizeSelected: (SizeOption) -> Unit,
) {
    CustomizationCard(title = "Size Selection") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            sizes.forEach { size ->
                val isSelected = chosenSize.id == size.id
                val extraLabel = if (size.extraPriceCents > 0)
                    "+${PriceCalculator.formatPriceAsDollars(size.extraPriceCents, currencySymbol)}"
                else "+${currencySymbol}0.00"
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSizeSelected(size) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFFFAEEE5) else Color.White,
                    border = if (isSelected)
                        BorderStroke(1.5.dp, Terracotta)
                    else
                        BorderStroke(1.dp, Border),
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = size.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Terracotta else MutedBrown,
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                        )
                        Text(
                            text = extraLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Terracotta.copy(alpha = 0.8f) else MutedBrown.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MilkSelectionSection(
    milkOptions: List<MilkOption>,
    chosenMilk: MilkOption,
    currencySymbol: String,
    onMilkSelected: (MilkOption) -> Unit,
) {
    CustomizationCard(title = "Milk Options / Spreads") {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            milkOptions.forEach { milk ->
                val isSelected = chosenMilk.id == milk.id
                val extraLabel = if (milk.extraPriceCents > 0)
                    "+${PriceCalculator.formatPriceAsDollars(milk.extraPriceCents, currencySymbol)}"
                else "+${currencySymbol}0.00"
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMilkSelected(milk) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFFFAEEE5) else Color.White,
                    border = if (isSelected)
                        BorderStroke(1.dp, Terracotta)
                    else
                        BorderStroke(1.dp, Border),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = milk.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) Terracotta else MutedBrown,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                        Text(
                            text = extraLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Terracotta else MutedBrown,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SugarSelectionSection(
    sugarLevels: List<String>,
    chosenSugar: String,
    onSugarSelected: (String) -> Unit,
) {
    CustomizationCard(title = "Sugar Levels / Serving") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            sugarLevels.forEach { sugar ->
                val isSelected = chosenSugar == sugar
                Surface(
                    onClick = { onSugarSelected(sugar) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Espresso else Color.White,
                    border = BorderStroke(1.dp, if (isSelected) Espresso else Border),
                ) {
                    Text(
                        text = sugar,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MutedBrown,
                        fontSize = 10.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddToCartBottomBar(
    quantity: Int,
    lineTotalCents: Long,
    currencySymbol: String,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    onAddToCartClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Border),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "−",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkText,
                        modifier = Modifier.clickable { onDecreaseQuantity() },
                    )
                    Text(
                        text = quantity.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DarkText,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "+",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkText,
                        modifier = Modifier.clickable { onIncreaseQuantity() },
                    )
                }
            }

            Button(
                onClick = onAddToCartClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFD9532F),
                                    Color(0xFFB84121),
                                    Color(0xFF8C2B12),
                                ),
                            ),
                            RoundedCornerShape(12.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${stringResource(R.string.label_add_to_cart)} • ${PriceCalculator.formatPriceAsDollars(lineTotalCents, currencySymbol)}",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomizationCard(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Border),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = DarkText,
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Preview(
    name = "Product Detail Screen",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=440",
)
@Composable
private fun ItemDetailScreenPreview() {
    val sampleMeta = StoreMeta(
        appName = "Brewkery Artisans",
        tagline = "Fresh Roast & Bakes",
        currencySymbol = "$",
        deliveryFeeCents = 250L,
        taxRatePercent = 8.0,
        estimatedDeliveryTime = "25 mins",
    )
    val sampleItem = MenuItem(
        id = 1,
        categoryId = "c_hot",
        name = "Toasted Caramel Macchiato",
        tagline = "Rich espresso with toasted caramel syrup and velvety steamed milk.",
        description = "Single-origin espresso layered with house-made salted caramel sauce, silky oat milk, and a delicate amber sugar drizzle.",
        basePriceCents = 485L,
        rating = 4.9,
        reviewCount = 312,
        prepTime = "4 mins",
        calories = 240,
        imageUrl = "https://images.unsplash.com/photo-1570968915860-54d5c301fa9f",
        badge = "BESTSELLER",
        ingredients = listOf("Espresso", "Oat Milk", "Caramel Sauce", "Amber Sugar"),
        customizations = ItemCustomizations(
            sizes = listOf(
                SizeOption("sz_small", "Small (8 oz)", 0L),
                SizeOption("sz_medium", "Medium (12 oz)", 50L),
                SizeOption("sz_large", "Large (16 oz)", 100L),
            ),
            sugarLevels = listOf("Standard (100%)", "Less Sugar (50%)", "No Sugar (0%)"),
            milkOptions = listOf(
                MilkOption("m_whole", "Whole Milk", 0L),
                MilkOption("m_oat", "Oat Milk (Barista Blend)", 60L),
                MilkOption("m_almond", "Almond Milk", 50L),
            ),
        ),
    )

    BrewkeryTheme {
        ItemDetailContent(
            item = sampleItem,
            meta = sampleMeta,
            onAddToCart = {},
            onBack = {},
            onNavigateToCart = {},
        )
    }
}
