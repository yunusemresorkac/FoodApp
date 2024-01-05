package com.app.orderfood.adapter.customer

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.orderfood.databinding.RestaurantItemBinding
import com.app.orderfood.model.Seller
import com.app.orderfood.util.DummyMethods
import com.app.orderfood.viewmodel.SellerViewModel
import com.bumptech.glide.Glide


class RestaurantAdapter(private var sellerList: ArrayList<Seller>, val context: Context, private val sellerViewModel: SellerViewModel
                        , private val restaurantClick: RestaurantClick
) : RecyclerView.Adapter<RestaurantAdapter.MyHolder>() {


    class MyHolder(val binding: RestaurantItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = RestaurantItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val seller = sellerList[position]

        if (seller.discountStatus == 0){
            holder.binding.discount.visibility = View.GONE
        }else{
            holder.binding.discount.visibility = View.VISIBLE
            holder.binding.discount.text = "İndirim Var!"
        }
        getSellerData(holder, seller)

        holder.itemView.setOnClickListener {
            restaurantClick.clickRestaurant(sellerList[position])
        }
    }

    private fun getSellerData(holder: MyHolder, seller: Seller ){

        sellerViewModel.getSeller(context,seller.userId){ restaurant ->
            restaurant?.let {
                if (restaurant.numberOfRates == 0){
                    holder.binding.rate.text = "Değerlendirme Yok"
                }else{
                    val rate = restaurant.rate / restaurant.numberOfRates
                    holder.binding.rate.text = "${DummyMethods.formatDoubleNumber(rate)} (${restaurant.numberOfRates})"
                }
                holder.binding.sellerName.text = "${restaurant.shopName} - ${restaurant.shopCity} (${restaurant.shopDistrict})"
                Glide.with(context).load(restaurant.sellerImage).into(holder.binding.sellerImage)
                holder.binding.category.text = "•${restaurant.category}"


            }
        }

    }

    override fun getItemCount(): Int {
        return sellerList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    interface RestaurantClick{
        fun clickRestaurant(seller: Seller)
    }


}