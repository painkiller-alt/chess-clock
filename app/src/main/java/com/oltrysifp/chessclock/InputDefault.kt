package com.oltrysifp.chessclock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oltrysifp.chessclock.util.darkenColor
import com.oltrysifp.chessclock.util.lightenColor
import com.oltrysifp.chessclock.util.saturateColor

@Composable
fun InputDefault(
    label: String,
    textState: MutableState<String>,
    icon: ImageVector? = null,
    maxLength: Int = 60,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    onValueAfter: () -> Unit = {},
    onEnter: () -> Unit = {},
) {
    Column {
        var text by remember { mutableStateOf(textState.value) }

        val singleLine = maxLength <= 60

        val focusManager = LocalFocusManager.current
        val focusedColor: Color = if (!isSystemInDarkTheme()) {
            darkenColor(saturateColor(color, 3f), 0.06f)
        } else {
            lightenColor(saturateColor(color, 0.8f), 0.16f)
        }

        val onSubmit = {
            onEnter()
        }

        TextField(
            label = {
                Text(
                    text = label,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
            value = text,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = color,
                focusedContainerColor = focusedColor,
                disabledLabelColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.onSurface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = KeyboardActions(
                onDone = {
                    onSubmit()
                    focusManager.clearFocus()
                },
            ),
            onValueChange = {
                if (it.length <= maxLength) text = it
                if (text == "") {
                    onSubmit()
                }
                textState.value = text
                onValueAfter()
            },
            shape = RoundedCornerShape(8.dp),
            singleLine = singleLine,
            trailingIcon = {
                if (icon != null) {
                    IconButton(onClick = {
                        onSubmit()
                        focusManager.clearFocus()
                    }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun InputCard(
    label: String,
    text: String,
    icon: ImageVector? = null,
    maxLength: Int = 60,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    onClick: () -> Unit
) {
    Column {
        val singleLine = maxLength <= 60

        TextField(
            label = {
                Text(
                    text = label,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },
            value = text,
            enabled = false,
            colors = TextFieldDefaults.colors(
                disabledLabelColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = color,
                disabledIndicatorColor = Color.Transparent,
                disabledTextColor = MaterialTheme.colorScheme.onBackground
            ),
            onValueChange = { },
            shape = RoundedCornerShape(8.dp),
            singleLine = singleLine,
            trailingIcon = {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }
}