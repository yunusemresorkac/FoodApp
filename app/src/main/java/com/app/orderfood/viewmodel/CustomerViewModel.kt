package com.app.orderfood.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.app.orderfood.model.Customer
import com.app.orderfood.model.Food
import com.app.orderfood.model.Seller
import com.app.orderfood.repo.CustomerRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart

class CustomerViewModel (application: Application) : AndroidViewModel(application) {

    private val repo : CustomerRepo = CustomerRepo()


    fun getCustomer(context : Context, userId: String, callback: (Customer?) -> Unit) {
        repo.getCustomer(context,userId, callback)
    }

    fun updateLocation(context: Context,userId: String, city : String, district : String,fullAddress : String){
        repo.updateLocation(context, userId, city, district,fullAddress)
    }



    fun getAllRestaurants(): Flow<List<Seller>> {
        return repo.getAllRestaurants()
    }

    fun getAllRestaurantsByCategory(category: List<String>):  Flow<List<Seller>> {
        return repo.getAllRestaurantsByCategory(category)
    }

    fun getAllRestaurantsByDistrict(shopDistrict : String):  Flow<List<Seller>> {
        return repo.getAllRestaurantsByDistrict(shopDistrict)
    }

    fun getAllRestaurantsByDiscount(shopDistrict : String):  Flow<List<Seller>> {
        return repo.getAllRestaurantsByDiscount(shopDistrict)
    }


}
