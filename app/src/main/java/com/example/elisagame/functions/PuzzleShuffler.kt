package com.example.elisagame.functions

import android.graphics.Bitmap


object PuzzleShuffler {

    private const val GRID_SIZE = 3 // Fixed grid size (3x3)

    fun shufflePuzzle(pieces: MutableList<Bitmap?>) {
        do {

            pieces.shuffle()
        } while (!isSolvable(pieces))
    }

    fun isSolvable(shuffledPieces: List<Bitmap?>): Boolean {
        val inversions = countInversions(shuffledPieces)

        // For a 3x3 grid, the number of inversions must be even to be solvable
        return inversions % 2 == 0
    }

    private fun countInversions(pieces: List<Bitmap?>): Int {
        val tileNumbers = pieces.filterNotNull().map { it.hashCode() } // Use hashCode as unique identifiers
        var inversions = 0

        for (i in tileNumbers.indices) {
            for (j in i + 1 until tileNumbers.size) {
                if (tileNumbers[i] > tileNumbers[j]) {
                    inversions++
                }
            }
        }

        return inversions
    }
}
