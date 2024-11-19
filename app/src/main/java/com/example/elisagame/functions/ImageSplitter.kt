package com.example.elisagame.functions

import android.graphics.Bitmap
import kotlin.random.Random

object ImageSplitter {

    private const val GRID_SIZE = 3 // Fixed grid size (3x3)

    fun splitImage(bitmap: Bitmap): List<Bitmap?> {
        // Calculate the dimensions of each piece
        val pieceWidth = bitmap.width / GRID_SIZE
        val pieceHeight = bitmap.height / GRID_SIZE
        val pieces = mutableListOf<Bitmap?>()

        // Split the image into a 3x3 grid
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                val piece = Bitmap.createBitmap(bitmap, col * pieceWidth, row * pieceHeight, pieceWidth, pieceHeight)
                pieces.add(piece)
            }
        }

        // Remove one random piece
        val emptyIndex = Random.nextInt(pieces.size)
        pieces[emptyIndex] = null

        return pieces
    }
}
