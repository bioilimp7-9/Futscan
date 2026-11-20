package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProductItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM research_products ORDER BY productNumber ASC")
    fun getAllProducts(): Flow<List<ProductItem>>

    @Query("SELECT * FROM research_products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): ProductItem?

    @Query("SELECT COUNT(*) FROM research_products")
    fun getProductCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductItem>)

    @Update
    suspend fun updateProduct(product: ProductItem)

    @Delete
    suspend fun deleteProduct(product: ProductItem)

    @Query("DELETE FROM research_products")
    suspend fun clearAll()
}
