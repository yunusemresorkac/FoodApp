package com.app.orderfood.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.core.content.res.ResourcesCompat
import com.app.orderfood.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.random.Random

class DummyMethods {

    companion object{

        fun stringTobitMap(encodedString: String?): Bitmap? {
            return try {
                val encodeByte =
                    Base64.decode(encodedString, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
            } catch (e: Exception) {
                e.message
                null
            }
        }

        fun hasPermissionForGallery(context: Context ): Boolean {
            var checkPermission = false
            Dexter.withActivity(context as Activity?)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        checkPermission = true
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        checkPermission = false
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).check()
            return checkPermission
        }

        fun hasPermissionForCamera(context: Context ): Boolean {
            var checkPermission = false
            Dexter.withActivity(context as Activity?)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        checkPermission = true
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        checkPermission = false
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).check()
            return checkPermission
        }



        fun generateRandomString(length: Int): String {
            val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            return (1..length)
                .map { Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
        }

        fun convertTime(time: Long): String? {
            val formatter = SimpleDateFormat("dd MMMM k:mm")
            return formatter.format(Date(time.toString().toLong()))
        }

        fun showMotionToast(context: Context, title :String, message: String, style: MotionToastStyle) =
            MotionToast.createToast(
                context as Activity,
                title,
                message,
                style,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(context, R.font.montserrat))

        fun formatDoubleNumber(number: Double):String {
            return String.format("%.1f", number)
        }

    }
}