package com.example.elisagame

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
        var timer by remember { mutableStateOf(0L) }
        var isTimerRunning by remember { mutableStateOf(false) }
        var resetTrigger by remember { mutableStateOf(0) }

        // Handle timer
        LaunchedEffect(isTimerRunning, resetTrigger) {
            if (isTimerRunning) {
                val startTime = System.currentTimeMillis() - timer
                while (isTimerRunning) {
                    kotlinx.coroutines.delay(100L)
                    timer = System.currentTimeMillis() - startTime
                }
            } else {
                timer = 0L
            }
        }

        // Handle image selection
        LaunchedEffect(selectedImageUri) {
            if (selectedImageUri != null) {
                isTimerRunning = false
                timer = 0L
                resetTrigger++

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

                    kotlinx.coroutines.delay(300L)
                    isTimerRunning = true
                }
            }
        }

        //Delay for UI
        LaunchedEffect(resetTrigger) {
            if (isPuzzleSolved) {
                kotlinx.coroutines.delay(300L)
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
                        Greeting(name = "Welcome to ElisaGame!")

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Timer: ${formatTime(timer)}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

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

                                            // Restore the missing piece
                                            val restoredIndex = ImageSplitter.restoreMissingPiece(tiles)
                                            emptyIndex = restoredIndex

                                            // Trigger UI recomposition
                                            resetTrigger++

                                            isTimerRunning = false

                                            Toast.makeText(
                                                this@MainActivity,
                                                "Puzzle Solved in ${formatTime(timer)}!",
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







    // Helper function to format the time
    fun formatTime(milliseconds: Long): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        val millis = milliseconds % 1000
        return String.format("%02d:%02d.%03d", minutes, seconds, millis)
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


