package com.app.orderfood.fragments.seller

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.orderfood.activities.customer.CustomerLoginActivity
import com.app.orderfood.activities.seller.SellerEditProfileActivity
import com.app.orderfood.databinding.FragmentSellerProfileBinding
import com.app.orderfood.permissions.PermissionManager
import com.app.orderfood.util.DummyMethods
import com.app.orderfood.util.LocationHelper
import com.app.orderfood.viewmodel.SellerViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class SellerProfileFragment : Fragment() {

    private lateinit var binding : FragmentSellerProfileBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseAuth : FirebaseAuth
    private val sellerViewModel by viewModel<SellerViewModel>()
    private lateinit var permissionManager : PermissionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSellerProfileBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionManager = PermissionManager()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        getSeller()

        binding.signOutBtn.setOnClickListener {
            signOut()
        }

        binding.location.setOnClickListener {
            manageLocation()
        }


        binding.editProfileBtn.setOnClickListener {
            val intent = Intent(requireContext(), SellerEditProfileActivity::class.java)
            startActivity(intent)
        }


    }


    private fun signOut(){
        firebaseAuth.signOut()
        val intent = Intent(requireContext(), CustomerLoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun getSeller(){
        sellerViewModel.getSeller(requireContext(),firebaseUser.uid){ seller ->
            seller?.let {
                binding.firstLetter.text = seller.firstName.substring(0,1)
                binding.fullName.text = "${seller.firstName} ${seller.lastName}"
                binding.location.text = seller.shopCity
                binding.email.text = seller.email
                binding.shopName.text = seller.shopName
                binding.category.text = seller.category


                if (seller.shopCity.equals("")){
                    binding.location.text = "Konum Bilgisi Ekle"
                }else{
                    binding.location.text = "${seller.shopCity} - ${seller.shopDistrict} \nDeğiştirmek için tıkla"
                }

                if (seller.numberOfRates == 0){
                    binding.rate.text = "Değerlendirme Yok"
                }else{
                    val rate = seller.rate / seller.numberOfRates
                    binding.rate.text = "${DummyMethods.formatDoubleNumber(rate)} (${seller.numberOfRates})"

                }


            }

        }
    }

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
                            sellerViewModel.updateLocation(requireContext(),firebaseUser.uid,city,district
                            ,getCompleteAddressString(location.latitude,location.longitude).toString()
                                ,location.latitude,location.longitude
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