package com.app.orderfood.fragments.customer

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.orderfood.R
import com.app.orderfood.activities.customer.MainActivity
import com.app.orderfood.controller.NavFragment
import com.app.orderfood.databinding.FragmentCustomerLoginBinding
import com.app.orderfood.fragments.seller.SellerRegisterFragment
import com.app.orderfood.model.Customer
import com.app.orderfood.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.yeslab.fastprefs.FastPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.aviran.cookiebar2.CookieBar

class CustomerLoginFragment : Fragment() {

    private lateinit var binding : FragmentCustomerLoginBinding
    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCustomerLoginBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore  = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        binding.newAccount.setOnClickListener {
            NavFragment.openNewFragment(CustomerRegisterFragment(),requireActivity(), R.id.fragment_container)
        }


        binding.changeAccountTypeTitle.setOnClickListener {
            NavFragment.openNewFragment(SellerRegisterFragment(),requireActivity(), R.id.fragment_container)

        }

        binding.btnLogin.setOnClickListener {
            login()
        }

        binding.tvForgotPassword.setOnClickListener {
            forgotPassword()
        }


    }

    private fun forgotPassword(){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_forgot_password)

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)

        val editText: EditText = dialog.findViewById(R.id.forgotPasswordEt)
        val button: Button = dialog.findViewById(R.id.sendPasswordLink)

        button.setOnClickListener {
            if (editText.text.toString().trim().isNotEmpty()) {
                firebaseAuth.sendPasswordResetEmail(editText.text.toString().trim())
                    .addOnSuccessListener {
                        dialog.dismiss()
                        CookieBar.build(requireContext() as Activity?)
                            .setTitle("Mail Gönderildi")
                            .setCookiePosition(CookieBar.TOP)
                            .show()
                    }
                    .addOnFailureListener {
                        dialog.dismiss()
                    }
            }
        }

    }


    private fun login() {
        if (binding.emailEt.text.toString().trim().isNotEmpty() && binding.passwordEt.text.toString().trim().isNotEmpty()) {
            val pd = ProgressDialog(requireContext(), R.style.CustomDialog)
            pd.setCancelable(false)
            pd.show()


            FirebaseFirestore.getInstance().collection("Customers").whereEqualTo("email",binding.emailEt.text.toString())
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        val querySnapshot: QuerySnapshot? = task.result
                        if (!querySnapshot!!.isEmpty) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {

                                    val authResult = withContext(Dispatchers.IO) {
                                        firebaseAuth.signInWithEmailAndPassword(
                                            binding.emailEt.text.toString().lowercase().trim(),
                                            binding.passwordEt.text.toString().trim()
                                        ).await()
                                    }

                                    val user = authResult.user
                                    user?.reload()


                                    val myUser = withContext(Dispatchers.IO) {
                                        firestore.collection("Customers").document(user!!.uid)
                                            .get().await().toObject(Customer::class.java)
                                    }

                                    if (myUser != null) {
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
        }

    }
}
