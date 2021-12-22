package com.androidians.imagecache.ui

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.androidians.imagecache.data.local.LocalStorage
import com.androidians.imagecache.databinding.ActivityMainBinding
import com.androidians.imagecache.utils.Utils.IMAGE_CACHE_FILE_NAME
import java.io.File

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val downloadManager: DownloadManager by lazy {
        getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    }
    private val viewModel : MainViewModel by viewModels()
    private var randomImageUrl: String = ""
    private val onCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            binding.getImageBtn.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setObservers()
        getUrlList()
        init()
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(onCompleteReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onCompleteReceiver)
    }

    // observers to get the random url
    private fun setObservers() {
        viewModel.imageUrlsLiveData.observe(this, { imageUrl ->
            randomImageUrl = imageUrl
            binding.getImageBtn.isEnabled = true
        })
    }

    // to initialize
    private fun init() {
        binding.getImageBtn.setOnClickListener {
            downloadImage(randomImageUrl)
        }
    }

    private fun downloadImage(randomImageUrl: String) {
        val file = File(getExternalFilesDir(null), IMAGE_CACHE_FILE_NAME)
        val downloadUri: Uri = Uri.parse(randomImageUrl)
        val request = DownloadManager.Request(downloadUri)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false)
            .setTitle(randomImageUrl)
            .setMimeType("image/*")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(file.toUri())
        downloadManager.enqueue(request)
    }

    // first try to get the url list from cache, else get it from assets
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