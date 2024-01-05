package com.app.orderfood.repo

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.app.orderfood.model.Customer
import com.app.orderfood.model.Food
import com.app.orderfood.model.Order
import com.app.orderfood.model.Seller
import com.app.orderfood.util.DummyMethods
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import www.sanju.motiontoast.MotionToastStyle

class SellerRepo {

    private val db = FirebaseFirestore.getInstance()

    //satıcı objesi döndür
    fun getSeller(context : Context, userId: String, callback: (Seller?) -> Unit) {

        val docRef = db.collection("Sellers").document(userId)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val seller = documentSnapshot.toObject(Seller::class.java)
                    if (seller != null) {
                        callback(seller)
                    }
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }


    //yemek ekle
    fun addFood(title : String, description : String, category : String,id : String, sellerId : String, status : Int, imageUrl : String,price: Double,
                discountPercentage : Double,stock : Int, context: Context, openNewFragment : () -> Unit
   ){
        CoroutineScope(Dispatchers.IO).launch {
            val food = Food(title, description, category, id, sellerId,status, imageUrl, price, discountPercentage,stock)
            try {
                db.collection("Foods").document(sellerId)
                    .collection("Foods").document(id)
                    .set(food)
                    .addOnSuccessListener {
                        DummyMethods.showMotionToast(context,"Ürün Eklendi","Ürünü daha sonra düzenleyebilirsiniz",
                            MotionToastStyle.SUCCESS)
                        openNewFragment()
                    }.addOnFailureListener {
                        DummyMethods.showMotionToast(context,"Ürün eklenirken bir hata oldu","Lütfen tekrar deneyiniz",
                            MotionToastStyle.ERROR)
                        openNewFragment()

                    }

            } catch (e: Exception) {
            }
        }

    }

    fun updateLocation(context: Context,userId: String, city : String, district : String,fullAddress : String
    ,latitude : Double, longitude : Double){
        CoroutineScope(Dispatchers.IO).launch {
            val map : HashMap<String,Any> = HashMap()
            map["shopCity"] = city
            map["shopDistrict"] = district
            map["fullAddress"] = fullAddress
            map["latitude"] = latitude
            map["longitude"] = longitude
            db.collection("Sellers").document(userId)
                .update(map)
                .addOnSuccessListener {
                    DummyMethods.showMotionToast(context,"Konum bilginiz güncellendi","",MotionToastStyle.SUCCESS)
                }

        }
    }


    /**get
      */

    fun getFoodById(id : String, sellerId: String, callback: (Food?) -> Unit){
        val docRef = db.collection("Foods").document(sellerId).collection("Foods").document(id)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val food = documentSnapshot.toObject(Food::class.java)
                    if (food != null) {
                        callback(food)
                    }
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }


    fun getRestaurantOrderById(id: String, userId : String, callback: (Order?) -> Unit){
        val docRef = db.collection("OurOrders").document(userId)
            .collection("OurOrders").document(id)
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


    private fun getFoods(sellerId: String, status: Int?): Flow<List<Food>> = callbackFlow {
        val reference = FirebaseFirestore.getInstance().collection("Foods")
            .document(sellerId).collection("Foods")

        if (status != null) {
            reference.whereEqualTo("status", status)
        }

        val listener = reference.orderBy("title", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }

                val foodList = ArrayList<Food>()

                querySnapshot?.forEach { document ->
                    val food = document.toObject(Food::class.java)
                    food.let {
                        foodList.add(it)
                    }
                }

                trySend(foodList)
            }

        awaitClose { listener.remove() }
    }


    fun getAllFoods(sellerId: String, status: Int?): Flow<List<Food>> = getFoods(sellerId,status).flowOn(Dispatchers.IO).onStart {
        emit(emptyList()) // Optional: Emit an empty list while fetching the data
    }.catch { exception ->
        // Handle any errors here
        // For instance, emit an empty list or log the error

        emit(emptyList())
    }



    private fun getRestaurantOrders(userId: String): Flow<List<Order>> = callbackFlow {
        val reference = db.collection("OurOrders").document(userId).collection("OurOrders")
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

    fun getAllRestaurantOrders(userId: String): Flow<List<Order>> = getRestaurantOrders(userId).flowOn(Dispatchers.IO).onStart {
        emit(emptyList()) // Optional: Emit an empty list while fetching the data
    }.catch { exception ->
        // Handle any errors here
        // For instance, emit an empty list or log the error

        emit(emptyList())
    }


    private fun getRestaurantOrderItemsById(userId: String, orderId: String): Flow<List<Order>> = callbackFlow {
        val reference = db.collection("OurOrders")
            .document(userId)
            .collection("OurOrders")
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


    fun getAllRestaurantOrdersById(userId: String, id : String): Flow<List<Order>> = getRestaurantOrderItemsById(userId,id).flowOn(Dispatchers.IO).onStart {
        emit(emptyList()) // Optional: Emit an empty list while fetching the data
    }.catch { exception ->
        // Handle any errors here
        // For instance, emit an empty list or log the error

        emit(emptyList())
    }






}