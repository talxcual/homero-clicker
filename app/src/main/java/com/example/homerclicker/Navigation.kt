package com.example.homerclicker

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.homerclicker.ui.main.StartScreen
import com.example.homerclicker.ui.main.GameScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Start)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Start> {
          StartScreen(
            onStartGame = { backStack.add(Game) },
            modifier = Modifier.safeDrawingPadding()
          )
        }
        entry<Game> {
          GameScreen(
            onBackToStart = { backStack.removeLastOrNull() },
            modifier = Modifier.safeDrawingPadding()
          )
        }
      },
  )
}
