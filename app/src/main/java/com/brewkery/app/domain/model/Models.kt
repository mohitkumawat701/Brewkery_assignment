package com.brewkery.app.domain.model

data class StoreMeta(
    val appName: String,
    val tagline: String,
    val currencySymbol: String,
    val deliveryFeeCents: Long,
    val taxRatePercent: Double,
    val estimatedDeliveryTime: String,
)

data class Category(
    val id: String,
    val name: String,
    val icon: String,
)

data class MenuItem(
    val id: Int,
    val categoryId: String,
    val name: String,
    val tagline: String,
    val description: String,
    val basePriceCents: Long,
    val rating: Double,
    val reviewCount: Int,
    val prepTime: String,
    val calories: Int,
    val imageUrl: String,
    val badge: String,
    val ingredients: List<String>,
    val customizations: ItemCustomizations,
)

data class ItemCustomizations(
    val sizes: List<SizeOption>,
    val sugarLevels: List<String>,
    val milkOptions: List<MilkOption>,
)

data class SizeOption(
    val id: String,
    val label: String,
    val extraPriceCents: Long,
)

data class MilkOption(
    val id: String,
    val name: String,
    val extraPriceCents: Long,
)

data class CartLineKey(
    val itemId: Int,
    val sizeId: String,
    val milkOptionId: String,
    val sugarLevel: String,
)

data class CartLine(
    val key: CartLineKey,
    val item: MenuItem,
    val chosenSize: SizeOption,
    val chosenMilk: MilkOption,
    val chosenSugar: String,
    val quantity: Int,
    val unitPriceCents: Long,
)

val CartLine.lineTotalCents: Long get() = unitPriceCents * quantity

enum class OrderStatus { PREPARING }

data class Order(
    val ticketId: String,
    val status: OrderStatus,
    val totalItemCount: Int,
    val estimatedWait: String,
)
