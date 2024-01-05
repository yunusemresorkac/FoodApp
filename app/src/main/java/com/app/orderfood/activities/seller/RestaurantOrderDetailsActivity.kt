package com.app.orderfood.activities.seller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.app.orderfood.R
import com.app.orderfood.activities.customer.FoodDetailsActivity
import com.app.orderfood.adapter.customer.OrderDetailAdapter
import com.app.orderfood.databinding.ActivityRestaurantOrderDetailsBinding
import com.app.orderfood.model.Order
import com.app.orderfood.notify.Data
import com.app.orderfood.notify.Sender
import com.app.orderfood.notify.Token
import com.app.orderfood.util.Constants
import com.app.orderfood.util.DummyMethods
import com.app.orderfood.viewmodel.CustomerViewModel
import com.app.orderfood.viewmodel.OrderViewModel
import com.app.orderfood.viewmodel.SellerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle

class RestaurantOrderDetailsActivity : AppCompatActivity(),OrderDetailAdapter.OrderDetailClick {


    private lateinit var binding : ActivityRestaurantOrderDetailsBinding
    private lateinit var orderId : String
    private lateinit var orderAdapter: OrderDetailAdapter
    private lateinit var orderList : ArrayList<Order>
    private lateinit var firebaseUser : FirebaseUser
    private val sellerViewModel by viewModel<SellerViewModel>()
    private val customerViewModel by viewModel<CustomerViewModel>()

    private lateinit var userId : String
    private var requestQueue: RequestQueue? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {finish()}

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        requestQueue = Volley.newRequestQueue(this)


        orderId = intent.getStringExtra("restaurantOrderId")!!


        initRecycler()
        getOrders()
        getOrderById()

