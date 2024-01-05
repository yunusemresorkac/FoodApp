package com.app.orderfood.adapter.seller

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.orderfood.databinding.ProductItemBinding
import com.app.orderfood.model.Food
import com.bumptech.glide.Glide


class ProductAdapter(private val foodList: ArrayList<Food>, val context: Context
    ,private val productClick: ProductClick
) : RecyclerView.Adapter<ProductAdapter.MyHolder>() {


    class MyHolder(val binding: ProductItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = ProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val food = foodList[position]


        holder.binding.productName.text = food.title
        holder.binding.productDesc.text = food.description
        Glide.with(context).load(food.imageUrl).into(holder.binding.productImage)


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
            productClick.clickProduct(foodList[position])
        }

    }



    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    interface ProductClick{
        fun clickProduct(food: Food)
    }

}