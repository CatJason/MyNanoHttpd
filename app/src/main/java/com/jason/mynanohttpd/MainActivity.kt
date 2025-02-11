package com.jason.mynanohttpd

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    lateinit var webView: WebView
    private val pickVideoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri ?: return@registerForActivityResult
            Toast.makeText(applicationContext, uri.toString(), Toast.LENGTH_LONG).show()

            // 检查路径是否是 file:// 或 content:// 协议
            val videoUriPath = uri.toString()

            // 加载视频到 WebView 播放
            val videoHtml = """
                <!DOCTYPE html>package com.jason.mynanohttpd

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    lateinit var webView: WebView
    private val pickVideoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri ?: return@registerForActivityResult
            Toast.makeText(applicationContext, uri.toString(), Toast.LENGTH_LONG).show()

            // 检查路径是否是 file:// 或 content:// 协议
            val videoUriPath = uri.toString()

            // 加载视频到 WebView 播放
            val videoHtml = ""${'"'}
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
            ""${'"'}
            webView.loadDataWithBaseURL(null, videoHtml, "text/html", "UTF-8", null)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 获取 WebView 组件
        webView = findViewById(R.id.webView)

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

        // 检查并申请权限
        checkAndRequestPermissions()
    }

    // 检查并申请权限
    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // 如果没有权限，申请权限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // 如果已经有权限，启动视频选择器
            pickVideoLauncher.launch("video/*")
        }
    }

    // 处理权限申请结果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 权限被授予，启动视频选择器
                pickVideoLauncher.launch("video/*")
            } else {
                // 权限被拒绝，显示提示
                Toast.makeText(this, "需要存储权限才能选择视频", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}
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
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 获取 WebView 组件
        webView = findViewById(R.id.webView)

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

        // 检查并申请权限
        checkAndRequestPermissions()
    }

    // 检查并申请权限
    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // 如果没有权限，申请权限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // 如果已经有权限，启动视频选择器
            pickVideoLauncher.launch("video/*")
        }
    }

    // 处理权限申请结果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 权限被授予，启动视频选择器
                pickVideoLauncher.launch("video/*")
            } else {
                // 权限被拒绝，显示提示
                Toast.makeText(this, "需要存储权限才能选择视频", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}