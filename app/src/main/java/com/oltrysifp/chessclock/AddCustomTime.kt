package com.oltrysifp.chessclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import com.oltrysifp.chessclock.composable.AnimatedError
import com.oltrysifp.chessclock.composable.SelectTimePopup
import com.oltrysifp.chessclock.models.TimeControl
import com.oltrysifp.chessclock.models.UserData
import com.oltrysifp.chessclock.ui.theme.ChessTimerTheme
import com.oltrysifp.chessclock.util.Constants
import com.oltrysifp.chessclock.util.DataStoreManager
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class AddCustomTime : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userDataString = intent.getStringExtra("userData") ?: return
        val userData = Json.decodeFromString<UserData>(userDataString)

        setContent {
            val context = LocalContext.current

            val dataStoreManager = DataStoreManager(context)

            val name = remember { mutableStateOf("") }
            val timerMode = remember { mutableStateOf(Constants.TimerMode.FISCHER) }
            var split by remember { mutableStateOf(false) }

            var errorSameName by remember { mutableStateOf(false) }
            var errorEmptyName by remember { mutableStateOf(false) }
            var errorZeroStartTimeFirst by remember { mutableStateOf(false) }
            var errorZeroStartTimeSecond by remember { mutableStateOf(false) }

            val firstPlayerTime = remember { mutableLongStateOf(0) }
            val firstPlayerAdd = remember { mutableLongStateOf(0) }
            val secondPlayerTime = remember { mutableLongStateOf(0) }
            val secondPlayerAdd = remember { mutableLongStateOf(0) }

            val firstTimeSelector = remember { mutableStateOf(false) }
            SelectTimePopup(firstTimeSelector, firstPlayerTime.longValue) { firstPlayerTime.longValue = it }
            val firstAddSelector = remember { mutableStateOf(false) }
            SelectTimePopup(firstAddSelector, firstPlayerAdd.longValue) { firstPlayerAdd.longValue = it }
            val secondTimeSelector = remember { mutableStateOf(false) }
            SelectTimePopup(secondTimeSelector, secondPlayerTime.longValue) { secondPlayerTime.longValue = it }
            val secondAddSelector = remember { mutableStateOf(false) }
            SelectTimePopup(secondAddSelector, secondPlayerAdd.longValue) { secondPlayerAdd.longValue = it }

            val onChange = {
                if (!split) {
                    secondPlayerTime.longValue = firstPlayerTime.longValue
                    secondPlayerAdd.longValue = firstPlayerAdd.longValue
                }
            }

            LaunchedEffect(
                firstPlayerTime.longValue,
                firstPlayerAdd.longValue
            ) {
                onChange()
            }
            LaunchedEffect(key1=firstPlayerTime.longValue) { errorZeroStartTimeFirst = false }
            LaunchedEffect(key1=secondPlayerTime.longValue) { errorZeroStartTimeSecond = false }

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
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            maxLength = 30,
                            onValueAfter = {
                                errorEmptyName = false
                                errorSameName = false
                            }
                        )

                        AnimatedError(
                            errorSameName,
                            "Такое название уже существует"
                        )
                        AnimatedError(
                            errorEmptyName,
                            "Название не может быть пустым"
                        )

                        Spacer(Modifier.padding(16.dp))

                        Column {
                            AnimatedContent(
                                split,
                                label = "PlayerDiff"
                            ) {
                                Text(
                                    if (it) "1 Игрок" else "Оба игрока"
                                )
                            }

                            Spacer(Modifier.padding(4.dp))

                            Column(
                                Modifier
                                    .fillMaxWidth()
                            ) {
                                val timePair = getTimesPair(firstPlayerTime.longValue * 1000)
                                val timeString = timePair.first
                                InputCard(
                                    "Время",
                                    timeString,
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    firstTimeSelector.value = true
                                }
                                AnimatedError(
                                    errorZeroStartTimeFirst,
                                    "минимум 1 сек."
                                )
                            }
                            Spacer(Modifier.padding(4.dp))

                            val addPair = getTimesPair(firstPlayerAdd.longValue * 1000)
                            val addString = addPair.first
                            Row(
                                Modifier
                                    .fillMaxWidth()
                            ) {
                                AnimatedContent(
                                    timerMode.value,
                                    label = "timerModeDiff",
                                    transitionSpec = {
                                        if (targetState == Constants.TimerMode.DELAY || initialState  == Constants.TimerMode.DELAY )
                                        // Default animation
                                        {(fadeIn(animationSpec = tween(220, delayMillis = 90)) +             scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))             .togetherWith(fadeOut(animationSpec = tween(90)))}
                                        else
                                        // No animation
                                        { EnterTransition.None togetherWith ExitTransition.None }
                                    }
                                ) {
                                    InputCard(
                                        if (it == Constants.TimerMode.DELAY) {"Задержка"}
                                        else {"Добавление"},
                                        addString,
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        firstAddSelector.value = true
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.padding(10.dp))

                        AnimatedVisibility(split) {
                            Column {
                                Text("2 Игрок")

                                Spacer(Modifier.padding(2.dp))

                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                ) {
                                    val timePair = getTimesPair(secondPlayerTime.longValue * 1000)
                                    val timeString = timePair.first
                                    InputCard(
                                        "Время",
                                        timeString,
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        secondTimeSelector.value = true
                                    }
                                    AnimatedError(
                                        errorZeroStartTimeSecond,
                                        "минимум 1 сек."
                                    )
                                }

                                Spacer(Modifier.padding(4.dp))

                                val addPair = getTimesPair(secondPlayerAdd.longValue * 1000)
                                val addString = addPair.first
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                ) {
                                    AnimatedContent(
                                        timerMode.value,
                                        label = "timerModeDiff"
                                    ) {
                                        InputCard(
                                            if (it == Constants.TimerMode.DELAY) {"Задержка"}
                                            else {"Добавление"},
                                            addString,
                                            color = MaterialTheme.colorScheme.surface
                                        ) {
                                            secondAddSelector.value = true
                                        }
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

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                onClick = {
                                    when (timerMode.value) {
                                        Constants.TimerMode.FISCHER ->
                                            {timerMode.value = Constants.TimerMode.BRONSTEIN }
                                        Constants.TimerMode.BRONSTEIN ->
                                            {timerMode.value = Constants.TimerMode.DELAY }
                                        Constants.TimerMode.DELAY ->
                                            {timerMode.value = Constants.TimerMode.FISCHER }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Filled.ArrowBackIosNew,
                                    "Предыдущий",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            AnimatedContent(
                                timerMode.value,
                                label = "slider"
                            ) {
                                Text(
                                    when (it) {
                                        Constants.TimerMode.FISCHER -> "Фишер"
                                        Constants.TimerMode.BRONSTEIN -> "Бронштейн"
                                        Constants.TimerMode.DELAY -> "Задержка"
                                    }
                                )
                            }

                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                onClick = {
                                    when (timerMode.value) {
                                        Constants.TimerMode.BRONSTEIN ->
                                        {timerMode.value = Constants.TimerMode.FISCHER }
                                        Constants.TimerMode.DELAY ->
                                        {timerMode.value = Constants.TimerMode.BRONSTEIN }
                                        Constants.TimerMode.FISCHER ->
                                        {timerMode.value = Constants.TimerMode.DELAY }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    "Предыдущий",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        AnimatedContent(
                            timerMode.value,
                            label = "textAnim",
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Constants.TimerModesDescr[it]?.let { text ->
                                Text(
                                    text,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(Modifier.padding(10.dp))

                        val userDataSaveCoroutine = rememberCoroutineScope()
                        Button(
                            shape = RoundedCornerShape(6.dp),
                            onClick = {
                                val firstPlayerTimeGot = firstPlayerTime.longValue
                                val firstPlayerAddGot = firstPlayerAdd.longValue

                                val secondPlayerTimeGot = secondPlayerTime.longValue
                                val secondPlayerAddGot = secondPlayerAdd.longValue

                                val timeControl = TimeControl(
                                    firstStart = firstPlayerTimeGot.toInt(),
                                    secondStart = secondPlayerTimeGot.toInt(),
                                    firstAdd = firstPlayerAddGot.toInt(),
                                    secondAdd = secondPlayerAddGot.toInt(),
                                    name = name.value,
                                    mode = timerMode.value
                                )

                                var isError = false
                                if (timeControl.name == "") {
                                    errorEmptyName = true
                                    isError = true
                                }
                                if (userData.customTimeControls.find { it.name == timeControl.name } != null) {
                                    errorSameName = true
                                    isError = true
                                }
                                if (timeControl.firstStart == 0) {
                                    errorZeroStartTimeFirst = true
                                    isError = true
                                }
                                if (timeControl.secondStart == 0) {
                                    errorZeroStartTimeSecond = true
                                    isError = true
                                }

                                if (isError) {
                                    return@Button
                                }

                                userData.customTimeControls.add(timeControl)

                                userDataSaveCoroutine.launch {
                                    dataStoreManager.saveUserData(userData)
                                    finish()
                                }
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
    externalChange: MutableState<Boolean>,
    maxNum: Int = 60,
    onEnter: () -> Unit
) {
    val text = remember { mutableStateOf(TextFieldValue(textState.value)) }

    LaunchedEffect(
        externalChange
    ) {
        text.value = TextFieldValue(textState.value)
        externalChange.value = false
    }

    val focusManager = LocalFocusManager.current
    val onSubmit = {
        onEnter()
    }

    Box {
        BasicTextField(
            text.value,
            {
                text.value = if (it.text.isEmpty()) {
                    it
                } else if (!it.text.isDigitsOnly()) {
                    it.text.isDigitsOnly()
                    val numericString = it.text.toCharArray()
                        .filter { s -> s.isLetterOrDigit() }
                        .joinToString(separator = "")

                    it.copy(
                        text = numericString
                    )
                } else if (it.text.toInt() <= maxNum) {
                    it
                } else {
                    it.copy(
                        text = maxNum.toString()
                    )
                }

                textState.value = text.value.text
            },
            modifier = Modifier
                .width(70.dp)
                .onFocusSelectAll(text),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            keyboardActions = KeyboardActions(
                onDone = {
                    onSubmit()
                    focusManager.clearFocus()
                },
            ),
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 40.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            ),
            decorationBox = {
                Card(
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
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
}

fun Modifier.onFocusSelectAll(textFieldValueState: MutableState<TextFieldValue>): Modifier =
    composed(
        inspectorInfo = debugInspectorInfo {
            name = "textFieldValueState"
            properties["textFieldValueState"] = textFieldValueState
        }
    ) {
        var triggerEffect by remember {
            mutableStateOf<Boolean?>(null)
        }
        if (triggerEffect != null) {
            LaunchedEffect(triggerEffect) {
                val tfv = textFieldValueState.value
                textFieldValueState.value = tfv.copy(selection = TextRange(0, tfv.text.length))
            }
        }
        onFocusChanged { focusState ->
            if (focusState.isFocused) {
                triggerEffect = triggerEffect?.let { bool ->
                    !bool
                } ?: true
            }
        }
    }