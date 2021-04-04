package com.android.ajay.demoproductapp.modal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * =========Written By Ajay Thakur (4th April 2021)==========
 */

@Dao
interface AppDao {

    //region================================Product Data Table Manipulation:-
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductData(product: ProductTable): Long?

    @Query("SELECT * FROM ProductTable")
    fun getAllProductData(): MutableList<ProductTable?>?

    @Query("DELETE FROM ProductTable WHERE productID = :id")
    fun deleteProductByID(id: String)

}