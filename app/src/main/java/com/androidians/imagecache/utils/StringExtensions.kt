package com.androidians.imagecache.utils

import android.util.Base64

fun String.base64ToByteCode() = Base64.decode(this.substring(this.indexOf(",")  + 1), Base64.DEFAULT)