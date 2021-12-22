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

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val imageUrls = MutableLiveData<String>()
    val imageUrlsLiveData: LiveData<String> get() = imageUrls

    fun getImageUrlFromAssets() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val jsonString = getApplication<Application>().assets.open(ASSETS_IMAGE_URL_FILE_NAME).bufferedReader().use { it.readText() }
                val localStorage = LocalStorage(getApplication<Application>())
                localStorage.putImageUrls(jsonString)
            }
        }
    }

    fun getRandomImageUrl(imageUrlsJson: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val gson = Gson()
                val listItemType = object : TypeToken<List<ImageUrl>>() {}.type
                val list = ArrayList<ImageUrl>()
                list.addAll(gson.fromJson(imageUrlsJson, listItemType))
                val randomIndex = Random.nextInt(list.size)
                imageUrls.postValue(list[randomIndex].url)
            }
        }
    }

    fun getImageFromUrl(imageUrl: String) {

    }

}