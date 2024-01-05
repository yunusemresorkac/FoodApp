package com.app.orderfood.fragments.customer

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.orderfood.R
import com.app.orderfood.activities.customer.FoodDetailsActivity
import com.app.orderfood.adapter.customer.CartAdapter
import com.app.orderfood.databinding.FragmentCartBinding
import com.app.orderfood.model.Cart
import com.app.orderfood.model.Food
import com.app.orderfood.model.Order
import com.app.orderfood.util.DummyMethods
import com.app.orderfood.viewmodel.CartViewModel
import com.app.orderfood.viewmodel.CustomerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle

class CartFragment : Fragment(), CartAdapter.CartClick {

    private lateinit var binding : FragmentCartBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var cartList: ArrayList<Cart>
    private lateinit var cartAdapter : CartAdapter
    private val cartViewModel by viewModel<CartViewModel>()
    private val customerViewModel by viewModel<CustomerViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        initRecycler()
        getCart()

        binding.deleteAllCart.setOnClickListener {
            deleteCart()
        }

    }

    private fun deleteCart(){
        for (cart in cartList){
            FirebaseFirestore.getInstance().collection("Carts").document(firebaseUser.uid)
                .collection("Carts").document(cart.food.id)
                .delete()
                .addOnSuccessListener {
                    println("sepet silindi")

                }.addOnFailureListener {
                    println("hata ${it.message}")


                }
        }

    }

    private fun initRecycler(){
        cartList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.setHasFixedSize(true)
        cartAdapter = CartAdapter(cartList,requireContext(),this)
        binding.recyclerView.adapter = cartAdapter
        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)

    }

    private fun getCart(){
        lifecycleScope.launch {
            cartViewModel.getAllCart(firebaseUser.uid).collect { carts ->

                cartList.clear()
                if (carts.isNotEmpty()) {
                    cartList.addAll(carts)
                }

                cartAdapter.notifyDataSetChanged()
                calculateTotalPrice()
//                if (chatList.size > 0){
//                    binding.chatsLay.visibility = View.VISIBLE
//                } else {
//                    binding.chatsLay.visibility = View.GONE
//                }
            }
        }
    }

    private fun calculateTotalPrice() {
        if (getTotalPrice(cartList) == 0.0) {
            binding.checkOutBtn.text = "Sepetiniz Boş"
            binding.checkOutBtn.isEnabled = false
        } else {
            binding.checkOutBtn.text = "Tamamla -> ₺${getTotalPrice(cartList)}"
            binding.checkOutBtn.isEnabled = true
            binding.checkOutBtn.setOnClickListener {
                customerViewModel.getCustomer(requireContext(),firebaseUser.uid){customer ->
                customer?.let {
                    if (customer.balance >= getTotalPrice(cartList)){
                        lostBalance()
                    }else{
                        DummyMethods.showMotionToast(requireContext(),"Yetersiz Bakiye","",MotionToastStyle.INFO)
                    }
                }
                }
            }
        }
    }



    private fun getTotalPrice(cartList: List<Cart>): Double {
        var totalPrice = 0.0

        for (cart in cartList) {
            val price = if (cart.food.discountPercentage > 0) {
                cart.food.price - (cart.food.price * cart.food.discountPercentage / 100)
            } else {
                cart.food.price
            }
            totalPrice += cart.quantity * price
        }

        return totalPrice
    }


    private fun lostBalance(){
        customerViewModel.getCustomer(requireContext(),firebaseUser.uid){ customer ->
            val map : HashMap<String,Any> = HashMap()
            customer?.let {
                map["balance"] = customer.balance - getTotalPrice(cartList)
                FirebaseFirestore.getInstance().collection("Customers").document(firebaseUser.uid)
                    .update(map)
                    .addOnSuccessListener {
                        lostStock()
                        createOrder()
                    }

            }
        }
    }

    private fun lostStock(){
        for (cart in cartList){
            val map : HashMap<String,Any> = HashMap()
            map["stock"] = cart.food.stock - cart.quantity
            FirebaseFirestore.getInstance().collection("Foods").document(cart.food.sellerId)
                .collection("Foods").document(cart.food.id)
                .update(map)
        }

    }

    private fun createOrder() {
        val orderId = DummyMethods.generateRandomString(10)
        val time = System.currentTimeMillis().toString()

        val orderItems = hashMapOf<String, HashMap<String, Any>>()

        for ((index, cart) in cartList.withIndex()) {
            val orderItem = hashMapOf<String, Any>()
            orderItem["food"] = cart.food
            orderItem["quantity"] = cart.quantity
            orderItems["$index"] = orderItem
        }

        val order = Order(cartList[0].food, 0, time, orderId, firebaseUser.uid, cartList[0].food.sellerId, getTotalPrice(cartList))

        val batch = FirebaseFirestore.getInstance().batch()
        val batchForSeller = FirebaseFirestore.getInstance().batch()

        batch.set(
            FirebaseFirestore.getInstance().collection("MyOrders").document(firebaseUser.uid)
                .collection("MyOrders").document(orderId), order
        )

        batchForSeller.set(
            FirebaseFirestore.getInstance().collection("OurOrders").document(cartList[0].food.sellerId)
                .collection("OurOrders").document(orderId), order
        )

        // Add order items to a subcollection under the order
        val orderItemsCollection = FirebaseFirestore.getInstance().collection("MyOrders")
            .document(firebaseUser.uid).collection("MyOrders").document(orderId).collection("OrderItems")

        val orderItemsCollectionForSeller = FirebaseFirestore.getInstance().collection("OurOrders")
            .document(cartList[0].food.sellerId).collection("OurOrders").document(orderId).collection("OrderItems")

        for ((index, orderItem) in orderItems) {
            orderItemsCollection.document("item$index").set(orderItem)
        }

        for ((index, orderItem) in orderItems) {
            orderItemsCollectionForSeller.document("item$index").set(orderItem)
        }

        batch.commit().addOnSuccessListener {
            batchForSeller.commit().addOnSuccessListener {
                deleteCart()
                DummyMethods.showMotionToast(requireContext(), "Siparişiniz Alındı", "", MotionToastStyle.SUCCESS)
            }
        }


    }


    override fun clickCart(cart: Cart) {
        val intent = Intent(requireContext(), FoodDetailsActivity::class.java)
        intent.putExtra("foodId",cart.food.id)
        intent.putExtra("foodSellerId",cart.food.sellerId)
        startActivity(intent)
    }

    override fun increaseQuantity(cart: Cart) {
        cartViewModel.increaseQuantity(firebaseUser.uid,cart.food.id,cart.quantity,cart.food,requireContext())
    }

    override fun decreaseQuantity(cart: Cart) {
        cartViewModel.decreaseQuantity(firebaseUser.uid,cart.food.id,cart.quantity)

    }

    override fun deleteItem(cart: Cart) {
        cartViewModel.deleteCartItem(firebaseUser.uid,cart.food.id)

    }

    class CreditCardSkTextWatcher(private val editText: EditText) : TextWatcher {

        private var isFormatting: Boolean = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Önceki metni izlemek için
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Metin değiştiğinde yapılacak işlemler
        }

        override fun afterTextChanged(s: Editable?) {
            if (!isFormatting) {
                isFormatting = true

                if (s != null && s.isNotEmpty()) {
                    if (s.length == 2 && !s.contains("/")) {
                        val formattedText = StringBuilder(s)
                        formattedText.insert(2, "/")
                        editText.setText(formattedText.toString())
                        editText.setSelection(formattedText.length)
                    } else if (s.length == 3 && s[2] != '/') {
                        val formattedText = StringBuilder(s.substring(0, 2) + "/" + s.substring(2))
                        editText.setText(formattedText.toString())
                        editText.setSelection(formattedText.length)
                    }
                }

                isFormatting = false
            } else {
                isFormatting = false
            }
        }

    }


    class CreditCardNumberTextWatcher(private val editText: EditText) : TextWatcher {

        private var isFormatting: Boolean = false

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Önceki metni izlemek için
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Metin değiştiğinde yapılacak işlemler
        }

        override fun afterTextChanged(s: Editable?) {
            if (!isFormatting) {
                isFormatting = true

                if (s != null && s.isNotEmpty() && s.length % 5 == 0) {
                    val formattedText = StringBuilder(s)
                    if (s[s.length - 1] != ' ') {
                        formattedText.insert(s.length - 1, " ")
                        editText.setText(formattedText.toString())
                        editText.setSelection(formattedText.length)
                    }
                }

                isFormatting = false
            } else {
                isFormatting = false
            }
        }
    }


}