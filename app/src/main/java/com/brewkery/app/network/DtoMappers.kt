package com.brewkery.app.network

import com.brewkery.app.model.Category
import com.brewkery.app.model.ItemCustomizations
import com.brewkery.app.model.MenuItem
import com.brewkery.app.model.MilkOption
import com.brewkery.app.model.PriceCalculator
import com.brewkery.app.model.SizeOption
import com.brewkery.app.model.StoreMeta

fun MetaDto.toDomain(): StoreMeta = StoreMeta(
    appName = app ?: "Brewkery",
    tagline = tagline ?: "",
    currencySymbol = currencySymbol ?: "$",
    deliveryFeeCents = PriceCalculator.dollarsToCents(deliveryFee ?: 0.0),
    taxRatePercent = taxRatePercent ?: 0.0,
    estimatedDeliveryTime = estimatedDeliveryTime ?: "",
)

fun CategoryDto.toDomain(): Category = Category(
    id = id ?: "",
    name = name ?: "",
    icon = icon ?: "",
)

fun MenuItemDto.toDomain(): MenuItem = MenuItem(
    id = id ?: 0,
    categoryId = categoryId ?: "",
    name = name ?: "",
    tagline = tagline ?: "",
    description = description ?: "",
    basePriceCents = PriceCalculator.dollarsToCents(basePrice ?: 0.0),
    rating = rating ?: 0.0,
    reviewCount = reviewCount ?: 0,
    prepTime = prepTime ?: "",
    calories = calories ?: 0,
    imageUrl = imageUrl ?: "",
    badge = badge ?: "",
    ingredients = ingredients,
    customizations = customizations?.toDomain() ?: ItemCustomizations(emptyList(), emptyList(), emptyList()),
)

fun CustomizationsDto.toDomain(): ItemCustomizations = ItemCustomizations(
    sizes = sizes.map { it.toDomain() },
    sugarLevels = sugarLevels,
    milkOptions = milkOptions.map { it.toDomain() },
)

fun SizeOptionDto.toDomain(): SizeOption = SizeOption(
    id = id ?: "",
    label = label ?: "",
    extraPriceCents = PriceCalculator.dollarsToCents(extraPrice ?: 0.0),
)

fun MilkOptionDto.toDomain(): MilkOption = MilkOption(
    id = id ?: "",
    name = name ?: "",
    extraPriceCents = PriceCalculator.dollarsToCents(extraPrice ?: 0.0),
)
