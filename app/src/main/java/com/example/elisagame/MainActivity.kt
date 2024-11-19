package com.example.elisagame

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.elisagame.functions.ImageSplitter
import com.example.elisagame.functions.MovementLogic
import com.example.elisagame.functions.PuzzleShuffler
import com.example.elisagame.functions.PuzzleValidator
import com.example.elisagame.ui.theme.ElisaGameTheme
import java.io.InputStream
import androidx.exifinterface.media.ExifInterface
import android.graphics.Matrix

class MainActivity : ComponentActivity() {

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionsHandler: PermissionsHandler
    private var selectedImageUri by mutableStateOf<Uri?>(null)
    private var tiles: MutableList<Bitmap?> = mutableListOf()
    private var originalTiles: List<Bitmap?> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize PermissionsHandler
        permissionsHandler = PermissionsHandler(
            activity = this,
            onPermissionGranted = { Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show() }
        )

        // Initialize gallery launcher
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedImageUri = result.data?.data
            }
        }

        // Check and request permissions
        if (!permissionsHandler.checkPermission()) {
            permissionsHandler.requestPermission()
        }

        // Compose UI setup
        setContent {
            GameScreen()
        }
    }

    @Composable
    fun GameScreen() {
        var tiles by remember { mutableStateOf<MutableList<Bitmap?>>(mutableListOf()) }
        var originalTiles by remember { mutableStateOf<List<Bitmap?>>(emptyList()) }
        var emptyIndex by remember { mutableStateOf(-1) }
        var isPuzzleSolved by remember { mutableStateOf(false) }
        var isInitialized by remember { mutableStateOf(false) }
        var imageWidth by remember { mutableStateOf(0) }
        var imageHeight by remember { mutableStateOf(0) }

        // Timer state
        var timer by remember { mutableStateOf(0) } // Time in seconds
        var isTimerRunning by remember { mutableStateOf(false) }

        // Coroutine to manage the timer
        LaunchedEffect(isTimerRunning) {
            if (isTimerRunning) {
                while (isTimerRunning) {
                    kotlinx.coroutines.delay(1000L)
                    timer++
                }
            }
        }

        LaunchedEffect(selectedImageUri) {
            if (selectedImageUri != null && !isInitialized) {
                val bitmap = getBitmapFromUri(selectedImageUri!!)
                bitmap?.let {
                    imageWidth = it.width
                    imageHeight = it.height

                    tiles = ImageSplitter.splitImage(it).toMutableList()
                    originalTiles = tiles.toList()

                    PuzzleShuffler.shufflePuzzle(tiles)
                    emptyIndex = tiles.indexOf(null)

                    isInitialized = true
                    isPuzzleSolved = false

                    // Start the timer
                    timer = 0
                    isTimerRunning = true

                    Log.d("GameScreen", "Tiles initialized: $tiles")
                    Log.d("GameScreen", "Empty index: $emptyIndex")
                }
            }
        }

        ElisaGameTheme {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                content = { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        // Greeting Text
                        Greeting(name = "Welcome to ElisaGame!")

                        Spacer(modifier = Modifier.height(16.dp))

                        // Timer Display
                        Text(
                            text = "Timer: ${timer}s",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Button to Open Gallery
                        Button(
                            onClick = {
                                if (permissionsHandler.checkPermission()) {
                                    openGallery()
                                } else {
                                    permissionsHandler.requestPermission()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Open Gallery")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Render Puzzle Grid UI
                        if (tiles.isNotEmpty()) {
                            PuzzleGridUI(
                                currentPieces = tiles,
                                emptyIndex = emptyIndex,
                                onTileClick = { clickedIndex ->
                                    if (MovementLogic.canMove(clickedIndex, emptyIndex, 3, 3)) {
                                        tiles[emptyIndex] = tiles[clickedIndex]
                                        tiles[clickedIndex] = null
                                        emptyIndex = clickedIndex

                                        if (PuzzleValidator.isSolved(tiles, originalTiles)) {
                                            isPuzzleSolved = true
                                            tiles[emptyIndex] = originalTiles[emptyIndex]

                                            // Stop the timer
                                            isTimerRunning = false

                                            Toast.makeText(
                                                this@MainActivity,
                                                "Puzzle Solved in $timer seconds!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                isPuzzleSolved = isPuzzleSolved,
                                onPuzzleSolved = {},
                                imageWidth = imageWidth,
                                imageHeight = imageHeight
                            )
                        } else {
                            Text(
                                text = "Select an image to start!",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            )
        }
    }




    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Correct the orientation using Exif data
            inputStream?.close() // Close the input stream to avoid resource leaks
            val exif = uri.path?.let { ExifInterface(contentResolver.openInputStream(uri)!!) }
            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    private fun cleanupResources() {
        // Release existing bitmap resources
        ImageSplitter.cleanupBitmaps(tiles)
        tiles.clear()
        originalTiles = listOf()
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupResources()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ElisaGameTheme {
        Greeting("Welcome to ElisaGame!")
    }
}
