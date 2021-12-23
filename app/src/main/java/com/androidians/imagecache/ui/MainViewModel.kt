package com.androidians.imagecache.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.androidians.imagecache.data.local.LocalStorage
import com.androidians.imagecache.data.models.ImageUrl
import com.androidians.imagecache.utils.Utils.ASSETS_IMAGE_URL_FILE_NAME
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.os.Environment
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val imageUrls = MutableLiveData<List<ImageUrl>>()
    val imageUrlsLiveData: LiveData<List<ImageUrl>> get() = imageUrls

    // to get the imageUrls from assets and store in preferences
    fun getImageUrlFromAssets() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val jsonString = getApplication<Application>().assets.open(ASSETS_IMAGE_URL_FILE_NAME).bufferedReader().use { it.readText() }
                val localStorage = LocalStorage(getApplication<Application>())
                localStorage.putImageUrls(jsonString)
                getRandomImageUrl(localStorage.getImageUrls())
            }
        }
    }

    // get the random url from the lsit
    fun getRandomImageUrl(imageUrlsJson: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val gson = Gson()
                val listItemType = object : TypeToken<List<ImageUrl>>() {}.type
                val list = ArrayList<ImageUrl>()
                list.addAll(gson.fromJson(imageUrlsJson, listItemType))
                imageUrls.postValue(list)
            }
        }
    }

}