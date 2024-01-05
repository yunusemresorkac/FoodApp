package com.app.orderfood.model

data class Food(
    val title:  String = "",
    val description: String = "",
    val category: String = "",
    val id: String = "",
    val sellerId: String = "",
    val status: Int = 0,
    val imageUrl: String = "",
    val price: Double = 0.0,
    val discountPercentage: Double =  0.0,
    val stock : Int = 0
) {
}

