package com.app.orderfood.repo

import android.content.Context
import com.app.orderfood.model.Customer
import com.app.orderfood.model.Food
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

class CustomerRepo {

    private val db = FirebaseFirestore.getInstance()

    //customer objesi döndür
    fun getCustomer(context : Context, userId: String, callback: (Customer?) -> Unit) {

        val docRef = db.collection("Customers").document(userId)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val customer = documentSnapshot.toObject(Customer::class.java)
                    if (customer != null) {
                        callback(customer)
                    }
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }


    //konum bilgisini güncelle
    fun updateLocation(context: Context,userId: String, city : String, district : String,fullAddress : String){
        CoroutineScope(Dispatchers.IO).launch {
            val map : HashMap<String,Any> = HashMap()
            map["city"] = city
            map["district"] = district
            map["fullAddress"] = fullAddress
            db.collection("Customers").document(userId)
                .update(map)
                .addOnSuccessListener {
                    DummyMethods.showMotionToast(context,"Konum bilginiz güncellendi","",MotionToastStyle.SUCCESS)
                }

        }
    }





    //ana sayfa için restoranları listele
    private fun getRestaurants(): Flow<List<Seller>> = callbackFlow {
        val reference = FirebaseFirestore.getInstance().collection("Sellers")
            .orderBy("registerDate", Query.Direction.DESCENDING)

        val listener = reference.addSnapshotListener { querySnapshot, exception ->
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }

            val sellerList = ArrayList<Seller>()


            querySnapshot?.forEach { document ->
                val seller = document.toObject(Seller::class.java)
                if (seller!=null){
                    sellerList.add(seller)
                }
            }

            trySend(sellerList)
        }

        awaitClose { listener.remove() }
    }

    fun getAllRestaurants(): Flow<List<Seller>> = getRestaurants().flowOn(Dispatchers.IO).onStart {
        emit(emptyList()) // Optional: Emit an empty list while fetching the data
    }.catch { exception ->
        // Handle any errors here
        // For instance, emit an empty list or log the error

        emit(emptyList())
    }


    //kategoriye göre listele
    private fun getRestaurantsByCategory(category: List<String>): Flow<List<Seller>> = callbackFlow {
        val reference = FirebaseFirestore.getInstance().collection("Sellers")
            .whereIn("category", category)

        val listener = reference.addSnapshotListener { querySnapshot, exception ->
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }

            val sellerList = ArrayList<Seller>()

            querySnapshot?.forEach { document ->
                val seller = document.toObject(Seller::class.java)
                sellerList.add(seller)
                println("seçilen listem $sellerList")
            }

            trySend(sellerList)
        }

        awaitClose { listener.remove() }
    }

    fun getAllRestaurantsByCategory(category: List<String>): Flow<List<Seller>> = getRestaurantsByCategory(category).flowOn(Dispatchers.IO).onStart {
        emit(emptyList()) // Optional: Emit an empty list while fetching the data
    }.catch { exception ->
        // Handle any errors here
        // For instance, emit an empty list or log the error

        emit(emptyList())
    }



    //ilçeye göre listele
    private fun getRestaurantsByDistrict(shopDistrict: String): Flow<List<Seller>> = callbackFlow {
        val reference = FirebaseFirestore.getInstance().collection("Sellers")
            .whereEqualTo("shopDistrict", shopDistrict)

        val listener = reference.addSnapshotListener { querySnapshot, exception ->
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }

            val sellerList = ArrayList<Seller>()

            querySnapshot?.forEach { document ->
                val seller = document.toObject(Seller::class.java)
                sellerList.add(seller)
            }

            trySend(sellerList)
        }

        awaitClose { listener.remove() }
    }

    fun getAllRestaurantsByDistrict(shopDistrict: String): Flow<List<Seller>> = getRestaurantsByDistrict(shopDistrict).flowOn(Dispatchers.IO).onStart {
        emit(emptyList()) // Optional: Emit an empty list while fetching the data
    }.catch { exception ->
        // Handle any errors here
        // For instance, emit an empty list or log the error

        emit(emptyList())
    }


    //indirimli restoranları listele
    private fun getRestaurantsByDiscount(shopDistrict: String): Flow<List<Seller>> = callbackFlow {
        val reference = FirebaseFirestore.getInstance().collection("Sellers")
            .whereEqualTo("shopDistrict", shopDistrict)
            .whereEqualTo("discountStatus",1)

        val listener = reference.addSnapshotListener { querySnapshot, exception ->
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }

            val sellerList = ArrayList<Seller>()

            querySnapshot?.forEach { document ->
                val seller = document.toObject(Seller::class.java)
                sellerList.add(seller)
            }

            trySend(sellerList)
        }

        awaitClose { listener.remove() }
    }

    fun getAllRestaurantsByDiscount(shopDistrict: String): Flow<List<Seller>> = getRestaurantsByDiscount(shopDistrict).flowOn(Dispatchers.IO).onStart {
        emit(emptyList()) // Optional: Emit an empty list while fetching the data
    }.catch { exception ->
        // Handle any errors here
        // For instance, emit an empty list or log the error

        emit(emptyList())
    }



}