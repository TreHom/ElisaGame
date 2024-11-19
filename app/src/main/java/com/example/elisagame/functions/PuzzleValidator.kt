package com.example.elisagame.functions

import android.graphics.Bitmap

object PuzzleValidator {

    fun isSolved(currentPieces: List<Bitmap?>, originalPieces: List<Bitmap?>): Boolean {
        for (i in currentPieces.indices) {
            if (currentPieces[i] != originalPieces[i]) {
                return false
            }
        }
        return true
    }
}
