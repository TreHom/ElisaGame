package com.example.elisagame.functions

object MovementLogic {

    fun canMove(clickedIndex: Int, emptyIndex: Int, rows: Int, cols: Int): Boolean {
        val clickedRow = clickedIndex / cols
        val clickedCol = clickedIndex % cols
        val emptyRow = emptyIndex / cols
        val emptyCol = emptyIndex % cols

        // Can move if adjacent horizontally or vertically
        return (clickedRow == emptyRow && kotlin.math.abs(clickedCol - emptyCol) == 1) ||
                (clickedCol == emptyCol && kotlin.math.abs(clickedRow - emptyRow) == 1)
    }
}
