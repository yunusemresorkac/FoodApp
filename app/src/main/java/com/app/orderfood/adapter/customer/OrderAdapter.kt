package com.app.orderfood.adapter.customer

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.orderfood.R
import com.app.orderfood.databinding.OrderItemBinding
import com.app.orderfood.model.Food
import com.app.orderfood.model.Order
import com.app.orderfood.model.Seller
import com.app.orderfood.util.Constants
import com.app.orderfood.util.DummyMethods
import com.app.orderfood.viewmodel.SellerViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class OrderAdapter(private val orderList: ArrayList<Order>, val context: Context,
                   private val sellerViewModel: SellerViewModel,private val orderClick: OrderClick
) : RecyclerView.Adapter<OrderAdapter.MyHolder>() {

    private lateinit var firebaseUser: FirebaseUser

    class MyHolder(val binding: OrderItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = OrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val order = orderList[position]

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        when (order.status){
            Constants.COMPLETED_ORDER -> {
                holder.binding.status.text = "Sipariş Tamamlandı"
                holder.binding.status.setTextColor(context.resources.getColor(R.color.green))
            }
            Constants.PREPARING_ORDER -> {
                holder.binding.status.text = "Sipariş Hazırlanıyor"
                holder.binding.status.setTextColor(context.resources.getColor(R.color.orange_700))
            }
            Constants.CANCELLED_ORDER -> {
                holder.binding.status.text = "Sipariş İptal Edildi"
                holder.binding.status.setTextColor(context.resources.getColor(R.color.error))
            }
        }

        holder.binding.time.text = DummyMethods.convertTime(order.time.toLong())
        holder.binding.price.text = "₺${order.totalPrice}"
        holder.binding.firstItem.text = order.food.title + " (${order.quantity} Adet)"
        getSellerData(holder, order)

        if (firebaseUser.uid.equals(order.userId)){
            holder.binding.deleteOrder.visibility = View.VISIBLE
        }else{
            holder.binding.deleteOrder.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            orderClick.clickOrder(orderList[position])
        }



        holder.binding.deleteOrder.setOnClickListener {
            FirebaseFirestore.getInstance().collection("MyOrders").document(order.userId)
                .collection("MyOrders").document(order.id)
                .delete().addOnSuccessListener {

                }
        }
    }


    private fun getSellerData(holder: MyHolder, order: Order){

        sellerViewModel.getSeller(context,order.sellerId){ restaurant ->
            restaurant?.let {
                holder.binding.shopName.text = restaurant.shopName
                Glide.with(context).load(restaurant.sellerImage).into(holder.binding.shopImage)
            }
        }

    }



    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    interface OrderClick{
        fun clickOrder(order: Order)

    }



}