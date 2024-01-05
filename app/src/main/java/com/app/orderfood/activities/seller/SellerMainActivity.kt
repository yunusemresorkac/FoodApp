package com.app.orderfood.activities.seller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.app.orderfood.R
import com.app.orderfood.databinding.ActivitySellerMainBinding
import com.app.orderfood.fragments.seller.AddFoodFragment
import com.app.orderfood.fragments.seller.FoodsFragment
import com.app.orderfood.fragments.seller.RestaurantOrdersFragment
import com.app.orderfood.fragments.seller.SellerProfileFragment
import com.app.orderfood.notify.Token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId

class SellerMainActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySellerMainBinding
    private var selectorFragment: Fragment? = null
    private lateinit var firebaseUser : FirebaseUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                RestaurantOrdersFragment()
            ).commit()
        }

        binding.bottom.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navOurOrders ->  if (selectorFragment !is RestaurantOrdersFragment) {
                    selectorFragment = RestaurantOrdersFragment()
                }
                R.id.navAddProduct ->  {
                   startActivity(Intent(this,AddFoodActivity::class.java))
                }

                R.id.navOurFoods -> if (selectorFragment !is FoodsFragment) {
                    selectorFragment = FoodsFragment()
                }
                R.id.navOurProfile -> if (selectorFragment !is SellerProfileFragment) {
                    selectorFragment = SellerProfileFragment()
                }

            }
            if (selectorFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectorFragment!!).commit()
            }
            true
        }

        updateToken(FirebaseInstanceId.getInstance().token!!, firebaseUser.uid)


    }
    private fun updateToken(token: String, userId: String) {
        val ref = FirebaseFirestore.getInstance().collection("Tokens")
        val mToken = Token(token)
        ref.document(userId).set(mToken)
    }


}