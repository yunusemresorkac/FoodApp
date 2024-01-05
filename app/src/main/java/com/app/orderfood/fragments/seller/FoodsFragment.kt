package com.app.orderfood.fragments.seller

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.orderfood.activities.seller.EditProductActivity
import com.app.orderfood.adapter.seller.ProductAdapter
import com.app.orderfood.databinding.FragmentFoodsBinding
import com.app.orderfood.model.Food
import com.app.orderfood.viewmodel.SellerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class FoodsFragment : Fragment(), ProductAdapter.ProductClick {

    private lateinit var binding : FragmentFoodsBinding
    private val sellerViewModel by viewModel<SellerViewModel>()
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var foodList : ArrayList<Food>
    private lateinit var productAdapter : ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =FragmentFoodsBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        initRecycler()
        getFoods()
    }

    private fun initRecycler(){
        foodList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.setHasFixedSize(true)
        productAdapter =  ProductAdapter(foodList,requireContext(),this)
        binding.recyclerView.adapter = productAdapter
        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)
    }

    private fun getFoods(){
        lifecycleScope.launch {
            sellerViewModel.getAllFoods(firebaseUser.uid,null).collect { foods ->
                foodList.clear()
                if (foods.isNotEmpty()) {
                    foodList.addAll(foods)
                }

                productAdapter.notifyDataSetChanged()
//                if (chatList.size > 0){
//                    binding.chatsLay.visibility = View.VISIBLE
//                } else {
//                    binding.chatsLay.visibility = View.GONE
//                }
            }
        }
    }

    override fun clickProduct(food: Food) {
        val intent = Intent(requireContext(),EditProductActivity::class.java)
        intent.putExtra("productId",food.id)
        intent.putExtra("sellerId",food.sellerId)
        startActivity(intent)
    }


}