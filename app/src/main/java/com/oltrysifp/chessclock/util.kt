package com.oltrysifp.chessclock

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun log(string: Any) {
    Log.d("MyLog", string.toString())
}

@Composable
fun EdgeToEdgeConfig(
    activity: ComponentActivity,
    statusBarColor: Color = Color.Transparent,
    bottomBarColor: Color = Color.Transparent,
) {
    val isDarkMode = isSystemInDarkTheme()

    val statusBar = statusBarColor.hashCode()
    val bottomBar = bottomBarColor.hashCode()

    DisposableEffect(isDarkMode) {
        activity.enableEdgeToEdge(
            statusBarStyle = if (!isDarkMode) {
                SystemBarStyle.light(
                    statusBar,
                    statusBar
                )
            } else {
                SystemBarStyle.dark(
                    statusBar
                )
            },

            navigationBarStyle = if (!isDarkMode) {
                SystemBarStyle.light(
                    bottomBar,
                    bottomBar
                )
            } else {
                SystemBarStyle.dark(
                    bottomBar
                )
            }
        )

        onDispose { }
    }
}

fun manipulateColor(color: Color, factor: Float): Color {
    val a: Float = color.alpha
    val r = color.red * factor
    val g = color.green * factor
    val b = color.blue * factor
    return Color(r,g,b,a)
}

fun darkenColor(color: Color, percent: Float): Color {
    return manipulateColor(color, 1.0f-percent)
}

fun lightenColor(color: Color, percent: Float): Color {
    return manipulateColor(color, 1.0f+percent)
}

fun saturateColor(color: Color, contrast: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    hsv[1] *= contrast
    val result = Color(android.graphics.Color.HSVToColor(hsv))

    return result
}