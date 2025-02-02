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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltrysifp.chessclock.models.TimeControl
import com.oltrysifp.chessclock.ui.theme.ChessTimerTheme


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

    Column(
        Modifier.fillMaxSize()
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
                val context = LocalContext.current
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
        CustomTimes(selectedTime, listOf(), context)
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
            TimeControl(60, 0, 60, 0, "1 мин. | +0 сек."),
            TimeControl(60, 1, 60, 1, "1 мин. | +1 сек."),
            TimeControl(120, 0, 120, 0, "2 мин. | +0 сек."),
            TimeControl(120, 1, 120, 1, "2 мин. | +1 сек.")
        ),
        selectedTime
    )
}

@Composable
fun BlitzTimes(selectedTime: MutableState<TimeControl?>) {
    TimesChoiceCard(
        "Блиц",
        listOf(
            TimeControl(180, 0, 180, 0, "3 мин. | +0 сек."),
            TimeControl(180, 2, 180, 2, "3 мин. | +2 сек."),
            TimeControl(300, 0, 300, 0, "5 мин. | +0 сек."),
            TimeControl(300, 2, 300, 2, "5 мин. | +2 сек."),
            TimeControl(300, 5, 300, 5, "5 мин. | +5 сек."),

        ),
        selectedTime
    )
}

@Composable
fun RapidTimes(selectedTime: MutableState<TimeControl?>) {
    TimesChoiceCard(
        "Рапид",
        listOf(
            TimeControl(600, 0, 600, 0, "10 мин. | +0 сек."),
            TimeControl(600, 5, 600, 5, "10 мин. | +5 сек."),
            TimeControl(900, 10, 900, 10, "15 мин. | +10 сек."),
            TimeControl(1200, 0, 1200, 0, "20 мин. | +0 сек."),
            TimeControl(1800, 0, 1800, 0, "30 мин. | +0 сек."),
            TimeControl(3600, 0, 3600, 0, "60 мин. | +0 сек."),
        ),
        selectedTime
    )
}

@Composable
fun CustomTimes(selectedTime: MutableState<TimeControl?>, customTimes: List<TimeControl>, context: Context) {
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
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    Modifier
                        .padding(10.dp)
                ) {
                    for (time in customTimes) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
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

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(context, AddCustomTime::class.java)
                                context.startActivity(intent)

                                expanded = false
                            },
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Добавить новое",
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

fun startTimer(context: Context, selectedTime: TimeControl) {
    val b = Bundle()
    b.putInt("firstStart", selectedTime.firstStart)
    b.putInt("firstAdd", selectedTime.firstAdd)
    b.putInt("secondStart", selectedTime.secondStart)
    b.putInt("secondAdd", selectedTime.secondAdd)
    b.putString("name", selectedTime.name)

    val intent = Intent(context, Timer::class.java)
    intent.putExtras(b)

    context.startActivity(intent)
}