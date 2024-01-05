package com.app.orderfood.activities.customer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.orderfood.R
import com.app.orderfood.databinding.ActivityCustomerEditProfileBinding
import com.app.orderfood.util.DummyMethods
import com.app.orderfood.viewmodel.CustomerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle

class CustomerEditProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCustomerEditProfileBinding
    private val customerViewModel by viewModel<CustomerViewModel>()
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {finish()}


        firebaseUser = FirebaseAuth.getInstance().currentUser!!


        getCustomerData()

        binding.btnUpdate.setOnClickListener {
            editProfile()
        }

    }

    //Kullanıcı verisini al
    private fun getCustomerData(){
        customerViewModel.getCustomer(this,firebaseUser.uid){ customer ->
            customer?.let {
                binding.firstNameEt.setText(customer.firstName)
                binding.lastNameEt.setText(customer.lastName)
                binding.emailEt.setText(customer.email)
                binding.cityEt.setText(customer.city)
                binding.districtEt.setText(customer.district)
                binding.fullAddressEt.setText(customer.fullAddress)

            }

        }
    }

    //Profili düzenle
    private fun editProfile(){
        if (binding.firstNameEt.text.toString().trim().isNotEmpty() && binding.lastNameEt.text.toString().trim().isNotEmpty()
            && binding.cityEt.text.toString().trim().isNotEmpty() && binding.districtEt.text.toString().trim().isNotEmpty()
            && binding.fullAddressEt.text.toString().trim().isNotEmpty()){
            val map : HashMap<String, Any> = HashMap()
            map["firstName"] = binding.firstNameEt.text.toString().trim()
            map["lastName"] = binding.lastNameEt.text.toString().trim()
            map["city"] = binding.lastNameEt.text.toString().trim()
            map["district"] = binding.lastNameEt.text.toString().trim()
            map["fullAddress"] = binding.lastNameEt.text.toString().trim()

            FirebaseFirestore.getInstance().collection("Customers").document(firebaseUser.uid)
                .update(map).addOnSuccessListener {
                    DummyMethods.showMotionToast(this,"Bilgileriniz Güncellendi","",MotionToastStyle.SUCCESS)
                }


        }
    }



}