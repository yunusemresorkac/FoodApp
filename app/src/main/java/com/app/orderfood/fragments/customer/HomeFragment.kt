package com.app.orderfood.fragments.customer

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.orderfood.R
import com.app.orderfood.activities.customer.RestaurantDetailsActivity
import com.app.orderfood.activities.customer.SearchProductActivity
import com.app.orderfood.adapter.customer.RestaurantAdapter
import com.app.orderfood.databinding.FragmentHomeBinding
import com.app.orderfood.model.Seller
import com.app.orderfood.viewmodel.CustomerViewModel
import com.app.orderfood.viewmodel.SellerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import org.checkerframework.checker.nullness.qual.NonNull
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeFragment : Fragment(), RestaurantAdapter.RestaurantClick{

    private lateinit var binding : FragmentHomeBinding
    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var restaurantList : ArrayList<Seller>
    private val customerViewModel by viewModel<CustomerViewModel>()
    private val sellerViewModel by viewModel<SellerViewModel>()
    private lateinit var firebaseUser : FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        initRecycler()

        getRestaurants()

        binding.seeCategory.setOnClickListener {
            showFilterDialog()
        }

        binding.seeByDistrict.setOnClickListener {
            getRestaurantsByDistrict()
        }

        binding.seeDiscounts.setOnClickListener {
            getRestaurantsByDiscount()
        }

        binding.searchBtn.setOnClickListener {
            startActivity(Intent(requireContext(),SearchProductActivity::class.java))
        }

//        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                // Eğer kullanıcı aşağıya doğru kaydırıyorsa ve bottom menu görünürse, bottom menuyu gizle
//                if (dy > 0 && binding.mainLay.visibility === View.VISIBLE) {
//                    binding.mainLay.visibility = View.GONE
//                } else if (dy < 0 && binding.mainLay.visibility !== View.VISIBLE) {
//                    binding.mainLay.visibility = View.VISIBLE
//                }
//            }
//        })



    }

    private fun showFilterDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_filter)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()


        val applyFilterButton = dialog.findViewById<Button>(R.id.applyFilterButton)
        val checkBoxList = mutableListOf<CheckBox>()
        checkBoxList.add(dialog.findViewById(R.id.cb1))
        checkBoxList.add(dialog.findViewById(R.id.cb2))
        checkBoxList.add(dialog.findViewById(R.id.cb3))
        checkBoxList.add(dialog.findViewById(R.id.cb4))
        checkBoxList.add(dialog.findViewById(R.id.cb5))
        checkBoxList.add(dialog.findViewById(R.id.cb6))
        checkBoxList.add(dialog.findViewById(R.id.cb7))
        checkBoxList.add(dialog.findViewById(R.id.cb8))


        applyFilterButton.setOnClickListener {
            val selectedCategories = getSelectedCategories(checkBoxList)
            if (selectedCategories.isNotEmpty()) {
                getRestaurantsByCategory(selectedCategories)
            }
            dialog.dismiss()
        }

    }


    private fun getSelectedCategories(checkBoxList: List<CheckBox>): List<String> {
        val selectedCategories = mutableListOf<String>()
        for (checkBox in checkBoxList) {
            if (checkBox.isChecked) {
                selectedCategories.add(checkBox.text.toString())
            }
        }

        return selectedCategories
    }


    private fun initRecycler(){
        restaurantList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.setHasFixedSize(true)
        restaurantAdapter = RestaurantAdapter(restaurantList,requireContext(), sellerViewModel,this)
        binding.recyclerView.adapter = restaurantAdapter
        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)
    }



    private fun getRestaurants(){
        lifecycleScope.launch {
            customerViewModel.getAllRestaurants().collect { restaurants ->

                restaurantList.clear()
                if (restaurants.isNotEmpty()) {
                    restaurantList.addAll(restaurants)
                }

                restaurantAdapter.notifyDataSetChanged()

            }
        }
    }

    private fun getRestaurantsByDistrict(){
        customerViewModel.getCustomer(requireContext(),firebaseUser.uid){customer ->
            customer?.let {
                lifecycleScope.launch {
                    customerViewModel.getAllRestaurantsByDistrict(customer.district).collect { restaurants ->
                        restaurantList.clear()
                        if (restaurants.isNotEmpty()) {
                            restaurantList.addAll(restaurants)
                        }

                        restaurantAdapter.notifyDataSetChanged()
                    }

                }

            }
        }


    }

    private fun getRestaurantsByCategory(category : List<String>){
        lifecycleScope.launch {
            customerViewModel.getAllRestaurantsByCategory(category).collect { restaurants ->
                restaurantList.clear()
                if (restaurants.isNotEmpty()) {
                    restaurantList.addAll(restaurants)
                }

                restaurantAdapter.notifyDataSetChanged()
            }

        }

    }

    private fun getRestaurantsByDiscount(){
        customerViewModel.getCustomer(requireContext(),firebaseUser.uid){customer ->
            customer?.let {
                lifecycleScope.launch {
                    customerViewModel.getAllRestaurantsByDiscount(customer.district).collect { restaurants ->
                        restaurantList.clear()
                        if (restaurants.isNotEmpty()) {
                            restaurantList.addAll(restaurants)
                        }else{
                            getRestaurants()
                        }

                        restaurantAdapter.notifyDataSetChanged()
                    }

                }

            }
        }


    }


    override fun clickRestaurant(seller: Seller) {
        val intent = Intent(requireContext(),RestaurantDetailsActivity::class.java)
        intent.putExtra("sellerId",seller.userId)
        startActivity(intent)
    }

}