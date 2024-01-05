package com.app.orderfood.activities.seller

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.transition.Transition
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.orderfood.R
import com.app.orderfood.adapter.seller.GalleryAdapter
import com.app.orderfood.controller.CategoryList
import com.app.orderfood.controller.NavFragment
import com.app.orderfood.databinding.ActivitySellerEditProfileBinding
import com.app.orderfood.fragments.seller.AddFoodFragment
import com.app.orderfood.util.Constants
import com.app.orderfood.util.DummyMethods
import com.app.orderfood.viewmodel.SellerViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle
import java.io.ByteArrayOutputStream

class SellerEditProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySellerEditProfileBinding
    private lateinit var firebaseUser: FirebaseUser
    private val sellerViewModel by viewModel<SellerViewModel>()
    private var category : String? = ""

    private  var myImageBitmap: Bitmap? = null

    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private lateinit var imageUri : Uri

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_GALLERY = 2
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {finish()}

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        initCategorySpinner()
        getSellerData()

        binding.btnUpdate.setOnClickListener {
            if (myImageBitmap!=null){
                editProfile()
            }
        }

        binding.goCamera.setOnClickListener {
            if (DummyMethods.hasPermissionForCamera(this)){
                dispatchTakePictureIntent()

            }
        }

        binding.pickFromGallery.setOnClickListener {
            if (DummyMethods.hasPermissionForGallery(this)) {
                dispatchGalleryIntent()
            }
        }



    }

    private fun editProfile(){
        saveAllData()
    }

    private fun getSellerData(){
        sellerViewModel.getSeller(this,firebaseUser.uid){seller ->
            seller?.let {
                binding.firstNameEt.setText(seller.firstName)
                binding.lastNameEt.setText(seller.lastName)
                binding.emailEt.setText(seller.email)
                binding.restoranName.setText(seller.shopName)

                binding.cityEt.setText(seller.shopCity)
                binding.districtEt.setText(seller.shopDistrict)
                binding.fullAddressEt.setText(seller.fullAddress)

                Glide.with(this)
                    .asBitmap()
                    .load(seller.sellerImage)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                            binding.imageView.setImageBitmap(resource)
                            myImageBitmap = resource
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })

                val defaultItemIndex = CategoryList.getCategoryList().indexOf(seller.category)
                binding.categorySpinner.setSelection(defaultItemIndex)
                category = defaultItemIndex.toString()
            }

        }
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



    private fun dispatchGalleryIntent() {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY)
    }

    private fun dispatchTakePictureIntent() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        imageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )!!
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, 1)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
               REQUEST_IMAGE_CAPTURE -> {
                    // Kamera ile çekim sonucu

                   val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                   myImageBitmap = imageBitmap
                   binding.imageView.setImageBitmap(imageBitmap)

                }
                REQUEST_IMAGE_GALLERY -> {
                    // Galeriden seçim sonucu
                    data?.data?.let { imageUri ->
                        val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        myImageBitmap = imageBitmap
                        binding.imageView.setImageBitmap(imageBitmap)
                    }

                }
            }
        }
    }

    private fun saveAllData() {
        val pd = ProgressDialog(this)
        pd.setCancelable(false)
        pd.show()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                myImageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100 , byteArrayOutputStream)
                val imageData = byteArrayOutputStream.toByteArray()

                val storageReference = firebaseStorage.reference.child("RestaurantImages/${System.currentTimeMillis()}.jpg")

                val uploadTask = storageReference.putBytes(imageData)
                    .addOnProgressListener {
                        val progress: Double =
                            100.0 * it.bytesTransferred / it.totalByteCount
                        val currentProgress = progress.toInt()
                        pd.setMessage("Yükleniyor... $currentProgress%")
                    }.await()
                val imageUrl = uploadTask.storage.downloadUrl.await().toString()
                addData(imageUrl)
                withContext(Dispatchers.Main) {
                    pd.dismiss()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pd.dismiss()
                    Toast.makeText(
                        this@SellerEditProfileActivity,
                        "Resimleri kaydetme hatası: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    private fun addData(imageUrl: String) {

        if (binding.firstNameEt.text.toString().trim().isNotEmpty() && binding.lastNameEt.text.toString().trim().isNotEmpty()
            && binding.restoranName.text.toString().trim().isNotEmpty()
            && binding.cityEt.text.toString().trim().isNotEmpty() && binding.districtEt.text.toString().trim().isNotEmpty()
            && binding.fullAddressEt.text.toString().trim().isNotEmpty()){
            val map : HashMap<String, Any> = HashMap()
            map["firstName"] = binding.firstNameEt.text.toString().trim()
            map["lastName"] = binding.lastNameEt.text.toString().trim()
            map["shopName"] = binding.restoranName.text.toString().trim()
            map["shopCity"] = binding.cityEt.text.toString().trim()
            map["shopDistrict"] = binding.districtEt.text.toString().trim()
            map["fullAddress"] = binding.fullAddressEt.text.toString().trim()
            map["category"] = category.toString()
            map["sellerImage"] = imageUrl


            FirebaseFirestore.getInstance().collection("Sellers").document(firebaseUser.uid)
                .update(map).addOnSuccessListener {
                    DummyMethods.showMotionToast(this,"Bilgileriniz Güncellendi","",
                        MotionToastStyle.SUCCESS)
                }


        }



    }





}