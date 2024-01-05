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
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.orderfood.R
import com.app.orderfood.adapter.seller.GalleryAdapter
import com.app.orderfood.controller.CategoryList
import com.app.orderfood.databinding.ActivityEditProductBinding
import com.app.orderfood.util.Constants
import com.app.orderfood.util.DummyMethods
import com.app.orderfood.viewmodel.SellerViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import www.sanju.motiontoast.MotionToastStyle
import java.io.ByteArrayOutputStream
import java.util.Base64

class EditProductActivity : AppCompatActivity() {

    private lateinit var binding : ActivityEditProductBinding
    private lateinit var productId : String
    private lateinit var sellerId : String

    private val sellerViewModel by viewModel<SellerViewModel>()
    private var category : String? = ""
    private lateinit var firebaseUser: FirebaseUser
    private  var myImageBitmap: Bitmap? = null

    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private lateinit var imageUri : Uri

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_GALLERY = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {finish()}



        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        productId = intent.getStringExtra("productId")!!
        sellerId = intent.getStringExtra("sellerId")!!

        initCategorySpinner()
        getFoodById(productId)


        binding.deleteProductBtn.setOnClickListener {
            deleteProduct(productId)
        }

        binding.updateBtn.setOnClickListener {
            saveAllData()
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


    //ürün güncelleme sayfası. intent ile alınan id üzerinden işlem yap

    private fun updateStatus(status : Int){
        CoroutineScope(Dispatchers.IO).launch {
            val map : HashMap<String,Any> = HashMap()
            map["status"] = status
            FirebaseFirestore.getInstance().collection("Foods").document(productId)
                .update(map)
                .addOnSuccessListener {
                    if (status == Constants.ACTIVE_PRODUCT){
                        DummyMethods.showMotionToast(this@EditProductActivity,"Ürün Tekrar Yayına Alındı"
                        ,"",MotionToastStyle.SUCCESS)
                    }else{
                        DummyMethods.showMotionToast(this@EditProductActivity,"Ürün Yayından Kaldırıldı"
                            ,"",MotionToastStyle.SUCCESS)
                    }
                }
        }
    }

    private fun deleteProduct(id : String){
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseFirestore.getInstance().collection("Foods").document(id)
                .delete().addOnSuccessListener {
                    DummyMethods.showMotionToast(this@EditProductActivity,"Ürün Silindi"
                        ,"",MotionToastStyle.SUCCESS)
                    finish()
                }
        }

    }

    private fun getFoodById(id : String){
        sellerViewModel.getFoodById(id,sellerId){ food ->
            food?.let {
                binding.titleEt.setText(food.title)
                binding.descEt.setText(food.description)
                binding.priceEt.setText(food.price.toString())
                binding.discountEt.setText(food.discountPercentage.toString())
                binding.stockEt.setText(food.stock.toString())


                Glide.with(this)
                    .asBitmap()
                    .load(food.imageUrl)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                            binding.imageView.setImageBitmap(resource)
                            myImageBitmap = resource
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })

                val defaultItemIndex = CategoryList.getCategoryList().indexOf(food.category)
                binding.categorySpinner.setSelection(defaultItemIndex)
                category = defaultItemIndex.toString()

                if (food.status == Constants.ACTIVE_PRODUCT){
                    binding.unpublishBtn.text = "Yayından Kaldır"
                    binding.unpublishBtn.setOnClickListener {
                        updateStatus(Constants.INACTIVE_PRODUCT)
                    }
                }else{
                    binding.unpublishBtn.text = "Tekrar Yayına Al"
                    binding.unpublishBtn.setOnClickListener {
                        updateStatus(Constants.ACTIVE_PRODUCT)
                    }
                }


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

                val storageReference = firebaseStorage.reference.child("FoodImages/${System.currentTimeMillis()}.jpg")

                val uploadTask = storageReference.putBytes(imageData)
                    .addOnProgressListener {
                        val progress: Double =
                            100.0 * it.bytesTransferred / it.totalByteCount
                        val currentProgress = progress.toInt()
                        pd.setMessage("Yükleniyor... $currentProgress%")
                    }.await()
                val imageUrl = uploadTask.storage.downloadUrl.await().toString()

                updateFood(productId,imageUrl)
                withContext(Dispatchers.Main) {
                    pd.dismiss()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pd.dismiss()
                    Toast.makeText(
                        this@EditProductActivity,
                        "Resimleri kaydetme hatası: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }


    private fun updateFood(id : String,imageUrl: String){
        CoroutineScope(Dispatchers.IO).launch {
            val map : HashMap<String,Any> = HashMap()
            map["category"] = category!!
            map["description"] = binding.descEt.text.toString().trim()
            map["title"] = binding.titleEt.text.toString().trim()
            map["price"] = binding.priceEt.text.toString().toDouble()
            map["stock"] = binding.stockEt.text.toString().toInt()
            map["imageUrl"] = imageUrl
            map["discountPercentage"] = binding.discountEt.text.toString().toDouble()

            if (binding.discountEt.text.toString().toDouble() >= 1){
                updateDiscountStatus()
            }
            FirebaseFirestore.getInstance().collection("Foods")
                .document(firebaseUser.uid).collection("Foods")
                .document(id).update(map)
                .addOnSuccessListener {

                    DummyMethods.showMotionToast(this@EditProductActivity,"Ürün Güncellendi"
                        ,"",MotionToastStyle.SUCCESS)
                    finish()
                }

        }
    }

    private fun updateDiscountStatus(){
        FirebaseFirestore.getInstance().collection("Sellers").document(firebaseUser.uid)
            .update("discountStatus",1)
    }








}