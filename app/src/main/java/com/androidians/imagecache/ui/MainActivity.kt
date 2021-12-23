package com.androidians.imagecache.ui

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.androidians.imagecache.data.local.LocalStorage
import com.androidians.imagecache.data.models.ImageUrl
import com.androidians.imagecache.databinding.ActivityMainBinding
import com.androidians.imagecache.utils.ImageUtils
import com.androidians.imagecache.utils.Utils.IMAGE_CACHE_FILE_NAME
import com.androidians.imagecache.utils.Utils.IMAGE_MIME_TYPE
import java.io.File

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val downloadManager: DownloadManager by lazy {
        getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    }
    private var file: File? = null
    private val inMemoryCache = HashMap<String, Bitmap?>()
    private val viewModel : MainViewModel by viewModels()
    private var randomImageUrl: String = ""
    private var downloadId: Long = 0
    private var imgViewWidth = 0
    private var imgViewHeight = 0
    private var imageUrlList = ArrayList<ImageUrl>()
    private val onCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            binding.getImageBtn.isEnabled = true
            queryDownloads()
        }
    }

    @SuppressLint("Range")
    private fun queryDownloads() {
        val cursor: Cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        cursor.moveToFirst()
        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        val uriStr = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
        val uri = Uri.parse(uriStr)
        file = null
        file = File(uri.path!!)
        inMemoryCache[randomImageUrl] =
            ImageUtils.decodeSampledBitmapFromFile(file!!, imgViewWidth, imgViewHeight)
        binding.placeHolderIV.setImageBitmap(inMemoryCache.get(randomImageUrl))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setObservers()
        getUrlList()
        init()
        registerReceiver(onCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onCompleteReceiver)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

//        binding.placeHolderIV.setImageBitmap(ImageUtils
//            .decodeSampledBitmapFromFile(file!!, imgViewWidth, imgViewHeight))
    }

    // observers to get the random url
    private fun setObservers() {
        viewModel.imageUrlsLiveData.observe(this, { imageUrls ->
            imageUrlList.addAll(imageUrls)
            if (randomImageUrl.isNotEmpty()) {
                downloadImage(randomImageUrl)
            }
        })
    }

    // to initialize
    private fun init() {
        binding.getImageBtn.setOnClickListener {
            imgViewWidth = binding.placeHolderIV.width
            imgViewHeight = binding.placeHolderIV.height
            binding.getImageBtn.isEnabled = false

            val cache = inMemoryCache[randomImageUrl]
            cache?.let {
                binding.placeHolderIV.setImageBitmap(it)
            } ?: downloadImage(randomImageUrl)

            //downloadImage(getRandomImageUrl())
        }
    }

    private fun getRandomImageUrl(): String {
        return imageUrlList.random().url
    }

    // to download image through downloadManager
    private fun downloadImage(randomImageUrl: String) {
        val file = File(getExternalFilesDir(null), IMAGE_CACHE_FILE_NAME)
        val downloadUri: Uri = Uri.parse(randomImageUrl)
        val request = DownloadManager.Request(downloadUri)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false)
            .setTitle(randomImageUrl)
            .setMimeType(IMAGE_MIME_TYPE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(file.toUri())
        downloadId = downloadManager.enqueue(request)
    }

    // first try to get the url list from cache, else get it from assets
    private fun getUrlList() {
        val localStorage = LocalStorage(this)
        if (localStorage.getImageUrls().isNotEmpty()) {
            viewModel.getRandomImageUrl(localStorage.getImageUrls())
        } else {
            viewModel.getImageUrlFromAssets()
        }
    }
}