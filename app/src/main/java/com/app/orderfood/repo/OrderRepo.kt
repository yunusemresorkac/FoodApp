package com.app.orderfood.repo

import com.app.orderfood.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart

class OrderRepo {

    private val db = FirebaseFirestore.getInstance()


    fun getOrderById(id: String, userId : String, callback: (Order?) -> Unit){
        val docRef = db.collection("MyOrders").document(userId)
            .collection("MyOrders").document(id)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val order = documentSnapshot.toObject(Order::class.java)
                    if (order != null) {
                        callback(order)
                    }
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }


    private fun getOrders(userId: String): Flow<List<Order>> = callbackFlow {
        val reference = db.collection("MyOrders").document(userId).collection("MyOrders")
            .orderBy("time",Query.Direction.DESCENDING)
        val listener = reference.addSnapshotListener { querySnapshot, exception ->
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }

            val orderList = ArrayList<Order>()


            querySnapshot?.forEach { document ->
                val order = document.toObject(Order::class.java)
                if (order!=null){
                    orderList.add(order)
                }
            }

            trySend(orderList)
        }

        awaitClose { listener.remove() }
    }

    fun getAllOrders(userId: String): Flow<List<Order>> = getOrders(userId).flowOn(Dispatchers.IO).onStart {
        emit(emptyList()) // Optional: Emit an empty list while fetching the data
    }.catch { exception ->
        // Handle any errors here
        // For instance, emit an empty list or log the error

        emit(emptyList())
    }


    private fun getOrderItemsById(userId: String, orderId: String): Flow<List<Order>> = callbackFlow {
        val reference = db.collection("MyOrders")
            .document(userId)
            .collection("MyOrders")
            .document(orderId)
            .collection("OrderItems")

        val listener = reference.addSnapshotListener { querySnapshot, exception ->
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }

            val orderItemList = ArrayList<Order>()

            querySnapshot?.forEach { document ->
                val orderItem = document.toObject(Order::class.java)
                orderItemList.add(orderItem)
                println("listem orders $orderItemList")
            }

            trySend(orderItemList)
        }

        awaitClose { listener.remove() }
    }


    fun getAllOrdersById(userId: String, id : String): Flow<List<Order>> = getOrderItemsById(userId,id).flowOn(Dispatchers.IO).onStart {
        emit(emptyList()) // Optional: Emit an empty list while fetching the data
    }.catch { exception ->
        // Handle any errors here
        // For instance, emit an empty list or log the error

        emit(emptyList())
    }

}