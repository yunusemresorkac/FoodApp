package com.app.orderfood.model

data class Order(
    val food: Food = Food(),
    val status: Int = 0,
    val time: String = "",
    val id: String = "",
    val userId: String = "",
    val sellerId : String = "",
    val totalPrice : Double = 0.0,
    val quantity : Int = 1,
) {

}
