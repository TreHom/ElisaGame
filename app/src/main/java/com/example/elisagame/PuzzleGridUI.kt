package com.example.elisagame

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

@Composable
fun PuzzleGridUI(
    currentPieces: List<Bitmap?>,
    emptyIndex: Int,
    onTileClick: (Int) -> Unit,
    isPuzzleSolved: Boolean,
    onPuzzleSolved: () -> Unit
) {
    if (isPuzzleSolved) {
        onPuzzleSolved()
    } else {
        PuzzleGrid(
            currentPieces = currentPieces,
            emptyIndex = emptyIndex,
            onTileClick = onTileClick
        )
    }
}

@Composable
fun PuzzleGrid(
    currentPieces: List<Bitmap?>,
    emptyIndex: Int,
    onTileClick: (Int) -> Unit
) {
    val gridSize = 3 // Fixed grid size
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
                            .size(100.dp)
                            .padding(4.dp)
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

