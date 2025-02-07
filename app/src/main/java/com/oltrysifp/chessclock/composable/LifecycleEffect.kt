package com.oltrysifp.chessclock.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

@Composable
fun LifecycleEffect(
    onPause: () -> Unit = {}
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    // Observe lifecycle events
    DisposableEffect(Unit) {
        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                onPause()
            }
        }

        // Add the observer to the lifecycle
        lifecycle.addObserver(lifecycleObserver)

        // Clean up when the composable is disposed
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
}