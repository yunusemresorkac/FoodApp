package com.app.orderfood.adapter.customer

import com.app.orderfood.databinding.OrderDetailItemBinding


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.orderfood.model.Food
import com.app.orderfood.model.Order
import com.app.orderfood.util.DummyMethods
import com.app.orderfood.viewmodel.SellerViewModel
import com.bumptech.glide.Glide


class OrderDetailAdapter(private val orderList: ArrayList<Order>, val context: Context
    ,private val clickOrderDetail : OrderDetailClick
) : RecyclerView.Adapter<OrderDetailAdapter.MyHolder>() {


    class MyHolder(val binding: OrderDetailItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = OrderDetailItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val order = orderList[position]

        holder.binding.productName.text = order.food.title
        holder.binding.productDesc.text = order.food.description
        holder.binding.price.text ="₺${order.food.price}"
        holder.binding.quantity.text = "${order.quantity} Adet"
        Glide.with(context).load(order.food.imageUrl).into(holder.binding.productImage)

        holder.itemView.setOnClickListener {
            clickOrderDetail.clickDetail(order)
        }

    }




    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    interface OrderDetailClick{
        fun clickDetail(order: Order)
    }



}