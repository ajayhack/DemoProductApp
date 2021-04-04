package com.android.ajay.demoproductapp.modal

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * =========Written By Ajay Thakur (4th April 2021)===========
 */

@Entity
data class ProductTable(
    @PrimaryKey
    var productID: String = "",
    var productName: String = "",
    var description: String = "",
    var regularPrice: String = "",
    var salePrice: String = "",
    var productImagePath: String = "",
    var colors: String = "",
    var stores: String = "",
    var colorsName: String = "",
    var storesName: String = "",
) : Serializable

