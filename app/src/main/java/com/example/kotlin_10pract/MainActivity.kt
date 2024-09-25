package com.example.kotlin_10pract

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ImageLoaderApp()
                }
            }
        }
    }
}

@Composable
fun ImageLoaderApp() {
    var url by remember { mutableStateOf("") }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Введите URL") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (url.isNotEmpty()) {
                    loadAndSaveImage(context, url) { bitmap ->
                        imageBitmap = bitmap
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Загрузить изображение")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                imageBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Загруженное изображение",
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Text("Изображение не загружено")
            }
        }
    }
}

private fun loadAndSaveImage(context: android.content.Context, url: String, onImageLoaded: (Bitmap?) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val imageBitmap = withContext(Dispatchers.IO) {
            loadImageFromNetwork(url)
        }

        withContext(Dispatchers.IO) {
            saveImageToDisk(context, imageBitmap)
        }

        withContext(Dispatchers.Main) {
            onImageLoaded(imageBitmap)
        }
    }
}

private fun loadImageFromNetwork(url: String): Bitmap? {
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()
    return try {
        val response = client.newCall(request).execute()
        val inputStream: InputStream = response.body!!.byteStream()
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun saveImageToDisk(context: android.content.Context, bitmap: Bitmap?) {
    if (bitmap != null) {
        val file = File(context.getExternalFilesDir(null), "downloaded_image.png")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
    }
}