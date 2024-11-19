package com.example.elisagame.functions

import android.graphics.Bitmap
import android.util.Log
import kotlin.random.Random

object ImageSplitter {

    private const val GRID_SIZE = 3 // Fixed grid size (3x3)

    fun splitImage(bitmap: Bitmap): List<Bitmap?> {
        val pieceWidth = bitmap.width / GRID_SIZE
        val pieceHeight = bitmap.height / GRID_SIZE
        val pieces = mutableListOf<Bitmap?>()

        try {
            // Split the image into a grid
            for (row in 0 until GRID_SIZE) {
                for (col in 0 until GRID_SIZE) {
                    val piece = Bitmap.createBitmap(bitmap, col * pieceWidth, row * pieceHeight, pieceWidth, pieceHeight)
                    pieces.add(piece)
                }
            }

            // Remove one random piece
            val emptyIndex = Random.nextInt(pieces.size)
            pieces[emptyIndex] = null
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ImageSplitter", "Error splitting image: ${e.message}")
        }

        return pieces
    }

    fun cleanupBitmaps(pieces: List<Bitmap?>) {
        pieces.forEach { it?.recycle() }
    }
}
