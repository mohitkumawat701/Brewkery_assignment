package com.brewkery.app.ui.features.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brewkery.app.data.repository.CartRepository
import com.brewkery.app.di.AppContainer
import com.brewkery.app.domain.model.CartLine
import com.brewkery.app.domain.model.CartLineKey
import com.brewkery.app.domain.model.Order
import com.brewkery.app.domain.model.StoreMeta
import com.brewkery.app.domain.pricing.PriceCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    val cartLines: StateFlow<List<CartLine>> = cartRepository.cartLines
        .map { it.values.toList() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeOrder: StateFlow<Order?> = cartRepository.activeOrder

    val cartItemCount: StateFlow<Int> = cartLines
        .map { lines -> lines.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val subtotalCents: StateFlow<Long> = cartLines
        .map { lines -> PriceCalculator.calculateSubtotalCents(lines) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    fun addToCart(line: CartLine) = cartRepository.addToCart(line)

    fun updateQuantity(key: CartLineKey, newQuantity: Int) =
        cartRepository.updateQuantity(key, newQuantity)

    fun clearCart() = cartRepository.clearCart()

    fun placeOrder(meta: StoreMeta): Order = cartRepository.placeOrder(meta)

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CartViewModel(container.cartRepository) as T
            }
    }
}
