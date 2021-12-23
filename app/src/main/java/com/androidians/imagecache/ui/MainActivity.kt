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
import android.graphics.drawable.GradientDrawable
import android.net.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.androidians.imagecache.data.local.LocalStorage
import com.androidians.imagecache.data.models.ImageUrl
import com.androidians.imagecache.databinding.ActivityMainBinding
import com.androidians.imagecache.utils.ImageUtils
import com.androidians.imagecache.utils.Utils.IMAGE_CACHE_FILE_NAME
import com.androidians.imagecache.utils.Utils.IMAGE_MIME_TYPE
import com.androidians.imagecache.utils.base64ToByteCode
import com.androidians.imagecache.utils.toBase64String
import java.io.File
import android.widget.Toast
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private val inMemoryCache = HashMap<String, Bitmap?>()
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val downloadManager: DownloadManager by lazy {
        getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    }
    private val connectivityManager: ConnectivityManager by lazy {
        getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val localStorage: LocalStorage by lazy {
        LocalStorage(this)
    }
    private var file: File? = null
    private val viewModel : MainViewModel by viewModels()
    private var randomImageUrl: String = ""
    private var downloadId: Long = 0
    private var imgViewWidth = 0
    private var imgViewHeight = 0
    private var imageUrlList = ArrayList<ImageUrl>()
    // once downloaded image from remote server complete, this receiver gets called
    private val onCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            binding.getImageBtn.isEnabled = true
            queryDownloads()
        }
    }

    // to query the downloaded images
    @SuppressLint("Range")
    private fun queryDownloads() {
        val cursor: Cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        cursor.moveToFirst()
        try {
            val uriStr = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
            val uri = Uri.parse(uriStr)
            file = File(uri.path!!)
            // store in inMemoryCache
            val bmp = ImageUtils.decodeSampledBitmapFromFile(file!!, imgViewWidth, imgViewHeight)
            // store last image as base64
            localStorage.putBitmapInBase64(bmp.toBase64String())
            inMemoryCache[randomImageUrl] =
                ImageUtils.decodeSampledBitmapFromFile(file!!, imgViewWidth, imgViewHeight)
            binding.placeHolderIV.setImageBitmap(inMemoryCache[randomImageUrl])
        } catch (e: Exception) {
            Log.e(TAG, "error while querying downloads", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setObservers()
        getUrlList()
        init()
        registerReceiver(onCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        fetchLastCachedImage()
        registerNetworkConnectivity()
        if (!isOnline()) {
            binding.errorMsgTV.visibility = View.VISIBLE
            binding.getImageBtn.isEnabled = false
        }

    }

    private fun registerNetworkConnectivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(object :
                ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    runOnUiThread {
                        binding.errorMsgTV.visibility = View.GONE
                        binding.getImageBtn.isEnabled = true
                    }
                }

                override fun onLost(network: Network) {
                    runOnUiThread {
                        binding.errorMsgTV.visibility = View.VISIBLE
                        binding.getImageBtn.isEnabled = false
                    }
                }
            })
        }
    }

    // fetch last cached bitmap stored as base64
    private fun fetchLastCachedImage() {
        if (localStorage.getBitmapInBase64().isNotEmpty()) {
            val bmp = BitmapFactory.decodeByteArray(
                localStorage.getBitmapInBase64().base64ToByteCode(), 0, localStorage.getBitmapInBase64()
                    .base64ToByteCode().size
            )
            binding.placeHolderIV.setImageBitmap(bmp)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onCompleteReceiver)
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

            randomImageUrl = getRandomImageUrl()
            val cache = inMemoryCache[randomImageUrl]
            cache?.let {
                Log.d(TAG, "from inMemory cache")
                binding.placeHolderIV.setImageBitmap(it)
                binding.getImageBtn.isEnabled = true
            } ?: downloadImage(randomImageUrl)
        }
    }

    private fun getRandomImageUrl(): String {
        return imageUrlList.random().url
    }

    // to download image through downloadManager
    private fun downloadImage(randomImageUrl: String) {
        Log.d(TAG, "downloadImage(), from remote server - randomImageUrl: $randomImageUrl")
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

    private fun isOnline(): Boolean {
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnected == true
    }
}

const val TAG = "MainActivity"