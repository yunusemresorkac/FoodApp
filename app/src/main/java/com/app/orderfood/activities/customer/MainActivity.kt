package com.app.orderfood.activities.customer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.app.orderfood.R
import com.app.orderfood.databinding.ActivityMainBinding
import com.app.orderfood.fragments.customer.CartFragment
import com.app.orderfood.fragments.customer.HomeFragment
import com.app.orderfood.fragments.customer.OrdersFragment
import com.app.orderfood.fragments.customer.ProfileFragment
import com.app.orderfood.notify.Token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectorFragment: Fragment? = null
    private lateinit var firebaseUser : FirebaseUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                HomeFragment()
            ).commit()
        }

        binding.bottom.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navHome ->  if (selectorFragment !is HomeFragment) {
                    selectorFragment = HomeFragment()
                }
                R.id.navCart ->  if (selectorFragment !is CartFragment) {
                    selectorFragment = CartFragment()
                }
                R.id.navOrders ->  if (selectorFragment !is OrdersFragment) {
                    selectorFragment = OrdersFragment()
                }

                R.id.navProfile -> if (selectorFragment !is ProfileFragment) {
                    selectorFragment = ProfileFragment()
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