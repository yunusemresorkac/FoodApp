package com.app.orderfood.activities.customer

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.RatingBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.orderfood.R
import com.app.orderfood.adapter.customer.OrderDetailAdapter
import com.app.orderfood.databinding.ActivityOrderDetailsBinding
import com.app.orderfood.model.Order
import com.app.orderfood.util.Constants
import com.app.orderfood.util.DummyMethods
import com.app.orderfood.viewmodel.OrderViewModel
import com.app.orderfood.viewmodel.SellerViewModel
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle

class OrderDetailsActivity : AppCompatActivity(),OrderDetailAdapter.OrderDetailClick {

    private lateinit var binding : ActivityOrderDetailsBinding
    private val orderViewModel by viewModel<OrderViewModel>()
    private lateinit var orderId : String
    private lateinit var orderAdapter: OrderDetailAdapter
    private lateinit var orderList : ArrayList<Order>
    private lateinit var firebaseUser : FirebaseUser
    private val sellerViewModel by viewModel<SellerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {finish()}

        firebaseUser = FirebaseAuth.getInstance().currentUser!!


        orderId = intent.getStringExtra("orderId")!!

        getOrderById()

        initRecycler()
        getOrders()

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

    //siparişin içindeki farklı ürünleri(yemekleri) listele
    private fun getOrders(){
        lifecycleScope.launch {
            orderViewModel.getAllOrdersById(firebaseUser.uid,orderId).collect { orders ->
                orderList.clear()
                if (orders.isNotEmpty()) {
                    orderList.addAll(orders)
                }

                orderAdapter.notifyDataSetChanged()
//                if (chatList.size > 0){
//                    binding.chatsLay.visibility = View.VISIBLE
//                } else {
//                    binding.chatsLay.visibility = View.GONE
//                }
            }
        }
    }





    //id değerine göre siparişin genel bilgilerini al
    private fun getOrderById(){
        orderViewModel.getOrderById(orderId,firebaseUser.uid){ order ->
            order?.let {
                binding.time.text = "${DummyMethods.convertTime(order.time.toLong())}"
                binding.totalPrice.text = "Toplam Tutar: ₺${order.totalPrice}"
                when (order.status){
                    Constants.COMPLETED_ORDER -> {
                        binding.statusImage.setImageResource(R.drawable.checked_tick)
                        binding.statusText.text =  "Sipariş Tamamlandı. \nDeğerlendirmek İçin Tıkla."
                        binding.statusText.setTextColor(resources.getColor(R.color.green))
                        binding.statusText.setOnClickListener {
                            showRateDialog(order)
                        }
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
                getSellerData(order)

            }
        }
    }

    //restoran bilgilerini al
    private fun getSellerData( order: Order){

        sellerViewModel.getSeller(this,order.sellerId){ restaurant ->
            restaurant?.let {
                binding.shopName.text = restaurant.shopName
                Glide.with(this).load(restaurant.sellerImage).into(binding.shopImage)

                binding.shopImage.setOnClickListener{
                    val intent = Intent(this,RestaurantDetailsActivity::class.java)
                    intent.putExtra("sellerId",restaurant.userId)
                    startActivity(intent)
                }
            }
        }

    }

    //değerlendirme penceresini aç.
    private fun showRateDialog(order: Order){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_rate)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()
        val rateBar = dialog.findViewById<RatingBar>(R.id.ratingBar)
        val sendRateBtn = dialog.findViewById<MaterialButton>(R.id.sendRateBtn)


        checkIfRated(firebaseUser.uid,orderId){exists ->
            if (exists){
                sendRateBtn.isEnabled = false
                sendRateBtn.text = "Zaten Değerlendirdiniz."
                getOldRate(firebaseUser.uid,orderId){ rate ->
                    rateBar.rating = rate.toFloat()
                    rateBar.isEnabled = false
                }
            }else{
                sendRateBtn.isEnabled = true
                rateBar.rating = 0f
            }
        }

        sendRateBtn.setOnClickListener {
            val rating =rateBar.rating.toDouble()
            updateSellerRate(order,rating)
            dialog.dismiss()

        }


    }

    private fun addMeToSellerRaters(order: Order,rate : Double){
        val map : HashMap<String,Any> = HashMap()
        map["userId"] = firebaseUser.uid
        map["sellerId"] = order.sellerId
        map["orderId"] = orderId
        map["rate"] = rate
        FirebaseFirestore.getInstance().collection("Rates").document(firebaseUser.uid)
            .collection("Rates").document(orderId)
            .set(map)
    }

    private fun updateSellerRate(order: Order, rating : Double){
        sellerViewModel.getSeller(this,order.sellerId){ seller ->
            seller?.let {
                val map : HashMap<String,Any> = HashMap()
                map["numberOfRates"] = seller.numberOfRates + 1
                map["rate"] = seller.rate + rating
                FirebaseFirestore.getInstance().collection("Sellers").document(order.sellerId)
                    .update(map).addOnSuccessListener {
                        addMeToSellerRaters(order,rating)
                        DummyMethods.showMotionToast(this,"Değerlendirmeniz Alındı","",MotionToastStyle.SUCCESS)
                    }

            }


        }
    }

    //kullanıcı daha önceden değerlendirmiş mi kontrol et
    private fun checkIfRated(userId: String, orderId: String, completion: (Boolean) -> Unit) {
        val ratesRef = FirebaseFirestore.getInstance().collection("Rates")
            .document(userId)
            .collection("Rates")
            .document(orderId)

        ratesRef.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    // Belge varsa true döner, yoksa false
                    val exists = document.exists()
                    completion(exists)
                } else {
                    // Hata durumunda false döner
                    completion(false)
                }
            }
    }

    //değerlendirdiyse puanı al
    private fun getOldRate(userId: String, orderId: String, completion: (Double) -> Unit) {
        val ratesRef = FirebaseFirestore.getInstance().collection("Rates")
            .document(userId)
            .collection("Rates")
            .document(orderId)

        ratesRef.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    // Belge varsa true döner, yoksa false
                    val exists = document.exists()
                    if (exists){
                        val rate = document.getDouble("rate")
                        completion(rate!!)
                    }
                } else {
                    // Hata durumunda false döner
                    completion(0.0)
                }
            }
    }

    //yemek detay sayfasına git
    override fun clickDetail(order: Order) {
        val intent = Intent(this,FoodDetailsActivity::class.java)
        intent.putExtra("foodId",order.food.id)
        intent.putExtra("foodSellerId",order.food.sellerId)
        startActivity(intent)
    }


}