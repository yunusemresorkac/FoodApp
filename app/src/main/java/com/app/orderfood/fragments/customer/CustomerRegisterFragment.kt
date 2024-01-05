package com.app.orderfood.fragments.customer


import android.app.ProgressDialog

import android.content.Intent

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.fragment.app.Fragment
import com.app.orderfood.R
import com.app.orderfood.activities.customer.MainActivity
import com.app.orderfood.controller.NavFragment
import com.app.orderfood.databinding.FragmentCustomerRegisterBinding
import com.app.orderfood.fragments.seller.SellerRegisterFragment
import com.app.orderfood.model.Customer
import com.app.orderfood.util.Constants

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslab.fastprefs.FastPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CustomerRegisterFragment : Fragment() {

    private lateinit var binding : FragmentCustomerRegisterBinding
    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerRegisterBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore  = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        binding.alreadyAccount.setOnClickListener {
            NavFragment.openNewFragment(CustomerLoginFragment(),requireActivity(), R.id.fragment_container)
        }

        binding.changeAccountTypeTitle.setOnClickListener {
            NavFragment.openNewFragment(SellerRegisterFragment(),requireActivity(), R.id.fragment_container)

        }

        binding.btnRegister.setOnClickListener {
            register()
        }


    }

    private fun register() {
        if (binding.emailEt.text.toString().trim().isNotEmpty() && binding.passwordEt.text.toString().trim().isNotEmpty() &&
            binding.firstNameEt.text.toString().trim().isNotEmpty()) {
            val pd = ProgressDialog(requireContext(), R.style.CustomDialog)
            pd.setCancelable(false)
            pd.show()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val authResult = firebaseAuth.createUserWithEmailAndPassword(
                        binding.emailEt.text.toString().lowercase().trim(),
                        binding.passwordEt.text.toString().trim()
                    ).await()

                    val userId = authResult.user?.uid ?: ""
                    val customer = Customer(
                        userId,binding.firstNameEt.text.toString().trim(),binding.lastNameEt.text.toString().trim()
                        ,binding.emailEt.text.toString().trim(),"","","",System.currentTimeMillis(),0.0
                    )

                    firestore.collection("Customers").document(userId)
                        .set(customer).addOnCompleteListener {
                            val prefs = FastPrefs(requireContext())
                            prefs.setString(Constants.ACCOUNT_TYPE, Constants.CUSTOMER_USER)
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)

                        }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        pd.dismiss()
                        Toast.makeText(context, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }



}