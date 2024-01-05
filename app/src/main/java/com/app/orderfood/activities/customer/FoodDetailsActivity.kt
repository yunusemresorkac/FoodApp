package com.app.orderfood.activities.customer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.app.orderfood.R
import com.app.orderfood.databinding.ActivityFoodDetailsBinding
import com.app.orderfood.model.Cart
import com.app.orderfood.util.DummyMethods
import com.app.orderfood.viewmodel.CustomerViewModel
import com.app.orderfood.viewmodel.SellerViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle

class FoodDetailsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFoodDetailsBinding
    private lateinit var sellerId : String
    private lateinit var foodId : String
    private val sellerViewModel by viewModel<SellerViewModel>()
    private var quantity : Int = 1
    private lateinit var firebaseUser: FirebaseUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {finish()}


        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        sellerId = intent.getStringExtra("foodSellerId")!!
        foodId = intent.getStringExtra("foodId")!!

        initSpinnerForQuantity()
        getFoodById()

    }

    private fun initSpinnerForQuantity(){

        //Adet seçimi için spinner ayarla
        val spinnerData = arrayOf(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerData)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                val selectedOption = spinnerData[position]
                quantity = selectedOption
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
            }
        }
    }

    //id değerine göre ürün al ve verileri göster
    private fun getFoodById(){
        sellerViewModel.getFoodById(foodId,sellerId){ food ->
            food?.let {
                binding.toolbar.title = food.title
                binding.foodDesc.text = food.description
                Glide.with(this).load(food.imageUrl).into(binding.foodImage)
                if (food.stock in 1..9){
                    binding.stock.visibility = View.VISIBLE
                    binding.stock.text = "Sınırlı Stok: ${food.stock} Adet"
                }else{
                    binding.stock.visibility = View.GONE
                }


                val formattedPrice: String
                if (food.discountPercentage > 0) {
                    val discountedPrice = food.price - (food.price * food.discountPercentage / 100)
                    formattedPrice = "₺$discountedPrice"
                    val originalPrice = "₺${food.price}"
                    val spannableString = SpannableString("$formattedPrice $originalPrice")
                    spannableString.setSpan(StrikethroughSpan(), formattedPrice.length + 1, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    binding.foodPrice.text = spannableString
                } else {
                    formattedPrice = "₺${food.price}"
                    binding.foodPrice.text = formattedPrice
                }


                binding.addToCartBtn.setOnClickListener {
                    if (quantity <= food.stock){
                        val cart = Cart(food,quantity)
                        FirebaseFirestore.getInstance().collection("Carts").document(firebaseUser.uid)
                            .collection("Carts").document(foodId).set(cart)
                            .addOnSuccessListener {
                                DummyMethods.showMotionToast(this,"Sepete Eklendi","",MotionToastStyle.SUCCESS)
                                finish()
                            }
                    }else{
                        Toast.makeText(this,"Stok Mevcut Değil",Toast.LENGTH_SHORT).show()
                    }
                }

            }

        }
    }

}