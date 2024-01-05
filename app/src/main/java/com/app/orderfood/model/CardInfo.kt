package com.app.orderfood.model

data class CardInfo(
    val userId : String = "",
    val cardNo : String = "",
    val cardName : String = "",
    val cardCvc : String = "",
    val cardExp : String = ""
) {
}