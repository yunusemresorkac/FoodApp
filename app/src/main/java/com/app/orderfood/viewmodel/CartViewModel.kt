package com.app.orderfood.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.app.orderfood.model.Cart
import com.app.orderfood.model.Food
import com.app.orderfood.repo.CartRepo
import kotlinx.coroutines.flow.Flow

class CartViewModel (application: Application) : AndroidViewModel(application) {

    private val repo : CartRepo = CartRepo()

    fun increaseQuantity(userId: String, id : String,quantity : Int,food :Food,context: Context){
        repo.increaseQuantity(userId,id,quantity,food,context)
    }

    fun decreaseQuantity(userId: String, id : String,quantity : Int){
        repo.decreaseQuantity(userId, id,quantity)
    }

    fun deleteCartItem(userId: String, id : String){
        repo.deleteCartItem(userId, id)
    }

    fun deleteAllCart(userId: String){
        repo.deleteAllCart(userId)
    }





    fun getAllCart(userId: String): Flow<List<Cart>> {
        return repo.getAllCart(userId)
    }
}