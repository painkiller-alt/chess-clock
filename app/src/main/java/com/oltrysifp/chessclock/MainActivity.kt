package com.oltrysifp.chessclock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltrysifp.chessclock.models.TimeControl
import com.oltrysifp.chessclock.models.UserData
import com.oltrysifp.chessclock.ui.theme.ChessTimerTheme
import com.oltrysifp.chessclock.util.Constants
import com.oltrysifp.chessclock.util.DataStoreManager
import com.oltrysifp.chessclock.util.EdgeToEdgeConfig
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EdgeToEdgeConfig(this)

            ChessTimerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        MenuContent()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun MenuContent() {
    val selectedTime: MutableState<TimeControl?> = remember { mutableStateOf(null) }
    val context = LocalContext.current
    val userData = remember { mutableStateOf(Constants.userDataDefault()) }
    val userDataLoaded = remember { mutableStateOf(false) }

    val dataStoreManager = DataStoreManager(context)
    LoadUserData(dataStoreManager, userData.value, userDataLoaded)

    val nestedScrollConnection = rememberNestedScrollInteropConnection()
    Column(
        Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text="Шахматные часы",
                fontSize = 30.sp
            )
        }

        Row(
            Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val time = selectedTime.value
            Text(
                text = time?.name ?: "Время не выбрано",
                fontSize = 16.sp
            )

            AnimatedContent(
                targetState = selectedTime.value,
                transitionSpec = {
                    if ( initialState == null ) {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                    } else {
                        fadeIn(tween(1)) togetherWith fadeOut(tween(1))
                    }
                },
                label = "fade"
            ) { selectedTime ->
                Button(
                    shape = CircleShape,
                    onClick = {
                        val selectedTimeFinal = selectedTime
                        if (selectedTimeFinal != null) {
                            startTimer(context, selectedTimeFinal)
                        }
                    },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(40.dp),
                    enabled = (selectedTime != null)
                ) {
                    Icon(Icons.Filled.PlayArrow, "Старт")
                }
            }
        }

        BulletTimes(selectedTime)
        Spacer(Modifier.padding(4.dp))
        BlitzTimes(selectedTime)
        Spacer(Modifier.padding(4.dp))
        RapidTimes(selectedTime)
        Spacer(Modifier.padding(4.dp))
        CustomTimes(selectedTime, userData.value, context, dataStoreManager)
    }
}

@Composable
fun TimesChoiceCard(
    label: String,
    times: List<TimeControl>,
    selectedTime: MutableState<TimeControl?>
) {
    var expanded by remember { mutableStateOf(false) }
    val markerRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "markerRotation")

    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                expanded = !expanded
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 24.sp)

            Icon(
                Icons.Filled.KeyboardArrowDown,
                "Маркер списка",
                modifier = Modifier
                    .rotate(markerRotation)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    Modifier
                        .padding(10.dp)
                ) {
                    for (time in times) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedTime.value = time
                                    expanded = false
                                },
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                time.name,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .padding(6.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BulletTimes(selectedTime: MutableState<TimeControl?>) {
    TimesChoiceCard(
        "Пуля",
        listOf(
            TimeControl(60, 60, 0, 0, "1 мин. | +0 сек."),
            TimeControl(60, 60, 1, 1, "1 мин. | +1 сек."),
            TimeControl(120, 120, 0, 0, "2 мин. | +0 сек."),
            TimeControl(120, 120, 1, 1, "2 мин. | +1 сек.")
        ),
        selectedTime
    )
}

@Composable
fun BlitzTimes(selectedTime: MutableState<TimeControl?>) {
    TimesChoiceCard(
        "Блиц",
        listOf(
            TimeControl(180, 180, 0, 0, "3 мин. | +0 сек."),
            TimeControl(180, 180, 2, 2, "3 мин. | +2 сек."),
            TimeControl(300, 300, 0, 0, "5 мин. | +0 сек."),
            TimeControl(300, 300, 2, 2, "5 мин. | +2 сек."),
            TimeControl(300, 300, 5, 5, "5 мин. | +5 сек."),

        ),
        selectedTime
    )
}

@Composable
fun RapidTimes(selectedTime: MutableState<TimeControl?>) {
    TimesChoiceCard(
        "Рапид",
        listOf(
            TimeControl(600, 600, 0, 0, "10 мин. | +0 сек."),
            TimeControl(600, 600, 5, 5, "10 мин. | +5 сек."),
            TimeControl(900, 900, 10, 10, "15 мин. | +10 сек."),
            TimeControl(1200, 1200, 0, 0, "20 мин. | +0 сек."),
            TimeControl(1800, 1800, 0, 0, "30 мин. | +0 сек."),
            TimeControl(3600, 3600, 0, 0, "60 мин. | +0 сек."),
        ),
        selectedTime
    )
}

@Composable
fun CustomTimes(
    selectedTime: MutableState<TimeControl?>,
    userData: UserData,
    context: Context,
    dataStoreManager: DataStoreManager
) {
    var expanded by remember { mutableStateOf(false) }
    val markerRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "markerRotation")
    val customTimeControls = remember { mutableStateListOf<TimeControl>() }
    customTimeControls.clear()
    customTimeControls.addAll(userData.customTimeControls)

    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                expanded = !expanded
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Пользовательские", fontSize = 24.sp)

            Icon(
                Icons.Filled.KeyboardArrowDown,
                "Маркер списка",
                modifier = Modifier
                    .rotate(markerRotation)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column (
                    Modifier
                        .padding(10.dp)
                ) {
                    customTimeControls.forEach { timeControl ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedTime.value = timeControl
                                    expanded = false
                                },
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    timeControl.name,
                                    fontSize = 20.sp,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .fillMaxWidth(0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                val userDataSaveCoroutine = rememberCoroutineScope()
                                Button(
                                    modifier = Modifier.size(36.dp),
                                    onClick = {
                                        userData.customTimeControls.removeIf { it.name == timeControl.name }
                                        customTimeControls.removeIf { it.name == timeControl.name }

                                        userDataSaveCoroutine.launch {
                                            dataStoreManager.saveUserData(userData)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        "Удалить",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val b = Bundle()
                                b.putString("userData", Json.encodeToString(userData))
                                val intent = Intent(context, AddCustomTime::class.java)
                                intent.putExtras(b)
                                context.startActivity(intent)

                                expanded = false
                            },
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Добавить новое",
                            fontSize = 20.sp,
                            modifier = Modifier
                                .padding(6.dp),)
                    }
                }
            }
        }
    }
}

fun startTimer(context: Context, selectedTime: TimeControl) {
    val b = Bundle()
    b.putString("selectedTime", Json.encodeToString(selectedTime))

    val intent = Intent(context, Timer::class.java)
    intent.putExtras(b)

    context.startActivity(intent)
}