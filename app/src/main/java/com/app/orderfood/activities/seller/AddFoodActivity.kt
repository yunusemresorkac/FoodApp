package com.app.orderfood.activities.seller

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.app.orderfood.R
import com.app.orderfood.controller.CategoryList
import com.app.orderfood.controller.NavFragment
import com.app.orderfood.databinding.ActivityAddFoodBinding
import com.app.orderfood.fragments.seller.AddFoodFragment
import com.app.orderfood.util.Constants
import com.app.orderfood.util.DummyMethods
import com.app.orderfood.viewmodel.SellerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream

class AddFoodActivity : AppCompatActivity() {


    private lateinit var binding : ActivityAddFoodBinding
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
        binding = ActivityAddFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {finish()}



        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        initCategorySpinner()
        binding.addFoodBtn.setOnClickListener {
            if (myImageBitmap!=null){
                addFood()
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
    private fun addFood(){
        if (binding.titleEt.text.toString().trim().isEmpty()){
            binding.titleEt.error = "Lütfen Başlık Girin"
            return
        }
        if (binding.descEt.text.toString().trim().isEmpty()){
            binding.descEt.error = "Lütfen Açıklama Girin"
            return
        }
        if (binding.priceEt.text.toString().trim().isEmpty()){
            binding.priceEt.error = "Lütfen Fiyat Girin"
            return
        }


        saveAllData()

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
        val pd = ProgressDialog(this@AddFoodActivity)
        pd.setCancelable(false)
        pd.show()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                myImageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100 , byteArrayOutputStream)
                val imageData = byteArrayOutputStream.toByteArray()

                val storageReference = firebaseStorage.reference.child("FoodImages/${System.currentTimeMillis()}.jpg")

                val uploadTask = storageReference.putBytes(imageData)
                    .addOnProgressListener {
                        val progress: Double =
                            100.0 * it.bytesTransferred / it.totalByteCount
                        val currentProgress = progress.toInt()
                        pd.setMessage("Yükleniyor... $currentProgress%")
                    }.await()
                val imageUrl = uploadTask.storage.downloadUrl.await().toString()

                withContext(Dispatchers.Main) {
                    addData(imageUrl)
                    pd.dismiss()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pd.dismiss()
                    Toast.makeText(
                        this@AddFoodActivity,
                        "Resimleri kaydetme hatası: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun addData(imageUrl: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {

                if (binding.discountEt.text.toString().toDouble() >= 1){
                    updateDiscountStatus()
                }
                 sellerViewModel.addFood(
                    binding.titleEt.text.toString().trim(),
                    binding.descEt.text.toString().trim(),
                    category.toString(),
                    System.currentTimeMillis().toString(),
                    firebaseUser.uid,
                    Constants.ACTIVE_PRODUCT,
                    imageUrl,
                    binding.priceEt.text.toString().toDouble(),
                    binding.discountEt.text.toString().toDouble(),
                     binding.stockEt.text.toString().toInt(),
                    this@AddFoodActivity
                ){
                    NavFragment.openNewFragment(
                        AddFoodFragment(),
                        this@AddFoodActivity,
                        R.id.fragment_container
                    )
                }


            } catch (e: Exception) {
                Toast.makeText(
                    this@AddFoodActivity,
                    "Yiyecek eklerken bir hata oluştu: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    private fun updateDiscountStatus(){
        FirebaseFirestore.getInstance().collection("Sellers").document(firebaseUser.uid)
            .update("discountStatus",1)
    }

    }