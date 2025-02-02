package com.oltrysifp.chessclock

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oltrysifp.chessclock.models.TimeControl
import com.oltrysifp.chessclock.ui.theme.ChessTimerTheme
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

class Timer : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firstStart = intent.getIntExtra("firstStart", -1)
        val firstAdd = intent.getIntExtra("firstAdd", -1)
        val secondStart = intent.getIntExtra("secondStart", -1)
        val secondAdd = intent.getIntExtra("secondAdd", -1)
        val name = intent.getStringExtra("name")

        val selectedTime = TimeControl(
            firstStart,
            firstAdd,
            secondStart,
            secondAdd,
            name ?: "Без имени"
        )

        setContent {
            EdgeToEdgeConfig(this)

            ChessTimerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier
                            .fillMaxSize()
                    ) {
                        TimerContent(innerPadding, selectedTime)
                    }
                }
            }
        }
    }
}

@Composable
fun TimerContent(innerPadding: PaddingValues, selectedTime: TimeControl) {
    val firstChecked = remember { mutableStateOf(false) }
    val secondChecked = remember { mutableStateOf(false) }

    var firstTimeLeft: Long by remember { mutableLongStateOf(selectedTime.firstStart.toLong() * 1000) }
    var secondTimeLeft: Long by remember { mutableLongStateOf(selectedTime.secondStart.toLong() * 1000) }
    var prevSystemTime = System.currentTimeMillis()

    LaunchedEffect(Unit) {
        while (true) {
            val passed = System.currentTimeMillis() - prevSystemTime
            prevSystemTime = System.currentTimeMillis()

            val firstNew = firstTimeLeft - passed
            val secondNew = secondTimeLeft - passed

            if (firstNew <= 0 || secondNew <= 0) {
                if (firstNew <= 0) {
                    firstTimeLeft = 0
                } else {
                    secondTimeLeft = 0
                }
                break
            }

            if (firstChecked.value) {firstTimeLeft = firstNew}
            if (secondChecked.value) {secondTimeLeft = secondNew}

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

    FirstPlayer(
        firstChecked,
        {
            val isNotStarted = (!firstChecked.value && !secondChecked.value)

            if (!isNotStarted) {
                firstTimeLeft += selectedTime.firstAdd * 1000
            }

            val isNotEnded = (firstTimeLeft.toInt() != 0 && secondTimeLeft.toInt() != 0)
            if ((firstChecked.value || isNotStarted) && isNotEnded) {
                firstChecked.value = false
                secondChecked.value = true
            }
        },
        startContent = {
            PlayerInterface(firstTimeLeft, false, firstAlpha, MaterialTheme.colorScheme.background, innerPadding)
        },
        checkedContent = {
            PlayerInterface(firstTimeLeft, true, firstAlpha, MaterialTheme.colorScheme.primary, innerPadding)
        }
    )
    CentralMenu()
    SecondPlayer(
        secondChecked,
        {
            val isNotStarted = (!firstChecked.value && !secondChecked.value)

            if (!isNotStarted) {
                secondTimeLeft += selectedTime.secondAdd * 1000
            }

            val isNotEnded = (firstTimeLeft.toInt() != 0 && secondTimeLeft.toInt() != 0)

            if ((secondChecked.value || isNotStarted) && isNotEnded) {
                firstChecked.value = true
                secondChecked.value = false
            }
        },
        startContent = {
            PlayerInterface(secondTimeLeft, false, secondAlpha, MaterialTheme.colorScheme.background, innerPadding)
        },
        checkedContent = {
            PlayerInterface(secondTimeLeft, true, secondAlpha, MaterialTheme.colorScheme.primary, innerPadding)
        }
    )
}

@Composable
fun FirstPlayer(
    checked: MutableState<Boolean>,
    onCheck: () -> Unit,
    startContent: @Composable () -> Unit,
    checkedContent: @Composable () -> Unit
) {
    var isFingerDown: Boolean by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .rotate(180f)
            .fillMaxHeight(0.45f)
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
                        onCheck()
                    }
                }
                isFingerDown = fingerDown
            }
        )
    }
}

@Composable
fun SecondPlayer(
    checked: MutableState<Boolean>,
    onCheck: () -> Unit,
    startContent: @Composable () -> Unit,
    checkedContent: @Composable () -> Unit
) {
    var isFingerDown: Boolean by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
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
                        onCheck()
                    }
                }
                isFingerDown = fingerDown
            }
        )

    }
}

