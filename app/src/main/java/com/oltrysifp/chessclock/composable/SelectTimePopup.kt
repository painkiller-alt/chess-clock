package com.oltrysifp.chessclock.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltrysifp.chessclock.TwoDigitsTimeField
import kotlin.math.floor

@Composable
fun SelectTimePopup(
    isActive: MutableState<Boolean>,
    selectedTime: Long,
    setSelectedTime: (t: Long) -> Unit
) {
    val hours = remember { mutableStateOf("") }
    val minutes = remember { mutableStateOf("") }
    val seconds = remember { mutableStateOf("") }

    val textChanged = remember { mutableStateOf(false) }

    LaunchedEffect(
        isActive.value
    ) {
        val minutesLeft = floor(selectedTime.toDouble() / 60)
        val hoursLeft = floor((minutesLeft) / 60)

        val secondsLeftInMinute = floor(selectedTime - minutesLeft * 60)
        val minutesLeftInHour = floor(minutesLeft - hoursLeft*60)

        val hoursInt = hoursLeft.toInt()
        hours.value = if (hoursInt == 0) {"00"} else hoursInt.toString()
        val minutesInt = minutesLeftInHour.toInt()
        minutes.value = if (minutesInt == 0) {"00"} else minutesInt.toString()
        val secondsInt = secondsLeftInMinute.toInt()
        seconds.value = if (secondsInt == 0) {"00"} else secondsInt.toString()
    }

    if (isActive.value) {
        Dialogue(
            onConfirmation = {
                isActive.value = false
                val hoursGot = hours.value.ifEmpty { "0" }.toInt()
                val minutesGot = minutes.value.ifEmpty { "0" }.toInt()
                val secondsGot = seconds.value.ifEmpty { "0" }.toInt()

                val totalMinutes = hoursGot*60 + minutesGot
                val totalSeconds = totalMinutes*60 + secondsGot

                setSelectedTime(totalSeconds.toLong())
                textChanged.value = true
            },
            onDismissRequest = {
                isActive.value = false
            },
            confirmText = "Сохранить",
            dismissText = "Назад",
            dialogTitle = "Настроить время",
            dialogContent = {
                Row {
                    TwoDigitsTimeField(hours, textChanged) {}
                    Text(":", fontSize = 50.sp, modifier = Modifier.offset(y=(-6).dp))
                    TwoDigitsTimeField(minutes, textChanged) {}
                    Text(":", fontSize = 50.sp, modifier = Modifier.offset(y=(-6).dp))
                    TwoDigitsTimeField(seconds, textChanged) {}
                }
            },
        )
    }
}