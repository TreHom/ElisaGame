package com.example.elisagame.functions

import android.graphics.Bitmap
import android.util.Log
import kotlin.random.Random

object PuzzleShuffler {

    private const val GRID_SIZE = 3 // Fixed grid size (3x3)

    fun shufflePuzzle(pieces: MutableList<Bitmap?>) {
        // Ensure the empty piece (null) is in the last position initially
        if (pieces.indexOf(null) != pieces.size - 1) {
            pieces.remove(null)
            pieces.add(null)
        }

        Log.d("PuzzleShuffler", "Shuffling pieces...")
        randomizeBySliding(pieces)
        Log.d("PuzzleShuffler", "Shuffled pieces: ${pieces.map { it?.hashCode() ?: "null" }}")
    }

    private fun randomizeBySliding(pieces: MutableList<Bitmap?>) {
        // Convert pieces into a 2D mutable grid
        val grid = pieces.chunked(GRID_SIZE).map { it.toMutableList() }.toMutableList()
        var emptyPos = findEmptyPosition(grid)
        val directions = listOf(Pair(0, 1), Pair(1, 0), Pair(0, -1), Pair(-1, 0)) // Right, Down, Left, Up

        repeat(2) { // Perform 100 random valid moves
            val validMoves = directions.map { Pair(emptyPos.first + it.first, emptyPos.second + it.second) }
                .filter { it.first in 0 until GRID_SIZE && it.second in 0 until GRID_SIZE }

            val nextMove = validMoves[Random.nextInt(validMoves.size)]
            swap(grid, emptyPos, nextMove)
            emptyPos = nextMove
        }

        // Flatten the grid back to a list
        val shuffledList = grid.flatten()
        pieces.clear()
        pieces.addAll(shuffledList)
    }

    private fun findEmptyPosition(grid: List<List<Bitmap?>>): Pair<Int, Int> {
        for (i in grid.indices) {
            for (j in grid[i].indices) {
                if (grid[i][j] == null) return Pair(i, j)
            }
        }
        throw IllegalStateException("No empty space found in the grid")
    }

    private fun swap(grid: MutableList<MutableList<Bitmap?>>, pos1: Pair<Int, Int>, pos2: Pair<Int, Int>) {
        val temp = grid[pos1.first][pos1.second]
        grid[pos1.first][pos1.second] = grid[pos2.first][pos2.second]
        grid[pos2.first][pos2.second] = temp
    }
}
