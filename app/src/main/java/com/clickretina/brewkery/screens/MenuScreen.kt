@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.clickretina.brewkery.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clickretina.brewkery.R
import com.clickretina.brewkery.components.AppImage
import com.clickretina.brewkery.model.Category
import com.clickretina.brewkery.model.ItemCustomizations
import com.clickretina.brewkery.model.MenuItem
import com.clickretina.brewkery.model.MilkOption
import com.clickretina.brewkery.model.Order
import com.clickretina.brewkery.model.PriceCalculator
import com.clickretina.brewkery.model.SizeOption
import com.clickretina.brewkery.model.StoreMeta
import com.clickretina.brewkery.theme.Amber
import com.clickretina.brewkery.theme.BannerFrom
import com.clickretina.brewkery.theme.BannerTo
import com.clickretina.brewkery.theme.Border
import com.clickretina.brewkery.theme.BrewkeryTheme
import com.clickretina.brewkery.theme.DarkText
import com.clickretina.brewkery.theme.Espresso
import com.clickretina.brewkery.theme.MutedBrown
import com.clickretina.brewkery.theme.Terracotta

@Composable
fun MenuScreen(
    menuViewModel: MenuViewModel,
    cartViewModel: CartViewModel,
    onItemClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    onOrderClick: () -> Unit,
) {
    val uiState by menuViewModel.uiState.collectAsState()
    val selectedCatId by menuViewModel.selectedCategoryId.collectAsState()
    val cartCount by cartViewModel.cartItemCount.collectAsState()
    val subtotalCents by cartViewModel.subtotalCents.collectAsState()
    val activeOrder by cartViewModel.activeOrder.collectAsState()

    MenuScreenContent(
        uiState = uiState,
        selectedCategoryId = selectedCatId,
        cartCount = cartCount,
        subtotalCents = subtotalCents,
        activeOrder = activeOrder,
        onCategorySelected = menuViewModel::selectCategory,
        onRetry = menuViewModel::fetchMenu,
        onItemClick = onItemClick,
        onCartClick = onCartClick,
        onOrderClick = onOrderClick,
    )
}

@Composable
fun MenuScreenContent(
    uiState: MenuUiState,
    selectedCategoryId: String?,
    cartCount: Int,
    subtotalCents: Long,
    activeOrder: Order?,
    onCategorySelected: (String?) -> Unit,
    onRetry: () -> Unit,
    onItemClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    onOrderClick: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            when (val state = uiState) {
                is MenuUiState.Loading -> LoadingState()
                is MenuUiState.Error -> ErrorState(
                    message = state.message,
                    onRetry = onRetry,
                )
                is MenuUiState.Success -> MenuSuccessList(
                    state = state,
                    selectedCategoryId = selectedCategoryId,
                    searchQuery = searchQuery,
                    cartCount = cartCount,
                    subtotalCents = subtotalCents,
                    activeOrder = activeOrder,
                    onQueryChange = { searchQuery = it },
                    onCategorySelected = onCategorySelected,
                    onItemClick = onItemClick,
                    onCartClick = onCartClick,
                    onOrderClick = onOrderClick,
                )
            }
        }
    }
}

@Composable
private fun MenuSuccessList(
    state: MenuUiState.Success,
    selectedCategoryId: String?,
    searchQuery: String,
    cartCount: Int,
    subtotalCents: Long,
    activeOrder: Order?,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onItemClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    onOrderClick: () -> Unit,
) {
    val categoryFiltered = if (selectedCategoryId == null) {
        state.items
    } else {
        state.items.filter { it.categoryId == selectedCategoryId }
    }
    val filteredItems = if (searchQuery.isBlank()) {
        categoryFiltered
    } else {
        val query = searchQuery.trim().lowercase()
        categoryFiltered.filter { item ->
            item.name.lowercase().contains(query) ||
                item.tagline.lowercase().contains(query) ||
                item.categoryId.lowercase().contains(query)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item(key = "appbar") {
            BrewkeryAppBar(
                cartCount = cartCount,
                onCartClick = onCartClick,
            )
        }

        item(key = "banner") {
            val currentOrder = activeOrder
            if (currentOrder != null) {
                ActiveOrderCard(
                    order = currentOrder,
                    onOrderClick = onOrderClick,
                )
            } else {
                StoreInfoBanner(meta = state.meta)
            }
        }

        item(key = "search") {
            BrewkerySearchBar(
                query = searchQuery,
                onQuery = onQueryChange,
            )
        }

        stickyHeader(key = "chips") {
            CategoryChipRow(
                categories = state.categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = onCategorySelected,
            )
        }

        if (filteredItems.isEmpty()) {
            item(key = "empty") {
                EmptyItemsState()
            }
        } else {
            items(filteredItems, key = { it.id }) { item ->
                MenuItemCard(
                    item = item,
                    currencySymbol = state.meta.currencySymbol,
                    onItemClick = { onItemClick(item.id) },
                )
            }
        }

        if (cartCount > 0) {
            item(key = "cart_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                ) {
                    CartCard(
                        cartCount = cartCount,
                        subtotalCents = subtotalCents,
                        currencySymbol = state.meta.currencySymbol,
                        onViewCart = onCartClick,
                    )
                }
            }
        }
    }
}

