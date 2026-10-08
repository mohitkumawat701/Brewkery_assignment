package com.brewkery.app

import com.brewkery.app.domain.model.CartLine
import com.brewkery.app.domain.model.CartLineKey
import com.brewkery.app.domain.model.ItemCustomizations
import com.brewkery.app.domain.model.MenuItem
import com.brewkery.app.domain.model.MilkOption
import com.brewkery.app.domain.model.SizeOption
import com.brewkery.app.domain.model.lineTotalCents
import com.brewkery.app.domain.pricing.PriceCalculator
import com.brewkery.app.util.TicketIdGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private fun makeMilk(id: String, extraCents: Long) = MilkOption(id, id, extraCents)
private fun makeSize(id: String, extraCents: Long) = SizeOption(id, id, extraCents)

private fun makeItem(id: Int = 1, baseCents: Long = 500L) = MenuItem(
    id = id, categoryId = "cat_hot_coffee", name = "Test Coffee",
    tagline = "", description = "", basePriceCents = baseCents,
    rating = 4.5, reviewCount = 100, prepTime = "5 min", calories = 200,
    imageUrl = "", badge = "", ingredients = emptyList(),
    customizations = ItemCustomizations(emptyList(), emptyList(), emptyList()),
)

private fun makeCartLine(
    item: MenuItem,
    size: SizeOption,
    milk: MilkOption,
    sugar: String,
    quantity: Int,
): CartLine {
    val sugarExtra = PriceCalculator.parseSugarExtraCents(sugar)
    val unit = PriceCalculator.calculateUnitPriceCents(item.basePriceCents, size.extraPriceCents, milk.extraPriceCents, sugarExtra)
    return CartLine(
        key = CartLineKey(item.id, size.id, milk.id, sugar),
        item = item,
        chosenSize = size,
        chosenMilk = milk,
        chosenSugar = sugar,
        quantity = quantity,
        unitPriceCents = unit,
    )
}

class PriceCalculatorReferenceTest {

    @Test
    fun `reference case gives 12_65 total`() {
        val subtotal = 940L
        val delivery = 250L
        val taxRate = 8.0
        val tax = PriceCalculator.calculateTaxCents(subtotal, taxRate)
        val total = PriceCalculator.calculateTotalCents(subtotal, delivery, tax)

        assertEquals("Tax should be 75 cents", 75L, tax)
        assertEquals("Total should be 1265 cents", 1265L, total)
    }
}

class UnitPriceTest {

    @Test
    fun `unit price sums base + size + milk extras`() {
        val unit = PriceCalculator.calculateUnitPriceCents(500L, 50L, 30L, 0L)
        assertEquals(580L, unit)
    }

    @Test
    fun `unit price includes sugar extra from regex`() {
        val sugarExtra = PriceCalculator.parseSugarExtraCents("Light Wildflower Honey (+0.40)")
        val unit = PriceCalculator.calculateUnitPriceCents(500L, 0L, 0L, sugarExtra)
        assertEquals(40L, sugarExtra)
        assertEquals(540L, unit)
    }

    @Test
    fun `plain sugar level has zero extra`() {
        val sugarExtra = PriceCalculator.parseSugarExtraCents("Regular")
        assertEquals(0L, sugarExtra)
    }
}

class TaxRoundingTest {

    @Test
    fun `tax rounds HALF_UP at midpoint`() {
        assertEquals(75L, PriceCalculator.calculateTaxCents(940L, 8.0))
        assertEquals(76L, PriceCalculator.calculateTaxCents(950L, 8.0))
        assertEquals(76L, PriceCalculator.calculateTaxCents(945L, 8.0))
    }

    @Test
    fun `delivery fee is excluded from tax base`() {
        val correctTax = PriceCalculator.calculateTaxCents(940L, 8.0)
        val wrongTax = PriceCalculator.calculateTaxCents(940L + 250L, 8.0)
        assertEquals(75L, correctTax)
        assertTrue("Delivery must not be taxed", correctTax < wrongTax)
    }
}

class CartMergeTest {

    private val item = makeItem()
    private val sizeM = makeSize("sz_medium", 50L)
    private val sizeL = makeSize("sz_large", 100L)
    private val milkO = makeMilk("m_oat", 30L)
    private val milkA = makeMilk("m_almond", 40L)
    private val sugar = "Regular"

    private fun mergeInto(
        lines: MutableMap<CartLineKey, CartLine>,
        newLine: CartLine,
    ) {
        val existing = lines[newLine.key]
        lines[newLine.key] = if (existing != null) {
            existing.copy(quantity = existing.quantity + newLine.quantity)
        } else {
            newLine
        }
    }

    @Test
    fun `same configuration merges quantity`() {
        val cart = mutableMapOf<CartLineKey, CartLine>()
        val line1 = makeCartLine(item, sizeM, milkO, sugar, 1)
        val line2 = makeCartLine(item, sizeM, milkO, sugar, 2)
        mergeInto(cart, line1)
        mergeInto(cart, line2)

        assertEquals(1, cart.size)
        assertEquals(3, cart.values.first().quantity)
    }

    @Test
    fun `different size creates separate line`() {
        val cart = mutableMapOf<CartLineKey, CartLine>()
        mergeInto(cart, makeCartLine(item, sizeM, milkO, sugar, 1))
        mergeInto(cart, makeCartLine(item, sizeL, milkO, sugar, 1))

        assertEquals(2, cart.size)
    }

    @Test
    fun `different milk creates separate line`() {
        val cart = mutableMapOf<CartLineKey, CartLine>()
        mergeInto(cart, makeCartLine(item, sizeM, milkO, sugar, 1))
        mergeInto(cart, makeCartLine(item, sizeM, milkA, sugar, 1))

        assertEquals(2, cart.size)
    }

    @Test
    fun `decrement to zero removes line`() {
        val cart = mutableMapOf<CartLineKey, CartLine>()
        val line = makeCartLine(item, sizeM, milkO, sugar, 1)
        mergeInto(cart, line)

        val existing = cart[line.key]!!
        val newQty = existing.quantity - 1
        if (newQty <= 0) cart.remove(line.key) else cart[line.key] = existing.copy(quantity = newQty)

        assertTrue(cart.isEmpty())
    }
}

class TicketIdTest {

    @Test
    fun `ticket id matches regex`() {
        repeat(20) {
            val id = TicketIdGenerator.generateTicketId()
            assertTrue(
                "Expected #BK-XXXXX format, got: $id",
                id.matches(Regex("^#BK-\\d{5}$"))
            )
        }
    }
}

class FormatPriceTest {

    @Test
    fun `formatPrice renders dollar symbol and two decimals`() {
        assertEquals("\$12.65", PriceCalculator.formatPriceAsDollars(1265L, "$"))
        assertEquals("\$0.75", PriceCalculator.formatPriceAsDollars(75L, "$"))
    }
}

class DollarsToCentsTest {

    @Test
    fun `converts floating-point dollar to exact cents`() {
        assertEquals(250L, PriceCalculator.dollarsToCents(2.50))
        assertEquals(40L, PriceCalculator.dollarsToCents(0.40))
        assertEquals(399L, PriceCalculator.dollarsToCents(3.99))
    }
}
