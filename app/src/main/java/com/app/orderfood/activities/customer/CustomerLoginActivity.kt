package com.app.orderfood.activities.customer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.orderfood.R
import com.app.orderfood.databinding.ActivityCustomerLoginBinding
import com.app.orderfood.fragments.customer.CustomerLoginFragment

class CustomerLoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCustomerLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                CustomerLoginFragment()
            ).commit()
        }

    }
}