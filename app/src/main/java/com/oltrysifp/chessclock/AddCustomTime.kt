package com.oltrysifp.chessclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltrysifp.chessclock.models.TimeControl
import com.oltrysifp.chessclock.ui.theme.ChessTimerTheme

class AddCustomTime : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val name = remember { mutableStateOf("") }
            var split by remember { mutableStateOf(false) }

            val firstPlayerTimeSeconds = remember { mutableStateOf("") }
            val firstPlayerTimeMinutes = remember { mutableStateOf("") }
            val firstPlayerAddSeconds = remember { mutableStateOf("") }
            val firstPlayerAddMinutes = remember { mutableStateOf("") }

            val secondPlayerTimeSeconds = remember { mutableStateOf("") }
            val secondPlayerTimeMinutes = remember { mutableStateOf("") }
            val secondPlayerAddSeconds = remember { mutableStateOf("") }
            val secondPlayerAddMinutes = remember { mutableStateOf("") }

            val onChange = {
                if (!split) {
                    secondPlayerTimeSeconds.value = firstPlayerTimeSeconds.value
                    secondPlayerTimeMinutes.value = firstPlayerTimeMinutes.value
                    secondPlayerAddSeconds.value = firstPlayerAddSeconds.value
                    secondPlayerAddMinutes.value = firstPlayerAddMinutes.value
                }
            }

            LaunchedEffect(key1=firstPlayerTimeSeconds.value) { onChange() }
            LaunchedEffect(key1=firstPlayerTimeMinutes.value) { onChange() }
            LaunchedEffect(key1=firstPlayerAddSeconds.value) { onChange() }
            LaunchedEffect(key1=firstPlayerAddMinutes.value) { onChange() }

            LaunchedEffect(key1=secondPlayerTimeSeconds.value) { onChange() }
            LaunchedEffect(key1=secondPlayerTimeMinutes.value) { onChange() }
            LaunchedEffect(key1=secondPlayerAddSeconds.value) { onChange() }
            LaunchedEffect(key1=secondPlayerAddMinutes.value) { onChange() }

            ChessTimerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier
                            .padding(innerPadding)
                            .padding(10.dp)
                    ) {
                        InputDefault(
                            "Название",
                            name,
                            color = MaterialTheme.colorScheme.surface,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )

                        Spacer(Modifier.padding(16.dp))

                        Column(
                            Modifier
                                .padding(10.dp)
                        ) {
                            Text("1 Игрок")

                            Spacer(Modifier.padding(4.dp))

                            Row(
                                Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Время",
                                    fontSize = 22.sp
                                )

                                Row() {
                                    TwoDigitsTimeField(firstPlayerTimeMinutes) {}
                                    Text(":", fontSize = 26.sp)
                                    TwoDigitsTimeField(firstPlayerTimeSeconds) {}
                                }
                            }

                            Spacer(Modifier.padding(10.dp))

                            Row(
                                Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Добавление",
                                    fontSize = 22.sp
                                )

                                Row() {
                                    TwoDigitsTimeField(firstPlayerAddMinutes) {}
                                    Text(":", fontSize = 26.sp)
                                    TwoDigitsTimeField(firstPlayerAddSeconds) {}
                                }
                            }
                        }

                        AnimatedVisibility(split) {
                            Column(
                                Modifier
                                    .padding(10.dp)
                            ) {
                                Text("2 Игрок")

                                Spacer(Modifier.padding(2.dp))

                                Row(
                                    Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Время",
                                        fontSize = 22.sp
                                    )

                                    Row() {
                                        TwoDigitsTimeField(secondPlayerTimeMinutes) {}
                                        Text(":", fontSize = 26.sp)
                                        TwoDigitsTimeField(secondPlayerTimeSeconds) {}
                                    }
                                }

                                Spacer(Modifier.padding(8.dp))

                                Row(
                                    Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Добавление",
                                        fontSize = 22.sp
                                    )

                                    Row() {
                                        TwoDigitsTimeField(secondPlayerAddMinutes) {}
                                        Text(":", fontSize = 26.sp)
                                        TwoDigitsTimeField(secondPlayerAddSeconds) {}
                                    }
                                }
                            }
                        }

                        Row(
                            Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Разное время"
                            )

                            Switch(
                                split,
                                {
                                    split = it
                                    onChange()
                                }
                            )
                        }

                        Spacer(Modifier.padding(10.dp))

                        Button(
                            shape = RoundedCornerShape(6.dp),
                            onClick = {
                                firstPlayerTimeSeconds.value = firstPlayerTimeSeconds.value.ifEmpty { "0" }
                                firstPlayerTimeMinutes.value = firstPlayerTimeMinutes.value.ifEmpty { "0" }
                                firstPlayerAddSeconds.value = firstPlayerAddSeconds.value.ifEmpty { "0" }
                                firstPlayerAddMinutes.value = firstPlayerAddMinutes.value.ifEmpty { "0" }

                                secondPlayerTimeSeconds.value = secondPlayerTimeSeconds.value.ifEmpty { "0" }
                                secondPlayerTimeMinutes.value = secondPlayerTimeMinutes.value.ifEmpty { "0" }
                                secondPlayerAddSeconds.value = secondPlayerAddSeconds.value.ifEmpty { "0" }
                                secondPlayerAddMinutes.value = secondPlayerAddMinutes.value.ifEmpty { "0" }

                                val firstPlayerTime = firstPlayerTimeSeconds.value.toInt() +
                                        firstPlayerTimeMinutes.value.toInt() * 60
                                val firstPlayerAdd = firstPlayerAddSeconds.value.toInt() +
                                        firstPlayerAddMinutes.value.toInt() * 60

                                val secondPlayerTime = secondPlayerTimeSeconds.value.toInt() +
                                        secondPlayerTimeMinutes.value.toInt() * 60
                                val secondPlayerAdd = secondPlayerAddSeconds.value.toInt() +
                                        secondPlayerAddMinutes.value.toInt() * 60

                                val timeControl = TimeControl(
                                    firstStart = firstPlayerTime,
                                    firstAdd = firstPlayerAdd,
                                    secondStart = secondPlayerTime,
                                    secondAdd = secondPlayerAdd,
                                    name = name.value
                                )

                                log(firstPlayerTime)
                                log(firstPlayerAdd)
                                log(secondPlayerTime)
                                log(secondPlayerAdd)
                                log(name.value)

                                finish()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Добавить"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TwoDigitsTimeField(
    textState: MutableState<String>,
    maxNum: Int = 60,
    onEnter: () -> Unit
) {
    var text by remember { mutableStateOf(textState.value) }

    val focusManager = LocalFocusManager.current
    val onSubmit = {
        onEnter()
    }


    BasicTextField(
        text,
        {
            if (it.isEmpty()) {
                text = it
            } else if (it.toInt() <= maxNum) {
                text = it
            } else {
                text = maxNum.toString()
            }

            textState.value = text
        },
        modifier = Modifier
            .width(34.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        keyboardActions = KeyboardActions(
            onDone = {
                onSubmit()
                focusManager.clearFocus()
            },
        ),
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        ),
        decorationBox = {
            Card(
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(4.dp)
                ) {
                    it()
                }
            }
        }
    )
}