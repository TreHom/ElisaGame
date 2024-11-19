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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.elisagame.functions.ImageSplitter
import com.example.elisagame.functions.MovementLogic
import com.example.elisagame.functions.PuzzleShuffler
import com.example.elisagame.functions.PuzzleValidator
import com.example.elisagame.ui.theme.ElisaGameTheme
import java.io.InputStream

class MainActivity : ComponentActivity() {

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionsHandler: PermissionsHandler
    private var selectedImageUri by mutableStateOf<Uri?>(null)

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
        var tiles by remember { mutableStateOf<List<Bitmap?>>(emptyList()) }
        var originalTiles by remember { mutableStateOf<List<Bitmap?>>(emptyList()) }
        var emptyIndex by remember { mutableStateOf(-1) }
        var isPuzzleSolved by remember { mutableStateOf(false) }

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

                        // Handle Image Selection
                        selectedImageUri?.let { uri ->
                            val bitmap = getBitmapFromUri(uri)
                            bitmap?.let {
                                tiles = ImageSplitter.splitImage(it) // Updated call
                                originalTiles = tiles.toList()
                                PuzzleShuffler.shufflePuzzle(tiles.toMutableList())
                                emptyIndex = tiles.indexOf(null)
                                isPuzzleSolved = false
                            }
                        }

                        // Render the Puzzle Grid UI
                        if (tiles.isNotEmpty()) {
                            PuzzleGridUI(
                                currentPieces = tiles,
                                emptyIndex = emptyIndex,
                                onTileClick = { clickedIndex ->
                                    if (MovementLogic.canMove(clickedIndex, emptyIndex, 3, 3)) {
                                        tiles = tiles.toMutableList().apply {
                                            this[emptyIndex] = this[clickedIndex]
                                            this[clickedIndex] = null
                                        }
                                        emptyIndex = clickedIndex

                                        // Check if the puzzle is solved
                                        if (PuzzleValidator.isSolved(tiles, originalTiles)) {
                                            isPuzzleSolved = true
                                        }
                                    }
                                },
                                isPuzzleSolved = isPuzzleSolved,
                                onPuzzleSolved = {
                                    Toast.makeText(this@MainActivity, "Puzzle Solved!", Toast.LENGTH_LONG).show()
                                }
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
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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
