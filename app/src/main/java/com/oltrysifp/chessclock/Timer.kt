package com.oltrysifp.chessclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltrysifp.chessclock.composable.CircularRevealAnimation
import com.oltrysifp.chessclock.composable.LifecycleEffect
import com.oltrysifp.chessclock.composable.SelectTimePopup
import com.oltrysifp.chessclock.models.TimeControl
import com.oltrysifp.chessclock.ui.theme.ChessTimerTheme
import com.oltrysifp.chessclock.util.Constants
import com.oltrysifp.chessclock.util.EdgeToEdgeConfig
import com.oltrysifp.chessclock.util.KeepScreenOn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.math.floor

class Timer : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedTimeString = intent.getStringExtra("selectedTime")

        val selectedTime = if (selectedTimeString != null) {
            Json.decodeFromString<TimeControl>(selectedTimeString)
        } else {
            return
        }

        setContent {
            val isPaused = remember { mutableStateOf(true) }

            LifecycleEffect(
                onPause = { isPaused.value = true }
            )

            EdgeToEdgeConfig(this)
            KeepScreenOn()

            ChessTimerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TimerContent(innerPadding, selectedTime, isPaused)
                }
            }
        }
    }
}

@Composable
fun TimerContent(innerPadding: PaddingValues, selectedTime: TimeControl, isPaused: MutableState<Boolean>) {
    val isDelayOn = selectedTime.mode == Constants.TimerMode.DELAY
    val firstChecked = remember { mutableStateOf(false) }
    val secondChecked = remember { mutableStateOf(false) }

    var firstMoveNumber by remember { mutableIntStateOf(0) }
    var secondMoveNumber by remember { mutableIntStateOf(0) }

    var firstTimeLeft by remember { mutableLongStateOf(selectedTime.firstStart.toLong() * 1000) }
    var secondTimeLeft by remember { mutableLongStateOf(selectedTime.secondStart.toLong() * 1000) }

    val lastChecked = remember { mutableIntStateOf(0) }
    var prevSystemTime = System.currentTimeMillis()

    var firstLastMoveTime by remember { mutableLongStateOf(0) }
    var secondLastMoveTime by remember { mutableLongStateOf(0) }
    var firstDelayLeft by remember {
        mutableLongStateOf(
            if (isDelayOn) selectedTime.firstAdd.toLong() * 1000
            else 0
        )
    }
    var secondDelayLeft by remember {
        mutableLongStateOf(
            if (isDelayOn) selectedTime.secondAdd.toLong() * 1000
            else 0
        )
    }

    val firstTimeSelector = remember { mutableStateOf(false) }
    SelectTimePopup(firstTimeSelector, firstTimeLeft/1000) { firstTimeLeft = it*1000 }
    val secondTimeSelector = remember { mutableStateOf(false) }
    SelectTimePopup(secondTimeSelector, secondTimeLeft/1000) { secondTimeLeft = it*1000 }

    LaunchedEffect(isPaused.value) {
        if (isPaused.value) {
            firstChecked.value = false
            secondChecked.value = false
        }

        while (!isPaused.value) {
            val passed = System.currentTimeMillis() - prevSystemTime
            prevSystemTime = System.currentTimeMillis()

            val firstNew: Long
            val secondNew: Long
            var firstDelayNew = firstDelayLeft
            var secondDelayNew = secondDelayLeft

            when (selectedTime.mode) {
                Constants.TimerMode.FISCHER -> {
                    firstNew = firstTimeLeft - passed
                    secondNew = secondTimeLeft - passed
                }

                Constants.TimerMode.BRONSTEIN -> {
                    firstNew = firstTimeLeft - passed
                    secondNew = secondTimeLeft - passed
                }

                Constants.TimerMode.DELAY -> {
                    if (firstDelayLeft < 0) {
                        firstNew = firstTimeLeft - passed
                    } else {
                        firstNew = firstTimeLeft
                        firstDelayNew = firstDelayLeft - passed
                    }

                    if (secondDelayLeft < 0) {
                        secondNew = secondTimeLeft - passed
                    } else {
                        secondNew = secondTimeLeft
                        secondDelayNew = secondDelayLeft - passed
                    }
                }
            }

            if (firstNew <= 0 || secondNew <= 0) {
                if (firstNew <= 0) {
                    firstTimeLeft = 0
                } else {
                    secondTimeLeft = 0
                }
                break
            }

            if (firstChecked.value) {
                firstTimeLeft = firstNew
                if (isDelayOn) { firstDelayLeft = firstDelayNew }
            }
            if (secondChecked.value) {
                secondTimeLeft = secondNew
                if (isDelayOn) { secondDelayLeft = secondDelayNew }
            }

            delay(50)
        }
    }

    val firstAlpha by animateFloatAsState(
        if (firstChecked.value) 1f else 0f,
        tween(200),
        label = "alpha"
    )
    val secondAlpha by animateFloatAsState(
        if (secondChecked.value) 1f else 0f,
        tween(200),
        label = "alpha"
    )

    val onSettingsOpen = { isFirst: Boolean ->
        isPaused.value = true

        firstChecked.value = false
        secondChecked.value = false

        if (isFirst) {
            firstTimeSelector.value = true
        } else {
            secondTimeSelector.value = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Player(
                firstChecked,
                Modifier.rotate(180f),
                {
                    isPaused.value = false
                    val isNotStarted = (!firstChecked.value && !secondChecked.value)
                    val isNotEnded = (firstTimeLeft.toInt() != 0 && secondTimeLeft.toInt() != 0)

                    if ((firstChecked.value || isNotStarted) && isNotEnded) {
                        firstChecked.value = false
                        secondChecked.value = true

                        if (!isNotStarted || lastChecked.intValue == 2) {
                            lastChecked.intValue = 1
                            firstMoveNumber += 1
                            when (selectedTime.mode) {
                                Constants.TimerMode.FISCHER ->
                                { firstTimeLeft += selectedTime.secondAdd * 1000 }
                                Constants.TimerMode.BRONSTEIN ->
                                {
                                    val plus = firstLastMoveTime - firstTimeLeft
                                    if (plus < selectedTime.firstAdd*1000) firstTimeLeft += plus
                                    else firstTimeLeft += selectedTime.firstAdd * 1000
                                }
                                Constants.TimerMode.DELAY -> { }
                            }
                            firstLastMoveTime = firstTimeLeft
                        } else if (lastChecked.intValue == 0) {
                            firstLastMoveTime = firstTimeLeft
                            secondLastMoveTime = secondTimeLeft
                            firstMoveNumber += 1
                        }
                        if (isDelayOn) firstDelayLeft = (selectedTime.firstAdd * 1000).toLong()
                    }
                },
                startContent = {
                    PlayerInterface(firstTimeLeft, firstDelayLeft, secondTimeLeft, firstMoveNumber, isPaused,
                        { onSettingsOpen(true) }, false, firstAlpha, MaterialTheme.colorScheme.background, innerPadding)
                },
                checkedContent = {
                    PlayerInterface(firstTimeLeft, firstDelayLeft, secondTimeLeft, firstMoveNumber, isPaused,
                        { onSettingsOpen(true) }, true, firstAlpha, MaterialTheme.colorScheme.primary, innerPadding
                    )
                }
            )
        }
        CentralMenu(
            isPaused,
            (firstTimeLeft.toInt() == 0 || secondTimeLeft.toInt() == 0),
            onResume = {
                if (lastChecked.intValue == 1) {
                    firstChecked.value = true
                } else if (lastChecked.intValue == 2) {
                    secondChecked.value = true
                }
            },
            onReset = {
                firstChecked.value = false
                secondChecked.value = false

                firstTimeLeft = selectedTime.firstStart.toLong() * 1000
                secondTimeLeft = selectedTime.secondStart.toLong() * 1000
                if (isDelayOn) {
                    secondDelayLeft = (selectedTime.secondAdd * 1000).toLong()
                    firstDelayLeft = (selectedTime.firstAdd * 1000).toLong()
                }
                firstMoveNumber = 0
                secondMoveNumber = 0
                secondLastMoveTime = 0
                firstLastMoveTime = 0
                lastChecked.intValue = 0
                isPaused.value = true
            }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Player(
                secondChecked,
                Modifier,
                {
                    isPaused.value = false
                    val isNotStarted = (!firstChecked.value && !secondChecked.value)
                    val isNotEnded = (firstTimeLeft.toInt() != 0 && secondTimeLeft.toInt() != 0)

                    if ((secondChecked.value || isNotStarted) && isNotEnded) {
                        firstChecked.value = true
                        secondChecked.value = false

                        if (!isNotStarted || lastChecked.intValue == 1) {
                            lastChecked.intValue = 2
                            secondMoveNumber += 1
                            when (selectedTime.mode) {
                                Constants.TimerMode.FISCHER ->
                                    { secondTimeLeft += selectedTime.secondAdd * 1000 }
                                Constants.TimerMode.BRONSTEIN ->
                                    {
                                        val plus = secondLastMoveTime - secondTimeLeft
                                        if (plus < selectedTime.secondAdd*1000) secondTimeLeft += plus
                                        else secondTimeLeft += selectedTime.secondAdd*1000
                                    }
                                Constants.TimerMode.DELAY -> { }
                            }
                            secondLastMoveTime = secondTimeLeft
                        } else if (lastChecked.intValue == 0) {
                            firstLastMoveTime = firstTimeLeft
                            secondLastMoveTime = secondTimeLeft
                            secondMoveNumber += 1
                        }
                        if (isDelayOn) secondDelayLeft = (selectedTime.secondAdd * 1000).toLong()
                    }
                },
                startContent = {
                    PlayerInterface(secondTimeLeft, secondDelayLeft, firstTimeLeft, secondMoveNumber, isPaused,
                        { onSettingsOpen(false) }, false, secondAlpha, MaterialTheme.colorScheme.background, innerPadding)
                },
                checkedContent = {
                    PlayerInterface(secondTimeLeft, secondDelayLeft, firstTimeLeft, secondMoveNumber, isPaused,
                        { onSettingsOpen(false) }, true, secondAlpha, MaterialTheme.colorScheme.primary, innerPadding)
                }
            )
        }
    }
}

