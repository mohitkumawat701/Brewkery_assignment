package com.brewkery.app.domain.pricing

import com.brewkery.app.domain.model.CartLine
import com.brewkery.app.domain.model.lineTotalCents
import java.math.BigDecimal
import java.math.RoundingMode

object PriceCalculator {

    private const val CENTS_PER_DOLLAR = 100L
    private const val PRICE_DISPLAY_DECIMALS = 2
    private val SUGAR_PRICE_REGEX = Regex("""\(\+(\d+\.\d+)\)""")

    fun parseSugarExtraCents(sugarLevel: String): Long {
        val match = SUGAR_PRICE_REGEX.find(sugarLevel) ?: return 0L
        val dollars = match.groupValues[1].toDoubleOrNull() ?: return 0L
        return dollarsToCents(dollars)
    }

    fun dollarsToCents(dollars: Double): Long =
        BigDecimal.valueOf(dollars)
            .multiply(BigDecimal(CENTS_PER_DOLLAR))
            .setScale(0, RoundingMode.HALF_UP)
            .toLong()

    fun calculateUnitPriceCents(
        basePriceCents: Long,
        sizeExtraCents: Long,
        milkExtraCents: Long,
        sugarExtraCents: Long,
    ): Long = basePriceCents + sizeExtraCents + milkExtraCents + sugarExtraCents

    fun calculateLineTotalCents(unitPriceCents: Long, quantity: Int): Long =
        unitPriceCents * quantity

    fun calculateSubtotalCents(lines: List<CartLine>): Long =
        lines.sumOf { it.lineTotalCents }

    // Delivery fee is excluded from tax calculation per business requirements
    fun calculateTaxCents(subtotalCents: Long, taxRatePercent: Double): Long =
        BigDecimal.valueOf(subtotalCents)
            .multiply(BigDecimal.valueOf(taxRatePercent))
            .divide(BigDecimal(CENTS_PER_DOLLAR), 0, RoundingMode.HALF_UP)
            .toLong()

    fun calculateTotalCents(
        subtotalCents: Long,
        deliveryFeeCents: Long,
        taxCents: Long,
    ): Long = subtotalCents + deliveryFeeCents + taxCents

    fun formatPriceAsDollars(cents: Long, currencySymbol: String): String {
        val dollars = BigDecimal.valueOf(cents).divide(BigDecimal(CENTS_PER_DOLLAR), PRICE_DISPLAY_DECIMALS, RoundingMode.HALF_UP)
        return "$currencySymbol$dollars"
    }

    // Alias for backward compatibility if referenced by legacy callers
    fun formatPrice(cents: Long, currencySymbol: String): String =
        formatPriceAsDollars(cents, currencySymbol)

    fun unitPriceCents(
        basePriceCents: Long,
        sizeExtraCents: Long,
        milkExtraCents: Long,
        sugarExtraCents: Long,
    ): Long = calculateUnitPriceCents(basePriceCents, sizeExtraCents, milkExtraCents, sugarExtraCents)

    fun lineTotalCents(unitPriceCents: Long, quantity: Int): Long =
        calculateLineTotalCents(unitPriceCents, quantity)

    fun subtotalCents(lines: List<CartLine>): Long = calculateSubtotalCents(lines)

    fun taxCents(subtotalCents: Long, taxRatePercent: Double): Long =
        calculateTaxCents(subtotalCents, taxRatePercent)

    fun totalCents(
        subtotalCents: Long,
        deliveryFeeCents: Long,
        taxCents: Long,
    ): Long = calculateTotalCents(subtotalCents, deliveryFeeCents, taxCents)
}
