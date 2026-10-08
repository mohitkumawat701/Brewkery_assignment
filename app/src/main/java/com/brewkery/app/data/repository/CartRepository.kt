package com.brewkery.app.data.repository

import com.brewkery.app.domain.model.CartLine
import com.brewkery.app.domain.model.CartLineKey
import com.brewkery.app.domain.model.Order
import com.brewkery.app.domain.model.OrderStatus
import com.brewkery.app.domain.model.StoreMeta
import com.brewkery.app.util.TicketIdGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartRepository {

    private val _cartLines = MutableStateFlow<Map<CartLineKey, CartLine>>(emptyMap())
    val cartLines: StateFlow<Map<CartLineKey, CartLine>> = _cartLines.asStateFlow()

    private val _activeOrder = MutableStateFlow<Order?>(null)
    val activeOrder: StateFlow<Order?> = _activeOrder.asStateFlow()

    fun addToCart(line: CartLine) {
        _cartLines.update { current ->
            val existing = current[line.key]
            if (existing != null) {
                current + (line.key to existing.copy(quantity = existing.quantity + line.quantity))
            } else {
                current + (line.key to line)
            }
        }
    }

    fun updateQuantity(key: CartLineKey, newQuantity: Int) {
        _cartLines.update { current ->
            when {
                newQuantity <= 0 -> current - key
                else -> {
                    val existing = current[key] ?: return@update current
                    current + (key to existing.copy(quantity = newQuantity))
                }
            }
        }
    }

    fun clearCart() {
        _cartLines.value = emptyMap()
    }

    fun placeOrder(meta: StoreMeta): Order {
        val lines = _cartLines.value.values.toList()
        val totalQty = lines.sumOf { it.quantity }
        val ticketId = TicketIdGenerator.generateTicketId()
        val order = Order(
            ticketId = ticketId,
            status = OrderStatus.PREPARING,
            totalItemCount = totalQty,
            estimatedWait = meta.estimatedDeliveryTime,
        )
        clearCart()
        _activeOrder.value = order
        return order
    }
}
