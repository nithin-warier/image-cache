package com.androidians.imagecache.data.local

import android.content.Context
import android.content.SharedPreferences
import com.androidians.imagecache.utils.Utils
import com.androidians.imagecache.utils.Utils.SHARED_PREFERENCES_BITMAP_IN_BASE64
import com.androidians.imagecache.utils.Utils.SHARED_PREFERENCES_DISK_CACHE_PATH
import com.androidians.imagecache.utils.Utils.SHARED_PREFERENCES_IMAGE_URLS
import com.androidians.imagecache.utils.Utils.SHARED_PREFERENCES_USER_LANGUAGE
import org.json.JSONObject
import java.util.*

class LocalStorage(private val context: Context) {

    private val preferences : SharedPreferences by lazy {
        context.getSharedPreferences(Utils.SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    }
    private val editor: SharedPreferences.Editor by lazy {
        preferences.edit()
    }

    fun getImageUrls() : String {
        return preferences.getString(SHARED_PREFERENCES_IMAGE_URLS, "") ?: ""
    }

    fun putImageUrls(imageUrls: String) {
        editor.putString(SHARED_PREFERENCES_IMAGE_URLS, imageUrls).apply()
    }

    fun getBitmapInBase64() : String {
        return preferences.getString(SHARED_PREFERENCES_BITMAP_IN_BASE64, "") ?: ""
    }

    fun putBitmapInBase64(bitmapInBase64: String) {
        editor.putString(SHARED_PREFERENCES_BITMAP_IN_BASE64, bitmapInBase64).apply()
    }

    fun getUserLanguage() : String {
        return preferences.getString(SHARED_PREFERENCES_USER_LANGUAGE, Locale.getDefault().language) ?: "en"
    }

    fun putUserLanguage(userLanguage: String) {
        editor.putString(SHARED_PREFERENCES_USER_LANGUAGE, userLanguage).apply()
    }

    fun getDiskCachePath() : String {
        return preferences.getString(SHARED_PREFERENCES_DISK_CACHE_PATH, JSONObject().toString()) ?: JSONObject().toString()
        /*val outputMap = HashMap<String, String>()
        val jsonStr = preferences.getString(SHARED_PREFERENCES_DISK_CACHE_PATH, JSONObject().toString())
            ?: JSONObject().toString()
        val jsonObject = JSONObject(jsonStr)
        val keysItr = jsonObject.keys()
        while (keysItr.hasNext()) {
            val key = keysItr.next()
            val value = jsonObject.getString(key)
            outputMap[key] = value
        }
        return outputMap*/
    }

    fun putDiskcachePath(diskCachePath: String) {
        editor.putString(SHARED_PREFERENCES_DISK_CACHE_PATH, diskCachePath).apply()
    }

}