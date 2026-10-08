package com.clickretina.brewkery.network

import com.clickretina.brewkery.model.Category
import com.clickretina.brewkery.model.MenuItem
import com.clickretina.brewkery.model.StoreMeta
import retrofit2.HttpException
import java.io.IOException

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
}

data class MenuData(
    val meta: StoreMeta,
    val categories: List<Category>,
    val items: List<MenuItem>,
)

class MenuRepository(private val api: BrewkeryApi) {

    suspend fun getMenu(): NetworkResult<MenuData> = try {
        val response = api.getMenu()
        val meta = response.meta?.toDomain()
            ?: return NetworkResult.Error("Missing store configuration.")
        val categories = response.categories.map { it.toDomain() }
        val items = response.items.map { it.toDomain() }
        NetworkResult.Success(MenuData(meta, categories, items))
    } catch (e: IOException) {
        NetworkResult.Error("Network error: ${e.message}")
    } catch (e: HttpException) {
        NetworkResult.Error("Server error ${e.code()}: ${e.message()}")
    } catch (e: Exception) {
        NetworkResult.Error("Unexpected error: ${e.message}")
    }

    suspend fun getItem(id: Int): NetworkResult<MenuItem> = try {
        NetworkResult.Success(api.getItem(id).toDomain())
    } catch (e: IOException) {
        NetworkResult.Error("Network error: ${e.message}")
    } catch (e: HttpException) {
        NetworkResult.Error("Server error ${e.code()}: ${e.message()}")
    } catch (e: Exception) {
        NetworkResult.Error("Unexpected error: ${e.message}")
    }
}