@Composable
fun CircularRevealAnimation(
    revealPercentTarget: Float,
    startContent: @Composable @UiComposable () -> Unit,
    endContent: @Composable @UiComposable () -> Unit,
    modifier: Modifier = Modifier,
    onPointerEvent: ((event: PointerEvent, isFingerDown: Boolean) -> Unit)? = null,
    animationSpec: AnimationSpec<Float> = spring(),
    topContentIsTransparent: Boolean = false,
) {
    // Tracks if the finger is up or down in real time
    var isFingerDown: Boolean by remember { mutableStateOf(false) }
    // Tracks the last position of the finger for the duration of the animation
    val fingerOffsetState: MutableState<Offset?> = remember { mutableStateOf(null) }
    // The percent of the top layer to clip
    val endContentClipPercent by animateFloatAsState(
        targetValue = revealPercentTarget,
        label = "Circular Reveal Clip Percent",
        animationSpec = animationSpec,
        finishedListener = {
            if (!isFingerDown) {
                fingerOffsetState.value = null
            }
        }
    )

    Box(
        modifier
            .pointerInput(onPointerEvent) {
            awaitPointerEventScope {
                while (true) {
                    val event: PointerEvent = awaitPointerEvent()

                    if (revealPercentTarget == 1f) {
                        return@awaitPointerEventScope
                    }

                    when (event.type) {
                        PointerEventType.Press -> {
                            isFingerDown = true
                            val offset = event.changes.last().position
                            fingerOffsetState.value = offset
                        }
                        PointerEventType.Release -> {
                            if (isFingerDown) {
                                isFingerDown = false
                            }
                        }
                        PointerEventType.Move -> {
                            if (isFingerDown) {
                                val offset = event.changes.last().position
                                if (
                                    offset.x < 0 ||
                                    offset.y < 0 ||
                                    offset.x > size.width ||
                                    offset.y > size.height
                                ) {
                                    isFingerDown = false
                                } else {
                                    fingerOffsetState.value = offset
                                }
                            }
                        }
                        else -> Log.v(TAG, "Unexpected Event type ${event.type}")
                    }

                    onPointerEvent?.invoke(event, isFingerDown)
                }
            }

            // Explicitly don't return the awaitPointerEventScope to avoid lint warning
            @Suppress("RedundantUnitExpression", "RedundantSuppression")
            Unit
        },
    ) {
        // Draw the bottom layer if the top layer is transparent, or the top isn't fully animated in.
        if (endContentClipPercent < 1f || topContentIsTransparent) {
            startContent()
        }

        val fingerOffset: Offset? = fingerOffsetState.value
        // Draw the top layer if it's not being fully clipped by the mask
        if (endContentClipPercent > 0f) {
            val path: Path = remember { Path() }

            val clipModifier: Modifier = if (endContentClipPercent < 1f && fingerOffset != null) {
                Modifier.drawWithContent {
                    path.rewind()

                    val largestDimension = max(size.width, size.height)

                    path.addOval(
                        Rect(
                            center = fingerOffset,
                            radius = endContentClipPercent * largestDimension
                        )
                    )

                    clipPath(path) {
                        this@drawWithContent.drawContent()
                    }
                }
            } else {
                Modifier
            }

            Box(
                modifier = clipModifier
            ) {
                endContent()
            }
        }
    }
}

@Composable
fun CentralMenu() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.1f)
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.PlayArrow,
            "Пауза",
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
fun PlayerInterface(timeLeft: Long, isActive: Boolean, a: Float, background: Color, innerPadding: PaddingValues) {
    val redColor = Color.Red
    val secondsLeft = timeLeft.toDouble() / 1000
    val minutesLeft = floor((secondsLeft+1) / 60)

    val secondsLeftInMinute = ceil(secondsLeft - minutesLeft*60)
    val centiSecondsLeftInSecond = ((timeLeft.toDouble() / 100) - (secondsLeftInMinute-1)*10).toInt()

    val secondsZero = if (secondsLeftInMinute<10) "0" else ""
    val minutesZero = if (minutesLeft<10) "0" else ""

    val mins = "${minutesZero}${minutesLeft.toInt()}"
    val secs = "${secondsZero}${secondsLeftInMinute.toInt()}"

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
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "${mins}:${secs}",
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
                ".${centiSecondsLeftInSecond}",
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
    }
}