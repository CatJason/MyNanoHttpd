package com.jason.mynanohttpd

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnRequestPermission: Button
    private val videoList = mutableListOf<VideoItem>() // 存储视频信息

    // 用于启动权限请求的 Launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // 权限被授予，加载视频列表
                loadVideoList()
            } else {
                Toast.makeText(this, "需要权限才能访问视频", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化 WebView
        webView = findViewById(R.id.webView)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        // 初始化 RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = VideoAdapter(videoList) { videoUri ->
            // 用户选择视频后，加载到 WebView 播放
            loadVideoInWebView(videoUri)
        }
        recyclerView.adapter = adapter

        // 初始化按钮
        btnRequestPermission = findViewById(R.id.btnRequestPermission)
        btnRequestPermission.setOnClickListener {
            // 点击按钮时重新请求权限
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
        }

        // 检查并申请权限
        checkAndRequestPermissions()
    }

    // 检查并申请权限
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 及以上，检查 READ_MEDIA_VIDEO 权限
            when {
                // 如果已经有全部访问权限
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED -> {
                    loadVideoList()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
                }
            }
        } else {
            // Android 12 及以下，不需要额外权限
            loadVideoList()
            btnRequestPermission.visibility = android.view.View.GONE // 隐藏按钮
        }
    }

    // 从 MediaStore 加载视频列表
    private fun loadVideoList() {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                // 将视频信息添加到列表
                videoList.add(VideoItem(name, uri))
            }

            // 更新 RecyclerView
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    // 在 WebView 中加载视频
    private fun loadVideoInWebView(videoUri: Uri) {
        val videoHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>视频播放</title>
            </head>
            <body>
                <video width="100%" height="auto" controls>
                    <source src="$videoUri" type="video/mp4">
                    您的浏览器不支持播放此视频。
                </video>
            </body>
            </html>
        """
        webView.loadDataWithBaseURL(null, videoHtml, "text/html", "UTF-8", null)
    }

    // 获取视频缩略图
    private fun getVideoThumbnail(videoId: Long): Bitmap? {
        return MediaStore.Video.Thumbnails.getThumbnail(
            contentResolver,
            videoId,
            MediaStore.Video.Thumbnails.MINI_KIND,
            null
        )
    }

    // RecyclerView 适配器
    private inner class VideoAdapter(
        private val videoList: List<VideoItem>,
        private val onItemClick: (Uri) -> Unit
    ) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

        inner class VideoViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
            val thumbnail: ImageView = itemView.findViewById(R.id.videoThumbnail)
            val name: android.widget.TextView = itemView.findViewById(R.id.videoName)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VideoViewHolder {
            val view = layoutInflater.inflate(R.layout.item_video, parent, false)
            return VideoViewHolder(view)
        }

        override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
            val videoItem = videoList[position]
            holder.name.text = videoItem.name

            // 加载视频缩略图
            val videoId = ContentUris.parseId(videoItem.uri)
            val thumbnail = getVideoThumbnail(videoId)
            if (thumbnail != null) {
                holder.thumbnail.setImageBitmap(thumbnail)
            } else {
                holder.thumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // 设置点击事件
            holder.itemView.setOnClickListener {
                onItemClick(videoItem.uri)
            }
        }

        override fun getItemCount(): Int {
            return videoList.size
        }
    }

    // 视频信息数据类
    data class VideoItem(val name: String, val uri: Uri)
}