@Composable
fun BrewkeryAppBar(cartCount: Int, onCartClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Espresso),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "BK",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = "Fresh Roast & Bakes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedBrown,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Brewkery Artisans",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Espresso,
                )
            }
        }

        Box(
            modifier = Modifier.clickable(onClick = onCartClick),
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Border),
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "🛍", fontSize = 14.sp)
                }
            }

            if (cartCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(17.dp)
                        .clip(CircleShape)
                        .background(Terracotta),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (cartCount > 99) "99+" else cartCount.toString(),
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false,
                            ),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
fun StoreInfoBanner(meta: StoreMeta) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(listOf(BannerFrom, BannerTo))
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Border),
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🛵", fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "STORE INFO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Terracotta,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp,
                        )
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Terracotta),
                        )
                    }
                    Text(
                        text = "Delivery in ${meta.estimatedDeliveryTime}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                    )
                    Text(
                        text = "${PriceCalculator.formatPriceAsDollars(meta.deliveryFeeCents, meta.currencySymbol)} flat fee",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedBrown,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Border),
            ) {
                Text(
                    text = "Open",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
fun BrewkerySearchBar(query: String, onQuery: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "🔍", fontSize = 12.sp, color = MutedBrown)
            Spacer(Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQuery,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = DarkText),
                cursorBrush = SolidColor(Terracotta),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Search roast, cold brew, pastry…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedBrown,
                        )
                    }
                    inner()
                },
            )
        }
    }
}

@Composable
fun CategoryChipRow(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "all") {
                CategoryPill(
                    label = "All Items",
                    isSelected = selectedCategoryId == null,
                    onClick = { onCategorySelected(null) },
                )
            }
            items(categories, key = { it.id }) { cat ->
                CategoryPill(
                    label = "${cat.icon} ${cat.name}",
                    isSelected = selectedCategoryId == cat.id,
                    onClick = { onCategorySelected(cat.id) },
                )
            }
        }
    }
}

@Composable
private fun CategoryPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (isSelected) Espresso else Color.White,
        border = if (isSelected) null else BorderStroke(1.dp, Border),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isSelected) Color.White else MutedBrown,
        )
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    currencySymbol: String,
    onItemClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppImage(
                model = item.imageUrl,
                contentDescription = stringResource(R.string.cd_item_image),
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(10.dp),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                if (item.badge.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFEF3C7),
                    ) {
                        Text(
                            text = item.badge.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78350F),
                            fontSize = 8.5.sp,
                            letterSpacing = 0.5.sp,
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                }
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "⭐ %.1f (%d)".format(item.rating, item.reviewCount),
                    fontSize = 11.sp,
                    color = MutedBrown,
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = PriceCalculator.formatPriceAsDollars(item.basePriceCents, currencySymbol),
                        fontWeight = FontWeight.ExtraBold,
                        color = Terracotta,
                        fontSize = 15.sp,
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Terracotta,
                        onClick = onItemClick,
                    ) {
                        Text(
                            text = "+ Customize",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartCard(
    cartCount: Int,
    subtotalCents: Long,
    currencySymbol: String,
    onViewCart: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onViewCart),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Espresso),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Terracotta),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = cartCount.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "View Your Cart",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFEBD8CB),
                    )
                    Text(
                        text = PriceCalculator.formatPriceAsDollars(subtotalCents, currencySymbol),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Proceed to Checkout",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Amber,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Amber,
                )
            }
        }
    }
}

