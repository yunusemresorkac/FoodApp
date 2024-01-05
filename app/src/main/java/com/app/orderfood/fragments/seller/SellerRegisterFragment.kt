package com.app.orderfood.fragments.seller

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.orderfood.R
import com.app.orderfood.activities.seller.SellerMainActivity
import com.app.orderfood.controller.CategoryList
import com.app.orderfood.controller.NavFragment
import com.app.orderfood.databinding.FragmentSellerRegisterBinding
import com.app.orderfood.fragments.customer.CustomerRegisterFragment
import com.app.orderfood.model.Seller
import com.app.orderfood.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslab.fastprefs.FastPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SellerRegisterFragment : Fragment() {

    private lateinit var binding : FragmentSellerRegisterBinding
    private lateinit var firestore : FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var category : String? = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSellerRegisterBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore  = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        binding.alreadyAccount.setOnClickListener {
            NavFragment.openNewFragment(SellerLoginFragment(),requireActivity(), R.id.fragment_container)
        }

        binding.changeAccountTypeTitle.setOnClickListener {
            NavFragment.openNewFragment(CustomerRegisterFragment(),requireActivity(), R.id.fragment_container)

        }

        binding.btnRegister.setOnClickListener {
            register()
        }


        initCategorySpinner()
    }

    private fun initCategorySpinner() {
        val categoryList = CategoryList.getCategoryList()

        binding.categorySpinner.item = categoryList

        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
                category = categoryList[position]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
    }

    private fun register() {
        if (binding.emailEt.text.toString().trim().isNotEmpty() && binding.passwordEt.text.toString().trim().isNotEmpty() &&
            binding.firstNameEt.text.toString().trim().isNotEmpty() && binding.lastNameEt.text.toString().trim().isNotEmpty()
            && binding.restoranName.text.toString().trim().isNotEmpty() && !category.equals("")) {
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
                    val seller = Seller(
                        userId,binding.firstNameEt.text.toString().trim(),binding.lastNameEt.text.toString().trim(),
                        binding.emailEt.text.toString().trim(),
                        binding.restoranName.text.toString().trim(),category.toString(),0.0,"","","",System.currentTimeMillis()
                        ,"",0,0,0.0,0.0
                    )

                    firestore.collection("Sellers").document(userId)
                        .set(seller).addOnCompleteListener {
                            val prefs = FastPrefs(requireContext())
                            prefs.setString(Constants.ACCOUNT_TYPE,Constants.SELLER_USER)
                            val intent = Intent(requireContext(), SellerMainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            pd.dismiss()
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