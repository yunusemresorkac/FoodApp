package com.app.orderfood.activities.customer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.orderfood.activities.seller.MapsActivity
import com.app.orderfood.adapter.customer.FoodAdapter
import com.app.orderfood.databinding.ActivityRestaurantDetailsBinding
import com.app.orderfood.model.Food
import com.app.orderfood.permissions.PermissionManager
import com.app.orderfood.util.Constants
import com.app.orderfood.util.DummyMethods
import com.app.orderfood.viewmodel.SellerViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RestaurantDetailsActivity : AppCompatActivity(), FoodAdapter.FoodClick{

    private lateinit var binding : ActivityRestaurantDetailsBinding
    private lateinit var sellerId : String
    private lateinit var foodAdapter: FoodAdapter
    private lateinit var foodList : ArrayList<Food>
    private val sellerViewModel by viewModel<SellerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {finish()}



        sellerId = intent.getStringExtra("sellerId")!!

        initRecycler()
        getSellerInfo()
        getFoods()


    }

    private fun initRecycler(){
        foodList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        foodAdapter = FoodAdapter(foodList,this@RestaurantDetailsActivity,this)
        binding.recyclerView.adapter = foodAdapter
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)
    }




    private fun getFoods(){
        lifecycleScope.launch {
            sellerViewModel.getAllFoods(sellerId,Constants.ACTIVE_PRODUCT).collect { foods ->
                foodList.clear()
                if (foods.isNotEmpty()) {
                    foodList.addAll(foods)
                }

                foodAdapter.notifyDataSetChanged()

            }
        }
    }

    private fun getSellerInfo(){
        sellerViewModel.getSeller(this,sellerId){ seller ->
            seller?.let {
                if (seller.discountStatus == 0){
                    binding.discount.visibility = View.GONE
                }else{
                    binding.discount.visibility = View.VISIBLE
                    binding.discount.text = "İndirim Var"
                }
                binding.toolbar.title = seller.shopName
                Glide.with(this).load(seller.sellerImage).into(binding.sellerImage)
                binding.location.text = "${seller.shopCity} (${seller.shopDistrict})"
                binding.category.text = "•${seller.category}"
                if (seller.numberOfRates == 0){
                    binding.rate.text = "Değerlendirme Yok"
                }else{
                    val rate = seller.rate / seller.numberOfRates
                    binding.rate.text = "${DummyMethods.formatDoubleNumber(rate)} (${seller.numberOfRates})"

                }
                binding.goMapsBtn.setOnClickListener{
                    val permissionManager = PermissionManager()
                    if (permissionManager.hasLocationPermission(this)){
                        val intent = Intent(this,MapsActivity::class.java)
                        intent.putExtra("restaurantLatitude",seller.latitude)
                        intent.putExtra("restaurantLongitude",seller.longitude)
                        intent.putExtra("mapTitle",seller.shopName)
                        startActivity(intent)
                    }else{
                        permissionManager.requestLocationPermission(this)
                    }

                }
            }
        }
    }

    override fun clickFood(food: Food) {
        val intent = Intent(this,FoodDetailsActivity::class.java)
        intent.putExtra("foodId",food.id)
        intent.putExtra("foodSellerId",food.sellerId)
        startActivity(intent)
    }
}