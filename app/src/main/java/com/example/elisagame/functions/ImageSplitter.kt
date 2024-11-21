package com.example.elisagame.functions

import android.graphics.Bitmap
import android.util.Log
import kotlin.random.Random

object ImageSplitter {

    private const val GRID_SIZE = 3 // Fixed grid size (3x3)
    private var missingPieceIndex: Int = -1 // To track the removed piece's index

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
            pieces[missingPieceIndex] = null
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ImageSplitter", "Error splitting image: ${e.message}")
        }

        return pieces
    }

    /**
     * Restores the missing piece in the puzzle by replacing the null entry with the original tile.
     *
     * @param pieces The current list of puzzle pieces.
     * @param originalTiles The original list of puzzle pieces for reference.
     * @return The index of the restored piece or -1 if restoration failed.
     */
    fun restoreMissingPiece(pieces: MutableList<Bitmap?>, originalTiles: List<Bitmap?>): Int {
        // Ensure the lists are valid
        if (pieces.size != originalTiles.size) {
            Log.e("ImageSplitter", "Mismatched sizes: pieces (${pieces.size}), originalTiles (${originalTiles.size})")
            return -1 // Return failure code
        }

        // Validate the missingPieceIndex
        if (missingPieceIndex !in pieces.indices) {
            Log.e("ImageSplitter", "Invalid missingPieceIndex: $missingPieceIndex")
            return -1
        }

        // Ensure the original tile is not null
        if (originalTiles[missingPieceIndex] == null) {
            Log.e("ImageSplitter", "Original tile at missingPieceIndex is null.")
            return -1
        }

        // Restore the missing piece
        pieces[missingPieceIndex] = originalTiles[missingPieceIndex]
        Log.d("ImageSplitter", "Missing piece restored at index: $missingPieceIndex")
        return missingPieceIndex
    }


    /**
     * Cleans up memory by recycling all bitmaps in the given list.
     *
     * @param pieces The list of bitmap pieces to clean up.
     */
    fun cleanupBitmaps(pieces: List<Bitmap?>) {
        pieces.forEach { it?.recycle() }
    }
}
