package com.app.orderfood.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.app.orderfood.model.Order
import com.app.orderfood.repo.OrderRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart

class OrderViewModel (application: Application) : AndroidViewModel(application) {

    private val repo : OrderRepo = OrderRepo()

    fun getOrderById(id: String, userId : String, callback: (Order?) -> Unit){
        repo.getOrderById(id, userId, callback)
    }

    fun getAllOrders(userId: String): Flow<List<Order>> {
        return repo.getAllOrders(userId)
    }

    fun getAllOrdersById(userId: String, id : String): Flow<List<Order>> {
        return repo.getAllOrdersById(userId, id)
    }



}