package com.app.orderfood.model

data class Customer(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val city: String = "",
    val district : String = "",
    val fullAddress : String = "",
    val registerDate : Long = 0,
    val balance : Double = 0.0
    )