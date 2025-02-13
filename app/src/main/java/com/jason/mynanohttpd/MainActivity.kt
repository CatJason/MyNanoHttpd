package com.jason.mynanohttpd

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var originalVideoPath: TextView
    private lateinit var copiedVideoPath: TextView
    private lateinit var htmlContent: TextView

    // 用于启动权限请求的 Launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // 权限被授予，加载视频
                loadVideoInWebView()
            } else {
                Toast.makeText(this, "需要权限才能访问视频", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化 WebView 和 TextView
        webView = findViewById(R.id.webView)
        originalVideoPath = findViewById(R.id.originalVideoPath)
        copiedVideoPath = findViewById(R.id.copiedVideoPath)
        htmlContent = findViewById(R.id.htmlContent)

        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        // 检查并申请权限
        checkAndRequestPermissions()
    }

    // 检查并申请权限
    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // 权限已授予
            loadVideoInWebView()
        } else {
            // 请求权限
            requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE)
        }
    }

    private fun copyAssetToAppDirectory(assetName: String, destFile: File) {
        try {
            val inputStream = assets.open(assetName)
            val outputStream = FileOutputStream(destFile)

            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            inputStream.close()
            outputStream.close()

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "视频文件复制失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadVideoInWebView() {
        val originalVideoName = "test.mp4"  // assets 中的视频文件名
        originalVideoPath.text = "原件视频路径: assets/$originalVideoName"

        // 将 assets 下的 test.mp4 复制到应用的私有目录
        val videoFile = File(filesDir, "test.mp4")
        if (!videoFile.exists()) {
            copyAssetToAppDirectory("test.mp4", videoFile)
        }

        // 使用 FileProvider 获取 content:// URI
        val videoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", videoFile)

        // 显示视频复制路径（实际路径）
        copiedVideoPath.text = "视频复制路径: ${videoFile.absolutePath}"

        // 创建视频的 HTML 代码
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
        // 显示传给 H5 的内容（content:// URI）
        htmlContent.text = "传给 H5 的内容: $videoUri"

        // 加载视频到 WebView
        webView.loadDataWithBaseURL(null, videoHtml, "text/html", "UTF-8", null)
    }

}

