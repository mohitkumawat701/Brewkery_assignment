@file:OptIn(ExperimentalMaterial3Api::class)

package com.brewkery.app.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brewkery.app.R
import com.brewkery.app.model.CartLine
import com.brewkery.app.model.CartLineKey
import com.brewkery.app.model.ItemCustomizations
import com.brewkery.app.model.MenuItem
import com.brewkery.app.model.MilkOption
import com.brewkery.app.model.PriceCalculator
import com.brewkery.app.model.SizeOption
import com.brewkery.app.model.StoreMeta
import com.brewkery.app.theme.Border
import com.brewkery.app.theme.BrewkeryTheme
import com.brewkery.app.theme.DarkText
import com.brewkery.app.theme.Espresso
import com.brewkery.app.theme.MutedBrown
import com.brewkery.app.theme.Terracotta

private const val DEFAULT_DELIVERY_FEE_CENTS = 250L
private const val DEFAULT_TAX_RATE_PERCENT = 8.0

@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    menuViewModel: MenuViewModel,
    onBack: () -> Unit,
    onOrderPlaced: () -> Unit,
) {
    val cartLines by cartViewModel.cartLines.collectAsState()
    val subtotalCents by cartViewModel.subtotalCents.collectAsState()
    val menuState by menuViewModel.uiState.collectAsState()
    val meta = (menuState as? MenuUiState.Success)?.meta

    CartScreenContent(
        cartLines = cartLines,
        subtotalCents = subtotalCents,
        meta = meta,
        onBack = onBack,
        onClearCart = cartViewModel::clearCart,
        onUpdateQuantity = { key, qty -> cartViewModel.updateQuantity(key, qty) },
        onPlaceOrder = {
            if (meta != null) {
                cartViewModel.placeOrder(meta)
                onOrderPlaced()
            }
        },
    )
}

@Composable
fun CartScreenContent(
    cartLines: List<CartLine>,
    subtotalCents: Long,
    meta: StoreMeta?,
    onBack: () -> Unit,
    onClearCart: () -> Unit,
    onUpdateQuantity: (CartLineKey, Int) -> Unit,
    onPlaceOrder: () -> Unit,
) {
    val deliveryFeeCents = meta?.deliveryFeeCents ?: DEFAULT_DELIVERY_FEE_CENTS
    val taxRatePercent = meta?.taxRatePercent ?: DEFAULT_TAX_RATE_PERCENT
    val taxCents = PriceCalculator.calculateTaxCents(subtotalCents, taxRatePercent)
    val totalCents = PriceCalculator.calculateTotalCents(subtotalCents, deliveryFeeCents, taxCents)

    Scaffold(
        containerColor = Color(0xFFFDFAF7),
        topBar = {
            CartTopAppBar(
                onBack = onBack,
                onClearCart = onClearCart,
            )
        },
        bottomBar = {
            if (cartLines.isNotEmpty() && meta != null) {
                BottomCheckoutSection(
                    subtotalCents = subtotalCents,
                    deliveryFeeCents = deliveryFeeCents,
                    taxCents = taxCents,
                    totalCents = totalCents,
                    taxRatePercent = taxRatePercent,
                    currencySymbol = meta.currencySymbol,
                    onPlaceOrder = onPlaceOrder,
                )
            }
        },
    ) { innerPadding ->
        if (cartLines.isEmpty()) {
            EmptyCartState(
                modifier = Modifier.padding(innerPadding),
                onBack = onBack,
            )
        } else {
            CartItemList(
                cartLines = cartLines,
                currencySymbol = meta?.currencySymbol ?: "$",
                innerPadding = innerPadding,
                onUpdateQuantity = onUpdateQuantity,
            )
        }
    }
}

