package com.app.orderfood.fragments.customer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.orderfood.activities.customer.OrderDetailsActivity
import com.app.orderfood.activities.customer.RestaurantDetailsActivity
import com.app.orderfood.adapter.customer.OrderAdapter
import com.app.orderfood.databinding.FragmentOrdersBinding
import com.app.orderfood.model.Order
import com.app.orderfood.viewmodel.OrderViewModel
import com.app.orderfood.viewmodel.SellerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class OrdersFragment :Fragment(), OrderAdapter.OrderClick {

    private lateinit var binding : FragmentOrdersBinding
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var orderList : ArrayList<com.app.orderfood.model.Order>
    private lateinit var firebaseUser: FirebaseUser
    private val orderViewModel by viewModel<OrderViewModel>()
    private val sellerViewModel by viewModel<SellerViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrdersBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        initRecycler()
        getOrders()

    }

    private fun initRecycler(){
        orderList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.setHasFixedSize(true)
        orderAdapter = OrderAdapter(orderList,requireContext(),sellerViewModel,this)
        binding.recyclerView.adapter = orderAdapter
        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)

    }

    private fun getOrders(){
        lifecycleScope.launch {
            orderViewModel.getAllOrders(firebaseUser.uid).collect { orders ->

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

    override fun clickOrder(order: Order) {
        val intent = Intent(requireContext(), OrderDetailsActivity::class.java)
        intent.putExtra("orderId",order.id)
        startActivity(intent)
    }


}