@Composable
fun ActiveOrderCard(order: Order, onOrderClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(BannerFrom, BannerTo)))
            .clickable(onClick = onOrderClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Border),
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🟢", fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ACTIVE ORDER",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF059669),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp,
                        )
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF059669)),
                        )
                    }
                    Text(
                        text = "Active Order ${order.ticketId}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                    )
                    Text(
                        text = "Preparing (Arriving in ${order.estimatedWait})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedBrown,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF059669),
            ) {
                Text(
                    text = "Track",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Terracotta)
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "☕",
            style = MaterialTheme.typography.displaySmall,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(stringResource(R.string.label_retry))
        }
    }
}

@Composable
fun EmptyItemsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "☕", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "No items in this category",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MutedBrown,
            )
        }
    }
}

private val previewStoreMeta = StoreMeta(
    appName = "Brewkery Artisans",
    tagline = "Fresh Roast & Bakes",
    currencySymbol = "$",
    deliveryFeeCents = 250L,
    taxRatePercent = 8.0,
    estimatedDeliveryTime = "25 mins",
)

private val previewCategories = listOf(
    Category(id = "c_hot", name = "Hot Coffee", icon = "☕"),
    Category(id = "c_cold", name = "Cold Brews", icon = "🥤"),
    Category(id = "c_pastry", name = "Artisan Pastries", icon = "🥐"),
)

private val previewMenuItems = listOf(
    MenuItem(
        id = 1,
        categoryId = "c_hot",
        name = "Toasted Caramel Macchiato",
        tagline = "Rich espresso with toasted caramel syrup and velvety steamed milk.",
        description = "Single-origin espresso layered with house-made salted caramel sauce.",
        basePriceCents = 485L,
        rating = 4.9,
        reviewCount = 312,
        prepTime = "4 mins",
        calories = 240,
        imageUrl = "https://images.unsplash.com/photo-1570968915860-54d5c301fa9f",
        badge = "BESTSELLER",
        ingredients = listOf("Espresso", "Oat Milk", "Caramel Sauce"),
        customizations = ItemCustomizations(
            sizes = listOf(SizeOption("sz_med", "Medium (12 oz)", 0L)),
            sugarLevels = listOf("Standard (100%)"),
            milkOptions = listOf(MilkOption("m_oat", "Oat Milk", 60L)),
        ),
    ),
    MenuItem(
        id = 2,
        categoryId = "c_cold",
        name = "Velvet Vanilla Cold Foam",
        tagline = "Slow-steeped cold brew crowned with sweet vanilla cream foam.",
        description = "Signature 18-hour cold brew over ice, topped with sweet vanilla cold foam.",
        basePriceCents = 540L,
        rating = 4.8,
        reviewCount = 248,
        prepTime = "3 mins",
        calories = 190,
        imageUrl = "https://images.unsplash.com/photo-1517701604599-bb29b565090c",
        badge = "STAFF PICK",
        ingredients = listOf("Cold Brew", "Vanilla Cream"),
        customizations = ItemCustomizations(
            sizes = listOf(SizeOption("sz_med", "Medium (12 oz)", 0L)),
            sugarLevels = listOf("Standard"),
            milkOptions = listOf(MilkOption("m_cream", "Vanilla Cream", 0L)),
        ),
    ),
)

@Preview(
    name = "Menu Screen",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=440",
)
@Composable
private fun MenuScreenPreview() {
    BrewkeryTheme {
        MenuScreenContent(
            uiState = MenuUiState.Success(
                meta = previewStoreMeta,
                categories = previewCategories,
                items = previewMenuItems,
            ),
            selectedCategoryId = null,
            cartCount = 2,
            subtotalCents = 1025L,
            activeOrder = null,
            onCategorySelected = {},
            onRetry = {},
            onItemClick = {},
            onCartClick = {},
            onOrderClick = {},
        )
    }
}

@Preview(name = "Product Card", showBackground = true)
@Composable
private fun MenuItemCardPreview() {
    BrewkeryTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MenuItemCard(
                item = previewMenuItems.first(),
                currencySymbol = "$",
                onItemClick = {},
            )
        }
    }
}

@Preview(name = "Cart Card", showBackground = true)
@Composable
private fun CartCardPreview() {
    BrewkeryTheme {
        CartCard(
            cartCount = 2,
            subtotalCents = 1025L,
            currencySymbol = "$",
            onViewCart = {},
        )
    }
}