@Composable
fun Player(
    checked: MutableState<Boolean>,
    modifier: Modifier,
    onCheck: () -> Unit,
    startContent: @Composable () -> Unit,
    checkedContent: @Composable () -> Unit
) {
    var isFingerDown: Boolean by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        CircularRevealAnimation(
            if (checked.value) 1f else 0f,
            startContent = {
                startContent()
            },
            endContent = {
                checkedContent()
            },
            onPointerEvent = { event, fingerDown ->
                when (event.type) {
                    PointerEventType.Release -> {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCheck()
                    }
                }
                isFingerDown = fingerDown
            }
        )

    }
}

@Composable
fun CentralMenu(
    isPaused: MutableState<Boolean>,
    isEnd: Boolean,
    onResume: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.05f)
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isPaused.value || isEnd) {
            Button(
                modifier = Modifier.size(64.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                onClick = {
                    isPaused.value = false
                    onResume()
                }
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    "Возобновить",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            Button(
                modifier = Modifier.size(64.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                onClick = { isPaused.value = true }
            ) {
                Icon(
                    Icons.Filled.Pause,
                    "Пауза",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Button(
            modifier = Modifier.size(64.dp),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            onClick = { onReset() }
        ) {
            Icon(
                Icons.Filled.Update,
                "Пауза",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun PlayerInterface(
    timeLeft: Long,
    delayLeft: Long,
    opponentTimeLeft: Long,
    moveNumber: Int,
    isPaused: MutableState<Boolean>,
    onSettings: () -> Unit,
    isActive: Boolean,
    a: Float,
    background: Color,
    innerPadding: PaddingValues
) {
    val redColor = Color.Red

    val timesPair = getTimesPair(timeLeft)
    val general = timesPair.first
    val centi = timesPair.second
    val secondsLeft = timeLeft.toDouble() / 1000

    val opponentTimesPair = getTimesPair(opponentTimeLeft)
    val opponentSecondsLeft = opponentTimeLeft.toDouble() / 1000
    val opponentGeneral = opponentTimesPair.first
    val opponentCenti = if (opponentSecondsLeft <= 5 && opponentSecondsLeft > 0) {opponentTimesPair.second} else ""

    var delayGeneral = ""
    var delayCenti = ""
    if (delayLeft > 0) {
        val delayTimesPair = getTimesPair(delayLeft)
        val delaySecondsLeft = delayLeft.toDouble() / 1000
        delayGeneral = delayTimesPair.first
        delayCenti = if (delaySecondsLeft <= 5 && delaySecondsLeft > 0) {delayTimesPair.second} else ""
    }

    Box(
        modifier = Modifier
            .graphicsLayer { alpha = a }
            .background(
                if (timeLeft.toInt() != 0) {
                    background
                } else {
                    redColor
                }
            )
            .fillMaxSize()
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        if (delayLeft > 0) {
            Text(
                "$delayGeneral$delayCenti",
                modifier = Modifier.offset(
                    y = (-100).dp
                ),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = if (!isActive) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        }

        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = general,
                fontSize = 90.sp,
                fontWeight = FontWeight.Black,
                color = if (!isActive) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        }

        if (secondsLeft <= 5 && secondsLeft > 0) {
            Text(
                centi,
                modifier = Modifier.offset(
                    x=132.dp,
                    y=24.dp
                ),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = if (!isActive) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
        }

        AnimatedVisibility(
            isPaused.value,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            val c = rememberCoroutineScope()
            TextButton (
                modifier = Modifier
                    .offset(y = 100.dp)
                    .size(72.dp),
                shape = CircleShape,
                onClick = {
                    c.launch {
                        onSettings()
                    }
                }
            ) {
                Icon(
                    Icons.Filled.Settings,
                    "Настроить время",
                    modifier = Modifier
                        .size(48.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }

    Row(
        Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "У противника: $opponentGeneral$opponentCenti",
            fontWeight = FontWeight.Black,
            color = if (!isActive) {
                MaterialTheme.colorScheme.onBackground
            } else {
                MaterialTheme.colorScheme.surface
            }
        )

        Text(
            "Ходов: $moveNumber",
            fontWeight = FontWeight.Black,
            color = if (!isActive) {
                MaterialTheme.colorScheme.onBackground
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    }
}

fun getTimesPair(millis: Long): Pair<String, String> {
    val secondsLeft = millis.toDouble() / 1000
    val minutesLeft = floor((secondsLeft) / 60)
    val hoursLeft = floor((minutesLeft) / 60)

    val secondsLeftInMinute = floor(secondsLeft - minutesLeft*60)
    val minutesLeftInHour = floor(minutesLeft - hoursLeft*60)
    val centiSecondsLeftInSecond = ((millis.toDouble() / 100) - (secondsLeftInMinute)*10).toInt()

    val secondsZero = if (secondsLeftInMinute<10) "0" else ""
    val minutesZero = if (minutesLeftInHour<10) "0" else ""

    val hours = if (hoursLeft > 0) {"${hoursLeft.toInt()}:"} else {""}
    val mins = "${minutesZero}${minutesLeftInHour.toInt()}"
    val secs = "${secondsZero}${secondsLeftInMinute.toInt()}"

    return Pair("$hours$mins:$secs", ".$centiSecondsLeftInSecond")
}
