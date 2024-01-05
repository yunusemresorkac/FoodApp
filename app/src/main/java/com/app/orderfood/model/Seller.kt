package com.app.orderfood.model

data class Seller(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val shopName : String = "",
    val category : String = "",
    val rate : Double = 0.0,
    val shopCity : String = "",
    val shopDistrict : String = "",
    val fullAddress : String = "",
    val registerDate : Long = 0,
    val sellerImage : String = "",
    val numberOfRates : Int = 0,
    val discountStatus : Int = 0,
    val latitude : Double = 0.0,
    val longitude : Double = 0.0
) {
}