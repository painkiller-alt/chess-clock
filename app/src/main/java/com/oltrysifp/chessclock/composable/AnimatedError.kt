package com.oltrysifp.chessclock.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AnimatedError(
    isVisible: Boolean,
    text: String
) {
    AnimatedVisibility(
        isVisible,
        enter = fadeIn() + expandVertically(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Text(
            text,
            color = Color.Red
        )
    }
}