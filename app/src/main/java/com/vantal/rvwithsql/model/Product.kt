package com.vantal.rvwithsql.model
data class Product(
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val imagePath: String
)