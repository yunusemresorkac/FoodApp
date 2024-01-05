package com.app.orderfood.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.app.orderfood.model.Food
import com.app.orderfood.model.Order
import com.app.orderfood.model.Seller
import com.app.orderfood.repo.SellerRepo
import kotlinx.coroutines.flow.Flow

class SellerViewModel  (application: Application) : AndroidViewModel(application) {

    private val repo : SellerRepo = SellerRepo()


    fun getSeller(context : Context, userId: String, callback: (Seller?) -> Unit) {
        repo.getSeller(context,userId, callback)
    }


    fun addFood(title : String, description : String, category : String,id : String, sellerId : String, status : Int, imageUrl : String,price: Double,
                discountPercentage : Double
                ,stock : Int, context: Context, openNewFragment : () -> Unit){
        repo.addFood(title, description, category, id,sellerId, status, imageUrl, price, discountPercentage,stock  ,context
        ,openNewFragment)
    }


    fun updateLocation(context: Context,userId: String, city : String, district : String,fullAddress :String
    ,latitude : Double, longitude : Double){
        repo.updateLocation(context, userId, city, district,fullAddress,latitude, longitude)
    }


    fun getFoodById(id : String, sellerId: String,callback: (Food?) -> Unit){
        repo.getFoodById(id,sellerId,callback)
    }

    fun getRestaurantOrderById(id: String, userId : String, callback: (Order?) -> Unit){
        repo.getRestaurantOrderById(id, userId, callback)
    }



    fun getAllFoods(sellerId: String,status: Int?): Flow<List<Food>> {
        return repo.getAllFoods(sellerId,status)
    }

    fun getAllRestaurantOrders(userId: String): Flow<List<Order>> {
        return repo.getAllRestaurantOrders(userId)
    }

    fun getAllRestaurantOrdersById(userId: String, id : String): Flow<List<Order>> {
        return repo.getAllRestaurantOrdersById(userId, id)
    }



}