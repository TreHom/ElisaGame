package com.example.elisagame

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp


@Composable
fun PuzzleGridUI(
    gridSize: Int = 3, // Grid is always 3x3
    currentPieces: List<Bitmap?>,
    emptyIndex: Int,
    onTileClick: (Int) -> Unit,
    isPuzzleSolved: Boolean,
    onPuzzleSolved: () -> Unit,
    imageWidth: Int,
    imageHeight: Int
) {
    // BoxWithConstraints allows dynamic layout based on available space
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight

        // Calculate the aspect ratio of the original image
        val aspectRatio = imageWidth.toFloat() / imageHeight.toFloat()

        // Calculate the size of the grid container
        val containerWidth: Dp
        val containerHeight: Dp
        if (maxWidth / maxHeight < aspectRatio) {
            containerWidth = maxWidth
            containerHeight = maxWidth / aspectRatio
        } else {
            containerWidth = maxHeight * aspectRatio
            containerHeight = maxHeight
        }

        // Calculate tile dimensions
        val tileWidth = containerWidth / gridSize
        val tileHeight = containerHeight / gridSize

        // Render the grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridSize),
            modifier = Modifier
                .width(containerWidth)
                .height(containerHeight)
                .background(Color.Black) // Optional background for the grid
        ) {
            items(currentPieces.size) { index ->
                val tile = currentPieces[index]
                Box(
                    modifier = Modifier
                        .size(tileWidth, tileHeight)
                        .background(if (tile == null) Color.Gray else Color.Transparent)
                        .clickable { onTileClick(index) },
                    contentAlignment = Alignment.Center
                ) {
                    tile?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun PuzzleGrid(
    currentPieces: List<Bitmap?>,
    emptyIndex: Int,
    onTileClick: (Int) -> Unit
) {
    val gridSize = sqrt(currentPieces.size.toDouble()).toInt()

    // Access screen configuration for dynamic sizing
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Calculate dynamic tile dimensions
    val tileWidth = (screenWidth / gridSize) * 0.9f // Leave some space for padding
    val tileHeight = tileWidth // For square tiles; adjust if rectangles are desired

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        for (row in 0 until gridSize) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (col in 0 until gridSize) {
                    val index = row * gridSize + col
                    val piece = currentPieces[index]

                    Box(
                        modifier = Modifier
                            .width(tileWidth)
                            .height(tileHeight)
                            .padding(2.dp)
                            .background(
                                color = if (piece == null) ComposeColor.Gray else ComposeColor.White,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                onTileClick(index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        piece?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Puzzle Piece",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
