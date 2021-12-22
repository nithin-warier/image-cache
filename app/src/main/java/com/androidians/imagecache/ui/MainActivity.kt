package com.androidians.imagecache.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.androidians.imagecache.data.local.LocalStorage
import com.androidians.imagecache.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val viewModel : MainViewModel by viewModels()
    private var randomImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setObservers()
        getUrlList()
        init()
    }

    private fun setObservers() {
        viewModel.imageUrlsLiveData.observe(this, { imageUrl ->
            Log.e("MainActy", "imageUrl: $imageUrl")
            randomImageUrl = imageUrl
            binding.getImageBtn.isEnabled = true
        })
    }

    private fun init() {
        binding.getImageBtn.setOnClickListener {
            viewModel.getImageFromUrl(randomImageUrl)
        }
    }

    private fun getUrlList() {
        val localStorage = LocalStorage(this)
        if (localStorage.getImageUrls().isNotEmpty()) {
            viewModel.getRandomImageUrl(localStorage.getImageUrls())
        } else {
            viewModel.getImageUrlFromAssets()
            viewModel.getRandomImageUrl(localStorage.getImageUrls())
        }
    }
}