package com.app.orderfood.adapter.customer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.orderfood.databinding.FoodItemBinding
import com.app.orderfood.model.Food
import com.bumptech.glide.Glide


class FoodAdapter(private val foodList: ArrayList<Food>, val context: Context,private val foodClick: FoodClick
) : RecyclerView.Adapter<FoodAdapter.MyHolder>() {


    class MyHolder(val binding: FoodItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = FoodItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val food = foodList[position]


        holder.binding.productName.text = food.title
        holder.binding.productDesc.text = food.description
        Glide.with(context).load(food.imageUrl).into(holder.binding.productImage)
        if (food.stock in 1..9){
            holder.binding.stock.visibility = View.VISIBLE
            holder.binding.stock.text = "Sınırlı Stok: ${food.stock} Adet"
        }else{
            holder.binding.stock.visibility = View.GONE
        }


        val formattedPrice: String
        if (food.discountPercentage > 0) {
            val discountedPrice = food.price - (food.price * food.discountPercentage / 100)
            formattedPrice = "₺$discountedPrice"
            val originalPrice = "₺${food.price}"
            val spannableString = SpannableString("$formattedPrice $originalPrice")
            spannableString.setSpan(StrikethroughSpan(), formattedPrice.length + 1, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.binding.price.text = spannableString
        } else {
            formattedPrice = "₺${food.price}"
            holder.binding.price.text = formattedPrice
        }

        holder.itemView.setOnClickListener {
            foodClick.clickFood(foodList[position])
        }

    }



    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }


    interface FoodClick{

        fun clickFood(food: Food)

    }

}