package com.oltrysifp.chessclock.composable

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun Dialogue(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    confirmText: String,
    dismissText: String,
    dialogTitle: String,
    dialogContent: @Composable () -> Unit,
    icon: ImageVector? = null,
) {
    AlertDialog(
        icon = if (icon != null) {
            {
                Icon(
                    icon,
                    contentDescription = icon.name
                )
            }
        } else null,
        title = {
            Text(text = dialogTitle)
        },
        text = { dialogContent() },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(dismissText)
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    )
}