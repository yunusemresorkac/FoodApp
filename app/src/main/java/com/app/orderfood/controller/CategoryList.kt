package com.app.orderfood.controller

class CategoryList {

    companion object{
        fun getCategoryList(): List<String> {
            return listOf(
                "Sokak Lezzetleri",
                "Ev Yemekleri",
                "Döner",
                "Pide - Lahmacun",
                "Pizza",
                "Burger",
                "Pastane",
                "Tatlı"
            )
        }
    }


}