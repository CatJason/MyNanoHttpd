package com.jason.mynanohttpd

import SimpleHttpServer
import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    lateinit var webView: WebView

    private var videoUri: Uri? = null
    private val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        // 用户选择了视频文件后，处理该 URI
        videoUri = uri
        videoUri?.let { uri ->
            val filePath = getFilePathFromUri(uri)
            Log.i("VideoFile", "视频文件路径: $filePath")

            // 弹出 Toast 显示视频文件路径
            filePath?.let {
                Toast.makeText(applicationContext, "视频文件路径: $it", Toast.LENGTH_LONG).show()

                // 检查路径是否是 file:// 或 content:// 协议
                val videoUriPath = if (uri.scheme == "file") {
                    uri.toString()  // 使用 file:// 路径
                } else {
                    // 使用 content:// 路径
                    uri.toString()
                }

                // 加载视频到 WebView 播放
                val videoHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>视频播放</title>
                </head>
                <body>
                    <video width="100%" height="auto" controls>
                        <source src="$videoUriPath" type="video/mp4">
                        您的浏览器不支持播放此视频。
                    </video>
                </body>
                </html>
            """
                webView.loadDataWithBaseURL(null, videoHtml, "text/html", "UTF-8", null)
            } ?: run {
                Toast.makeText(applicationContext, "无法获取视频文件路径", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 启动视频选择器
        pickVideoLauncher.launch("video/*")

        // 获取 WebView 组件
        webView = findViewById(R.id.webView)

        // 启动 HTTP 服务器
        startHttpServer()

        // 设置 WebView 加载页面
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        // 处理 WindowInsets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // 获取文件路径的方法
    private fun getFilePathFromUri(uri: Uri): String? {
        val contentResolver: ContentResolver = contentResolver
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    // 启动 HTTP 服务器
    private fun startHttpServer() {
        Thread {
            try {
                val server = SimpleHttpServer(this)
                server.start()
                Log.d("HTTPServer", "Server started on http://127.0.0.1:8080")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("HTTPServer", "Error starting server: ${e.message}")
                runOnUiThread {
                    Toast.makeText(applicationContext, "服务器启动失败", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    // 确保退出时关闭服务器
    override fun onDestroy() {
        super.onDestroy()
        // Stop HTTP server if necessary
    }
}
