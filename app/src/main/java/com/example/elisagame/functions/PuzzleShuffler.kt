package com.example.elisagame.functions

import android.graphics.Bitmap
import kotlin.math.abs
import kotlin.random.Random

object PuzzleShuffler {

    private const val GRID_SIZE = 3 // Fixed grid size (3x3)

    fun shufflePuzzle(pieces: MutableList<Bitmap?>) {
        do {
            pieces.shuffle()
        } while (!isSolvable(pieces))
    }

    private fun isSolvable(pieces: List<Bitmap?>): Boolean {
        val inversions = countInversions(pieces)
        val emptyRow = findEmptyRow(pieces)

        // Solvability rule for 3x3 grid:
        // Inversions should be even for the puzzle to be solvable
        return inversions % 2 == 0
    }

    private fun countInversions(pieces: List<Bitmap?>): Int {
        var inversions = 0

        for (i in pieces.indices) {
            val current = pieces[i] ?: continue // Skip the empty tile

            for (j in i + 1 until pieces.size) {
                val next = pieces[j] ?: continue // Skip the empty tile

                // Count as an inversion if the current tile is "greater" than the next
                if (i > j) {
                    inversions++
                }
            }
        }
        return inversions
    }

    private fun findEmptyRow(pieces: List<Bitmap?>): Int {
        val emptyIndex = pieces.indexOf(null) // Find the index of the empty slot
        return GRID_SIZE - (emptyIndex / GRID_SIZE) // Row index from the bottom (1-based)
    }
}
