package com.example.elisagame.functions

import android.graphics.Bitmap
import android.util.Log
import kotlin.random.Random

object ImageSplitter {

    private const val GRID_SIZE = 3 // Fixed grid size (3x3)
    private var missingPieceIndex: Int = -1 // To track the removed piece's index
    private var missingPiece: Bitmap? = null // Store the removed piece separately

    /**
     * Splits the given bitmap into a grid of pieces and removes one random piece.
     *
     * @param bitmap The image to be split into pieces.
     * @return A list of bitmap pieces with one piece replaced by null.
     */
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
            missingPieceIndex = Random.nextInt(pieces.size) // Track the index of the removed piece
            missingPiece = pieces[missingPieceIndex] // Store the missing piece
            pieces[missingPieceIndex] = null
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ImageSplitter", "Error splitting image: ${e.message}")
        }

        return pieces
    }

    /**
     * Restores the missing piece in the puzzle by replacing the null entry with the stored missing piece.
     *
     * @param pieces The current list of puzzle pieces.
     * @return The index of the restored piece or -1 if restoration failed.
     */
    fun restoreMissingPiece(pieces: MutableList<Bitmap?>): Int {
        // Validate the missingPieceIndex
        if (missingPieceIndex !in pieces.indices) {
            Log.e("ImageSplitter", "Invalid missingPieceIndex: $missingPieceIndex")
            return -1
        }

        // Ensure the missing piece is available
        if (missingPiece == null) {
            Log.e("ImageSplitter", "Missing piece is null.")
            return -1
        }

        // Restore the missing piece
        pieces[missingPieceIndex] = missingPiece
        Log.d("ImageSplitter", "Missing piece restored at index: $missingPieceIndex")
        return missingPieceIndex
    }

    /**
     * Cleans up memory by recycling all bitmaps in the given list and the missing piece.
     *
     * @param pieces The list of bitmap pieces to clean up.
     */
    fun cleanupBitmaps(pieces: List<Bitmap?>) {
        pieces.forEach { it?.recycle() }
        missingPiece?.recycle()
        missingPiece = null
    }
}
