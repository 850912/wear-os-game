package com.example.wearboardgames

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameCatalogTest {
    @Test
    fun catalogHasUniqueModesAndValidCategories() {
        assertEquals("curated catalog", 29, games.size)
        assertEquals(games.size, games.map { it.mode }.toSet().size)
        assertEquals(games.size, games.map { it.title }.toSet().size)
        val categoryNames = categories.map { it.first }.toSet()
        assertTrue(categoryNames.isNotEmpty())
        games.forEach { game -> assertTrue("unknown category for ${game.title}", game.category in categoryNames) }
        categoryNames.forEach { category -> assertTrue("empty category: $category", games.any { it.category == category }) }
    }

    @Test
    fun everyCatalogModeHasExactlyOneRuntimeRoute() {
        games.forEach { game ->
            val matches = listOf(
                TetrisView.supportsMode(game.mode),
                SnakeView.supportsMode(game.mode),
                FlappyView.supportsMode(game.mode),
                RunnerView.supportsMode(game.mode),
                PongView.supportsMode(game.mode),
                BreakoutView.supportsMode(game.mode),
                BounceView.supportsMode(game.mode),
                PinballView.supportsMode(game.mode),
                LaneDodgeView.supportsMode(game.mode),
                StackView.supportsMode(game.mode),
                SimonView.supportsMode(game.mode),
                Game2048View.supportsMode(game.mode),
                XiangqiView.supportsMode(game.mode),
                ChessView.supportsMode(game.mode),
                GomokuView.supportsMode(game.mode),
                Connect4View.supportsMode(game.mode),
                ReversiView.supportsMode(game.mode),
                SudokuView.supportsMode(game.mode),
                MinesView.supportsMode(game.mode),
                MazeView.supportsMode(game.mode),
                SokobanView.supportsMode(game.mode),
                ClassicMiniGameView.supportsMode(game.mode),
            ).count { it }
            assertEquals("route count for ${game.title} (${game.mode})", 1, matches)
        }
    }
}
