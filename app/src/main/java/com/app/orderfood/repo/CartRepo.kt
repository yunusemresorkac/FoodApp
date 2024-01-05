package com.app.orderfood.repo

import android.content.Context
import android.widget.Toast
import com.app.orderfood.model.Cart
import com.app.orderfood.model.Food
import com.app.orderfood.util.DummyMethods
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToastStyle

class CartRepo {

    private val db = FirebaseFirestore.getInstance()

    //sepetteki ürün adetini artır
    fun increaseQuantity(userId: String, id : String,quantity : Int,food : Food,context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            if (food.stock < 10){
                if (quantity < food.stock){
                    try {
                        val map : HashMap<String,Any> = HashMap()
                        map["quantity"] = quantity + 1
                        db.collection("Carts").document(userId)
                            .collection("Carts").document(id)
                            .update(map)
                            .addOnSuccessListener {

                            }.addOnFailureListener {

                            }

                    } catch (e: Exception) {
                    }
                }else{
                    withContext(Dispatchers.Main){
                        Toast.makeText(context,"Stok Mevcut Değil", Toast.LENGTH_SHORT).show()
                    }

                }

            }

        }
    }

    //adet düşür
    fun decreaseQuantity(userId: String, id : String,quantity : Int){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val map : HashMap<String,Any> = HashMap()
                if (quantity > 1){
                    map["quantity"] = quantity - 1

                }
                db.collection("Carts").document(userId)
                    .collection("Carts").document(id)
                    .update(map)
                    .addOnSuccessListener {

                    }.addOnFailureListener {


                    }

            } catch (e: Exception) {
            }
        }
    }

    //ürünü sil
    fun deleteCartItem(userId: String, id : String){
        CoroutineScope(Dispatchers.IO).launch {
            try {

                db.collection("Carts").document(userId)
                    .collection("Carts").document(id)
                    .delete()
                    .addOnSuccessListener {
                    }.addOnFailureListener {
                    }

            } catch (e: Exception) {

            }
        }
    }

    //tüm sepeti sil
    fun deleteAllCart(userId: String){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.collection("Carts").document(userId)
                    .delete()
                    .addOnSuccessListener {
                        println("sepet silindi")

                    }.addOnFailureListener {
                        println("hata ${it.message}")


                    }

            } catch (e: Exception) {
                println("hata ${e.message}")

            }
        }
    }




    private fun getCart(userId: String): Flow<List<Cart>> = callbackFlow {
        val reference = FirebaseFirestore.getInstance().collection("Carts")
            .document(userId).collection("Carts")

        val listener = reference.addSnapshotListener { querySnapshot, exception ->
            if (exception != null) {
                close(exception)
                return@addSnapshotListener
            }

            val cartList = ArrayList<Cart>()


            querySnapshot?.forEach { document ->
                val cart = document.toObject(Cart::class.java)
                if (cart!=null){
                    cartList.add(cart)
                }
            }

            trySend(cartList)
        }

        awaitClose { listener.remove() }
    }

    fun getAllCart(userId: String): Flow<List<Cart>> = getCart(userId).flowOn(Dispatchers.IO).onStart {
        emit(emptyList()) // Optional: Emit an empty list while fetching the data
    }.catch { exception ->
        // Handle any errors here
        // For instance, emit an empty list or log the error

        emit(emptyList())
    }



}