@Composable
private fun CartTopAppBar(
    onBack: () -> Unit,
    onClearCart: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back_button),
                    tint = Espresso,
                )
            }
            Text(
                text = "YOUR CART",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Espresso,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp,
            )
            TextButton(onClick = onClearCart) {
                Text(
                    text = "Clear Cart",
                    color = Terracotta,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun CartItemList(
    cartLines: List<CartLine>,
    currencySymbol: String,
    innerPadding: PaddingValues,
    onUpdateQuantity: (CartLineKey, Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + 8.dp,
            bottom = innerPadding.calculateBottomPadding() + 8.dp,
        ),
    ) {
        items(
            items = cartLines,
            key = { line ->
                "${line.key.itemId}|${line.key.sizeId}|${line.key.milkOptionId}|${line.key.sugarLevel}"
            },
        ) { line ->
            CartLineCard(
                line = line,
                currencySymbol = currencySymbol,
                onIncrease = { onUpdateQuantity(line.key, line.quantity + 1) },
                onDecrease = { onUpdateQuantity(line.key, line.quantity - 1) },
            )
        }
    }
}

@Composable
private fun CartLineCard(
    line: CartLine,
    currencySymbol: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
) {
    val sizePart = line.chosenSize.label.takeIf { it.isNotBlank() } ?: ""
    val milkPart = line.chosenMilk.name.takeIf { it.isNotBlank() } ?: ""
    val sugarPart = line.chosenSugar.takeIf { it.isNotBlank() } ?: ""
    val summary = listOf(sizePart, milkPart, sugarPart).filter { it.isNotBlank() }.joinToString(" • ")
    val lineTotalCents = line.unitPriceCents * line.quantity

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = line.item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (summary.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedBrown,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = PriceCalculator.formatPriceAsDollars(lineTotalCents, currencySymbol),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Terracotta,
                )
            }

            Spacer(Modifier.width(12.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF7EBE1),
                border = BorderStroke(1.dp, Border),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "−",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DarkText,
                        modifier = Modifier.clickable { onDecrease() },
                    )
                    Text(
                        text = line.quantity.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(min = 20.dp),
                    )
                    Text(
                        text = "+",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DarkText,
                        modifier = Modifier.clickable { onIncrease() },
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomCheckoutSection(
    subtotalCents: Long,
    deliveryFeeCents: Long,
    taxCents: Long,
    totalCents: Long,
    taxRatePercent: Double,
    currencySymbol: String,
    onPlaceOrder: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFDFAF7),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 12.dp),
        ) {
            OrderSummaryCard(
                subtotalCents = subtotalCents,
                deliveryFeeCents = deliveryFeeCents,
                taxCents = taxCents,
                totalCents = totalCents,
                taxRatePercent = taxRatePercent,
                currencySymbol = currencySymbol,
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Button(
                    onClick = onPlaceOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFD9532F),
                                        Color(0xFFB84121),
                                        Color(0xFF8C2B12),
                                    ),
                                ),
                                shape = RoundedCornerShape(12.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "🔒 ${stringResource(R.string.label_place_order)} • ${PriceCalculator.formatPriceAsDollars(totalCents, currencySymbol)}",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(
    subtotalCents: Long,
    deliveryFeeCents: Long,
    taxCents: Long,
    totalCents: Long,
    taxRatePercent: Double,
    currencySymbol: String,
) {
    val taxRateDisplay = taxRatePercent.toBigDecimal().stripTrailingZeros().toPlainString()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Border),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            SummaryRow("Subtotal", PriceCalculator.formatPriceAsDollars(subtotalCents, currencySymbol))
            SummaryRow("Delivery Fee", PriceCalculator.formatPriceAsDollars(deliveryFeeCents, currencySymbol))
            SummaryRow("Est. Tax ($taxRateDisplay%)", PriceCalculator.formatPriceAsDollars(taxCents, currencySymbol))
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = Border,
                thickness = 1.dp,
            )
            SummaryRow(
                label = "Total Payable",
                amount = PriceCalculator.formatPriceAsDollars(totalCents, currencySymbol),
                isBold = true,
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = if (isBold) 12.sp else 11.sp,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (isBold) DarkText else MutedBrown,
        )
        Text(
            text = amount,
            fontSize = if (isBold) 13.sp else 11.sp,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (isBold) Terracotta else DarkText,
        )
    }
}



@Composable
private fun EmptyCartState(modifier: Modifier = Modifier, onBack: () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("🛒", style = MaterialTheme.typography.displaySmall)
            Text(
                text = stringResource(R.string.empty_cart_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MutedBrown,
            )
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.label_back_to_menu),
                    color = Terracotta,
                )
            }
        }
    }
}

private val previewCartMeta = StoreMeta(
    appName = "Brewkery Artisans",
    tagline = "Fresh Roast & Bakes",
    currencySymbol = "$",
    deliveryFeeCents = 250L,
    taxRatePercent = 8.0,
    estimatedDeliveryTime = "25 mins",
)

private val previewCartLine = CartLine(
    key = CartLineKey(1, "sz_med", "m_oat", "Standard (100%)"),
    item = MenuItem(
        id = 1,
        categoryId = "c_hot",
        name = "Toasted Caramel Macchiato",
        tagline = "Rich espresso with toasted caramel syrup",
        description = "Single-origin espresso layered with house-made salted caramel sauce.",
        basePriceCents = 485L,
        rating = 4.9,
        reviewCount = 312,
        prepTime = "4 mins",
        calories = 240,
        imageUrl = "https://images.unsplash.com/photo-1570968915860-54d5c301fa9f",
        badge = "BESTSELLER",
        ingredients = listOf("Espresso"),
        customizations = ItemCustomizations(
            sizes = emptyList(),
            sugarLevels = emptyList(),
            milkOptions = emptyList(),
        ),
    ),
    chosenSize = SizeOption("sz_med", "Medium (12 oz)", 50L),
    chosenMilk = MilkOption("m_oat", "Oat Milk", 60L),
    chosenSugar = "Standard (100%)",
    quantity = 2,
    unitPriceCents = 595L,
)

@Preview(
    name = "Cart Screen",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=440",
)
@Composable
private fun CartScreenPreview() {
    BrewkeryTheme {
        CartScreenContent(
            cartLines = listOf(previewCartLine),
            subtotalCents = 1190L,
            meta = previewCartMeta,
            onBack = {},
            onClearCart = {},
            onUpdateQuantity = { _, _ -> },
            onPlaceOrder = {},
        )
    }
}

@Preview(
    name = "Empty Cart Screen",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=440",
)
@Composable
private fun EmptyCartScreenPreview() {
    BrewkeryTheme {
        CartScreenContent(
            cartLines = emptyList(),
            subtotalCents = 0L,
            meta = previewCartMeta,
            onBack = {},
            onClearCart = {},
            onUpdateQuantity = { _, _ -> },
            onPlaceOrder = {},
        )
    }
}
