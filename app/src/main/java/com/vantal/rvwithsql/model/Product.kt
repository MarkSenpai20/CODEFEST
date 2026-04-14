package com.vantal.rvwithsql.model

// Product.kt
data class Product(
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val imagePath: String   // e.g., "drawable/ic_phone" or "ic_phone"
)