        binding.completeBtn.setOnClickListener {
            completeOrder()
        }
        binding.cancelBtn.setOnClickListener {
            cancelOrder()
        }
        binding.deleteBtn.setOnClickListener {
            deleteOrder()
        }

    }

    private fun sendNotification(receiver: String, message: String, title: String, sender: String) {

        val db = FirebaseFirestore.getInstance()
        val tokensCollection = db.collection("Tokens")
        val query = tokensCollection.whereEqualTo(FieldPath.documentId(), receiver)

        query.get().addOnSuccessListener { querySnapshot ->
            for (documentSnapshot in querySnapshot) {
                val token = documentSnapshot.toObject(Token::class.java).token

                val data = Data(
                    sender,
                    message,
                    title,
                    receiver,
                    R.mipmap.ic_launcher_round
                )
                val senderData = Sender(token,data)

                try {
                    val senderJsonObj = JSONObject(Gson().toJson(senderData))
                    val jsonObjectRequest = object : JsonObjectRequest(
                        "https://fcm.googleapis.com/fcm/send",
                        senderJsonObj,
                        Response.Listener { response ->
                            Log.d("JSON_RESPONSE 0", "onResponseSuccess: $response")
                        },
                        Response.ErrorListener { error ->
                            Log.d("JSON_RESPONSE 1", "onResponseError: $error")
                        }) {
                        @Throws(AuthFailureError::class)
                        override fun getHeaders(): Map<String, String> {
                            val headers = HashMap<String, String>()

                            headers["Content-Type"] = "application/json"
                            headers["Authorization"] = "key=${Constants.FCM_KEY}"
                            return headers
                        }
                    }
                    requestQueue!!.add(jsonObjectRequest)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }.addOnFailureListener { error ->
            Log.d("QUERY_ERROR", "Failed to query tokens collection: $error")
        }
    }




    private fun initRecycler(){
        orderList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        orderAdapter = OrderDetailAdapter(orderList,this,this)
        binding.recyclerView.adapter = orderAdapter
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)

    }

    private fun getOrders(){
        lifecycleScope.launch {
            sellerViewModel.getAllRestaurantOrdersById(firebaseUser.uid,orderId).collect { orders ->
                orderList.clear()
                if (orders.isNotEmpty()) {
                    orderList.addAll(orders)
                }

                orderAdapter.notifyDataSetChanged()

            }
        }
    }




    private fun getOrderById(){
        sellerViewModel.getRestaurantOrderById(orderId,firebaseUser.uid){ order ->
            order?.let {
                userId = order.userId
                println("order bilgi ${order.toString()}")
                binding.time.text = "${DummyMethods.convertTime(order.time.toLong())}"
                binding.totalPrice.text = "Toplam Tutar: ₺${order.totalPrice}"
                when (order.status){
                    Constants.COMPLETED_ORDER -> {
                        binding.statusImage.setImageResource(R.drawable.checked_tick)
                        binding.statusText.text =  "Sipariş Tamamlandı"
                        binding.statusText.setTextColor(resources.getColor(R.color.green))
                    }
                    Constants.CANCELLED_ORDER -> {
                        binding.statusImage.setImageResource(R.drawable.baseline_cancel_24)
                        binding.statusText.text =  "Sipariş İptal Edildi"
                        binding.statusText.setTextColor(resources.getColor(R.color.error))
                    }
                    Constants.PREPARING_ORDER -> {
                        binding.statusImage.setImageResource(R.drawable.status_wait)
                        binding.statusText.text =  "Sipariş Hazırlanıyor"
                        binding.statusText.setTextColor(resources.getColor(R.color.orange_700))
                    }
                }
            }
            customerViewModel.getCustomer(this,order!!.userId){ customer ->
                customer?.let {
                    binding.address.text = customer.fullAddress + "\n" + customer.district
                }
            }

        }



    }



    private fun completeOrder(){
        FirebaseFirestore.getInstance().collection("OurOrders").document(firebaseUser.uid)
            .collection("OurOrders").document(orderId)
            .update("status",Constants.COMPLETED_ORDER)
            .addOnSuccessListener {
                FirebaseFirestore.getInstance().collection("MyOrders").document(userId)
                    .collection("MyOrders").document(orderId)
                    .update("status",Constants.COMPLETED_ORDER).addOnSuccessListener {
                        sellerViewModel.getSeller(this,firebaseUser.uid){ seller ->
                            seller?.let {
                                sendNotification(userId,"${seller.shopName} adresinden verdiğiniz sipariş kapınızda!","Siparişiniz Tamamlandı"
                                ,firebaseUser.uid)
                            }
                        }
                        DummyMethods.showMotionToast(this,"Sipariş Tamamlandı Olarak İşaretlendi","",MotionToastStyle.SUCCESS)
                    }
            }
    }

    private fun cancelOrder(){
        FirebaseFirestore.getInstance().collection("OurOrders").document(firebaseUser.uid)
            .collection("OurOrders").document(orderId)
            .update("status",Constants.CANCELLED_ORDER)
            .addOnSuccessListener {
                FirebaseFirestore.getInstance().collection("MyOrders").document(userId)
                    .collection("MyOrders").document(orderId)
                    .update("status",Constants.CANCELLED_ORDER).addOnSuccessListener {
                        DummyMethods.showMotionToast(this,"Sipariş İptal Edildi Olarak İşaretlendi","",MotionToastStyle.ERROR)

                    }
            }
    }

    private fun deleteOrder(){
        FirebaseFirestore.getInstance().collection("OurOrders").document(firebaseUser.uid)
            .collection("OurOrders").document(orderId)
            .delete()
            .addOnSuccessListener {
                DummyMethods.showMotionToast(this,"Sipariş Tamamen Silindi","",MotionToastStyle.ERROR)
                finish()
            }
    }

    override fun clickDetail(order: Order) {
        val intent = Intent(this, EditProductActivity::class.java)
        intent.putExtra("productId",order.food.id)
        intent.putExtra("sellerId",order.food.sellerId)
        startActivity(intent)
    }

}