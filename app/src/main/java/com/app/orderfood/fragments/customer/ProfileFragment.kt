package com.app.orderfood.fragments.customer

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.orderfood.R
import com.app.orderfood.activities.customer.CustomerEditProfileActivity
import com.app.orderfood.activities.customer.CustomerLoginActivity
import com.app.orderfood.controller.NavFragment
import com.app.orderfood.databinding.FragmentProfileBinding
import com.app.orderfood.model.CardInfo
import com.app.orderfood.model.Customer
import com.app.orderfood.model.Seller
import com.app.orderfood.permissions.PermissionManager
import com.app.orderfood.util.DummyMethods
import com.app.orderfood.util.LocationHelper
import com.app.orderfood.viewmodel.CustomerViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle
import java.util.Locale

class ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding
    private lateinit var firebaseUser: FirebaseUser
    private val customerViewModel by viewModel<CustomerViewModel>()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var permissionManager: PermissionManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        permissionManager = PermissionManager()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!


        getCustomer()

        binding.signOutBtn.setOnClickListener {
           signOut()
        }

        binding.city.setOnClickListener {
            manageLocation()
        }

        binding.editProfileBtn.setOnClickListener {
            val intent = Intent(requireContext(),CustomerEditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.addBalance.setOnClickListener {
            showCheckoutDialog()
        }

    }

    private fun saveCardInfo(cardNo : String, cardName : String, cardCvc : String, cardExp : String){
        val cardInfo = CardInfo(firebaseUser.uid,cardNo,cardName, cardCvc, cardExp)
        FirebaseFirestore.getInstance().collection("Cards").document(firebaseUser.uid)
            .set(cardInfo)
            .addOnSuccessListener {

            }
    }

    //kayıtlı kart varsa al
    private fun getSavedCard(textView: TextView, cardLayout : RelativeLayout){
        val docRef = FirebaseFirestore.getInstance().collection("Cards").document(firebaseUser.uid)
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val card = documentSnapshot.toObject(CardInfo::class.java)
                    if (card != null) {
                        cardLayout.visibility = View.VISIBLE
                        textView.text = "${card.cardName}\n${card.cardNo}"
                    }
                } else {
                }
            }
            .addOnFailureListener {
            }
    }

    //kartı sil
    private fun deleteCard(dialog: Dialog){
        FirebaseFirestore.getInstance().collection("Cards").document(firebaseUser.uid)
            .delete().addOnSuccessListener {
                DummyMethods.showMotionToast(requireContext(),"Kartınız Silindi","",MotionToastStyle.SUCCESS)
                dialog.dismiss()
            }
    }

    //bakiye ekleme dialogu
    private fun showCheckoutDialog(){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_checkout)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()

        val saveCardCb = dialog.findViewById<CheckBox>(R.id.saveCardCb)
        val amountEt = dialog.findViewById<EditText>(R.id.amountEt)
        val cardNoEt = dialog.findViewById<EditText>(R.id.cardNoEt)
        val cardSkEt = dialog.findViewById<EditText>(R.id.cardSkEt)
        val cardCvcEt = dialog.findViewById<EditText>(R.id.cardCvcEt)
        val cardNameEt = dialog.findViewById<EditText>(R.id.cardNameEt)
        val confirmBtn = dialog.findViewById<TextView>(R.id.confirmBtn)
        val savedCard = dialog.findViewById<RelativeLayout>(R.id.savedCard)
        val savedCardName = dialog.findViewById<TextView>(R.id.savedCardName)
        val deleteCard = dialog.findViewById<ImageView>(R.id.deleteCard)

        deleteCard.setOnClickListener { deleteCard(dialog) }

        cardNoEt.addTextChangedListener(CartFragment.CreditCardNumberTextWatcher(cardNoEt))
        cardSkEt.addTextChangedListener(CartFragment.CreditCardSkTextWatcher(cardSkEt))

        getSavedCard(savedCardName,savedCard)


        confirmBtn.setOnClickListener {
            if (amountEt.text.toString().trim().isNotEmpty() && cardNoEt.text.toString().trim().isNotEmpty() &&
                cardSkEt.text.toString().trim().isNotEmpty() && cardCvcEt.text.toString().trim().isNotEmpty()
                && cardNameEt.text.toString().trim().isNotEmpty()){
                customerViewModel.getCustomer(requireContext(),firebaseUser.uid){ customer ->
                    customer?.let {
                        if (saveCardCb.isChecked){
                            saveCardInfo(cardNoEt.text.toString().trim(),cardNameEt.text.toString().trim()
                            ,cardCvcEt.text.toString().trim(),cardSkEt.text.toString().trim())
                        }
                        updateBalance(dialog,customer,amountEt)
                    }

                }
            }


        }

        savedCard.setOnClickListener {
            dialog.dismiss()
            showAmountDialog()
        }

    }

    //kayıtlı karta tıklandığında gösterilen dialog
    private fun showAmountDialog(){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_get_amount)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()
        val amountEt = dialog.findViewById<EditText>(R.id.amountEt)
        val confirmBtn = dialog.findViewById<MaterialButton>(R.id.confirmBtn)

        confirmBtn.setOnClickListener {
            customerViewModel.getCustomer(requireContext(),firebaseUser.uid){ customer ->
                customer?.let {
                    updateBalance(dialog,customer,amountEt)

                }

            }


        }


    }

    //bakiye güncelle
    private fun updateBalance(dialog : Dialog, customer : Customer, editText: EditText){
        val map : HashMap<String,Any> = HashMap()
        map["balance"] = customer.balance + editText.text.toString().trim().toDouble()
        FirebaseFirestore.getInstance().collection("Customers")
            .document(firebaseUser.uid)
            .update(map)
            .addOnSuccessListener {
                dialog.dismiss()
                DummyMethods.showMotionToast(requireContext(),"Bakiyeniz Güncellendi","",MotionToastStyle.SUCCESS)
                NavFragment.openNewFragment(ProfileFragment(),requireActivity(),R.id.fragment_container)
            }
    }


    //çıkış yap/
    private fun signOut(){
        firebaseAuth.signOut()
        val intent = Intent(requireContext(),CustomerLoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    //kullanıcı bilgisini al customer objesi döndür
    private fun getCustomer(){
        customerViewModel.getCustomer(requireContext(),firebaseUser.uid){ customer ->
            customer?.let {
                binding.firstLetter.text = customer.firstName.substring(0,1)
                binding.fullName.text = "${customer.firstName} ${customer.lastName}"
                binding.email.text = customer.email
                binding.balance.text = "₺${DummyMethods.formatDoubleNumber(customer.balance)}"
                if (customer.city.equals("")){
                    binding.city.text = "Konum Bilgisi Ekle"

                }else{
                    binding.city.text = "${customer.city} - ${customer.district} \nDeğiştirmek için tıkla"
                }
            }
        }
    }

    //konum izni
    private fun manageLocation(){
        if (!permissionManager.hasLocationPermission(requireContext())){
            permissionManager.requestLocationPermission(requireContext())
            return
        }
        if (!permissionManager.isLocationEnabled(requireContext())){
            permissionManager.openGPSSettings(requireContext())
            return
        }

        getLocation()

    }

    @SuppressLint("MissingPermission")
    //konum bilgisini güncelle updateLocation()
    private fun getLocation() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS açıksa
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val locationHelper = LocationHelper(requireContext())
                        val cityAndDistrict = locationHelper.getCityAndDistrictFromLatLng(location.latitude, location.longitude)
                        cityAndDistrict?.let { (city, district) ->
                            customerViewModel.updateLocation(requireContext(),firebaseUser.uid,city,district
                                ,getCompleteAddressString(location.latitude,location.longitude).toString()
                            )
                            println("Şehir: $city, İlçe: $district")
                        } ?: run {
                            println("Konum bilgisi bulunamadı.")
                        }
                    }
                }
        } else {
           permissionManager.openGPSSettings(requireContext())
        }
    }

    // enlem ve boylamdan tam adresi al
    private fun getCompleteAddressString(latitude: Double, longitude: Double): String? {
        var strAdd = ""
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                val returnedAddress: Address = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
                Log.w("My Current loction address", strReturnedAddress.toString())
            } else {
                Log.w("My Current loction address", "No Address returned!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w("My Current loction address", "Canont get Address!")
        }
        return strAdd
    }

}