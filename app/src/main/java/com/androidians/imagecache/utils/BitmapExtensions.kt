package com.androidians.imagecache.utils

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

// extension function to convert bitmap to base64 string
fun Bitmap.toBase64String():String{
    ByteArrayOutputStream().apply {
        compress(Bitmap.CompressFormat.JPEG,10,this)
        return Base64.encodeToString(toByteArray(),Base64.DEFAULT)
    }
}