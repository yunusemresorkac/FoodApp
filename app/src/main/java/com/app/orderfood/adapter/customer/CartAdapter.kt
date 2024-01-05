package com.app.orderfood.adapter.customer


import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.orderfood.databinding.CartItemBinding
import com.app.orderfood.databinding.FoodItemBinding
import com.app.orderfood.model.Cart
import com.app.orderfood.model.Food
import com.app.orderfood.viewmodel.CartViewModel
import com.bumptech.glide.Glide


class CartAdapter(private val cartList: ArrayList<Cart>, val context: Context,
    private val cartClick: CartClick
) : RecyclerView.Adapter<CartAdapter.MyHolder>() {


    class MyHolder(val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val cart = cartList[position]


        holder.binding.productName.text = cart.food.title
        holder.binding.productDesc.text = cart.food.description
        Glide.with(context).load(cart.food.imageUrl).into(holder.binding.productImage)
        val formattedPrice: String
        if (cart.food.discountPercentage > 0) {
            val discountedPrice = cart.food.price - (cart.food.price * cart.food.discountPercentage / 100)
            formattedPrice = "₺$discountedPrice"
            val originalPrice = "₺${cart.food.price}"
            val spannableString = SpannableString("$formattedPrice $originalPrice")
            spannableString.setSpan(StrikethroughSpan(), formattedPrice.length + 1, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.binding.price.text = spannableString

            val totalPrice = cart.quantity * discountedPrice
            holder.binding.quantity.text = "Adet: ${cart.quantity}\nToplam Fiyat: ₺$totalPrice"
        } else {
            val formattedPrice = "₺${cart.food.price}"
            holder.binding.price.text = formattedPrice

            val totalPrice = cart.quantity * cart.food.price
            holder.binding.quantity.text = "Adet: ${cart.quantity}\nToplam Fiyat: ₺$totalPrice"
        }



        holder.itemView.setOnClickListener {
            cartClick.clickCart(cartList[position])
        }

        holder.binding.increaseBtn.setOnClickListener {
            cartClick.increaseQuantity(cartList[position])
        }

        holder.binding.decreaseBtn.setOnClickListener {
            cartClick.decreaseQuantity(cartList[position])
        }

        holder.binding.deleteBtn.setOnClickListener {
            cartClick.deleteItem(cartList[position])
        }

    }



    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    interface CartClick{
        fun clickCart(cart: Cart)

        fun increaseQuantity(cart: Cart)

        fun decreaseQuantity(cart: Cart)

        fun deleteItem(cart: Cart)


    }


}