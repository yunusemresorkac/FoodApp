package com.app.orderfood.model

data class Cart(
    val food : Food,
    var quantity : Int
) {
    constructor() : this(Food(), 0)

}