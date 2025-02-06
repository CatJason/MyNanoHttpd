import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import com.jason.mynanohttpd.MainActivity
import fi.iki.elonen.NanoHTTPD

class SimpleHttpServer(val context: Context) : NanoHTTPD("127.0.0.1", 8080) {

    // 处理 HTTP 请求
    override fun serve(session: IHTTPSession?): Response {
        val uri = session?.uri ?: return newFixedLengthResponse("Invalid Request")

        // 如果请求的是视频文件
        if (uri == "/video") {
            return serveVideo(session)
        }

        // 默认返回一个简单的HTML页面
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>My Custom Webpage</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f4f4f4;
                        color: #333;
                        text-align: center;
                        padding: 50px;
                    }
                    h1 {
                        color: #1e90ff;
                    }
                </style>
            </head>
            <body>
                <h1>Welcome to My Custom Web Server!</h1>
                <p>This page is served from a NanoHTTPD server running on Android.</p>
                <p><a href="/video">Click here to watch the video</a></p>
            </body>
            </html>
        """.trimIndent()

        return newFixedLengthResponse(htmlContent)
    }

    // 读取并返回本地视频文件
    private fun serveVideo(session: IHTTPSession): Response {
        // 请求权限，Android 14及以上需要READ_EXTERNAL_STORAGE权限
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.e("miaomiaomiao", "没有存储读取权限")
            return newFixedLengthResponse("没有权限读取视频文件")
        }

        // 调用系统文件选择器
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        (context as MainActivity).startActivityForResult(intent, PICK_VIDEO_REQUEST)

        return newFixedLengthResponse("请选择视频文件")
    }

    // 在 MainActivity 中接收选择的文件
    companion object {
        const val PICK_VIDEO_REQUEST = 1
    }
}

