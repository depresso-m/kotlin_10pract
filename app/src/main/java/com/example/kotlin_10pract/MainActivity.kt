package com.example.kotlin_10pract

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageLoaderApp() {
    val navController = rememberNavController()
    val imageBitmaps = remember { mutableStateListOf<Bitmap>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = getCurrentRoute(navController)) }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.navigate("input_url") }
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Ввод URL")
                    }
                    IconButton(
                        onClick = { navController.navigate("show_images") }
                    ) {
                        Icon(Icons.Default.List, contentDescription = "Показать изображения")
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "input_url",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("input_url") {
                InputUrlScreen(navController, imageBitmaps)
            }
            composable("show_images") {
                ShowImagesScreen(imageBitmaps)
            }
        }
    }
}

@Composable
fun InputUrlScreen(navController: NavHostController, imageBitmaps: MutableList<Bitmap>) {
    var url by remember { mutableStateOf("") }
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
                        if (bitmap != null) {
                            imageBitmaps.add(bitmap)
                            navController.navigate("show_images")
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Загрузить изображение")
        }
    }
}

@Composable
fun ShowImagesScreen(imageBitmaps: List<Bitmap>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(imageBitmaps) { bitmap ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(300.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Загруженное изображение",
                        modifier = Modifier.fillMaxSize()
                    )
                }
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

@Composable
fun getCurrentRoute(navController: NavHostController): String {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    return when (currentRoute) {
        "input_url" -> "Ввод URL"
        "show_images" -> "Показать изображения"
        else -> "Ввод URL" // Значение по умолчанию, если маршрут не определен
    }
}