package com.app.orderfood.activities.customer

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.orderfood.adapter.customer.RestaurantAdapter
import com.app.orderfood.databinding.ActivitySearchProductBinding
import com.app.orderfood.model.Seller
import com.app.orderfood.viewmodel.SellerViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel


class SearchProductActivity : AppCompatActivity(),RestaurantAdapter.RestaurantClick {

    private lateinit var binding : ActivitySearchProductBinding
    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var restaurantList : ArrayList<Seller>
    private val sellerViewModel by viewModel<SellerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {finish()}

        restaurantList = ArrayList()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)

        binding.searchEt.requestFocus()

        binding.searchEt.addTextChangedListener(textWatcher)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val et = s.toString().trim()
            if (et.length > 1) {
                val capitalizedText = et.substring(0, 1).toUpperCase() + et.substring(1)
                searchSellers(capitalizedText)
            }

        }

        override fun afterTextChanged(s: Editable?) {
            restaurantList.clear()

        }
    }

    //restoran adına göre ara, eğer sonuç gelmezse mutfak türüne göre ara (searchSellerByCategory())
    private fun searchSellers(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = FirebaseFirestore.getInstance()
            val usersRef = db.collection("Sellers")

            val querySnapshot = usersRef.whereGreaterThanOrEqualTo("shopName", query)
                .whereLessThanOrEqualTo("shopName", query + "\uf8ff")
                .get()
                .await()

            val tempList = ArrayList<Seller>()

            for (document in querySnapshot.documents) {
                val seller = document.toObject(Seller::class.java)
                if (seller != null) {
                    tempList.add(seller)
                }
            }

            withContext(Dispatchers.Main) {
                restaurantList = tempList

                if (restaurantList.isNotEmpty()) {
                    restaurantAdapter = RestaurantAdapter(restaurantList, this@SearchProductActivity, sellerViewModel,this@SearchProductActivity)
                    binding.recyclerView.adapter = restaurantAdapter
                    restaurantAdapter.notifyDataSetChanged()
                } else {
                    searchSellerByCategory(query)
                }
            }
        }
    }
    private suspend fun searchSellerByCategory(query: String) {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("Sellers")

        val querySnapshot = usersRef.whereGreaterThanOrEqualTo("category", query)
            .whereLessThanOrEqualTo("category", query + "\uf8ff")
            .get()
            .await()

        val tempList = ArrayList<Seller>() // Geçici liste oluştur

        for (document in querySnapshot.documents) {
            val seller = document.toObject(Seller::class.java)
            if (seller != null) {
                tempList.add(seller)
            }
        }

        withContext(Dispatchers.Main) {
            restaurantList = tempList

            restaurantAdapter = RestaurantAdapter(restaurantList, this@SearchProductActivity, sellerViewModel,this@SearchProductActivity)
            binding.recyclerView.adapter = restaurantAdapter
            restaurantAdapter.notifyDataSetChanged()
        }
    }

    override fun clickRestaurant(seller: Seller) {
        val intent = Intent(this,RestaurantDetailsActivity::class.java)
        intent.putExtra("sellerId",seller.userId)
        startActivity(intent)
    }


}