package com.app.orderfood.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.app.orderfood.R
import com.app.orderfood.activities.customer.CustomerLoginActivity
import com.app.orderfood.activities.customer.MainActivity
import com.app.orderfood.activities.seller.SellerMainActivity
import com.app.orderfood.notify.Token
import com.app.orderfood.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslab.fastprefs.FastPrefs

class OpeningActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opening)

        Handler().postDelayed({
            control()
        },2000)


    }

    private fun control(){
        if (FirebaseAuth.getInstance().currentUser != null){
            val prefs = FastPrefs(this)
            val type = prefs.getString(Constants.ACCOUNT_TYPE,Constants.CUSTOMER_USER)
            if (type.equals(Constants.CUSTOMER_USER)){
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }else if (type.equals(Constants.SELLER_USER)){
                val intent = Intent(this, SellerMainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }else{
            val intent = Intent(this, CustomerLoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }




}