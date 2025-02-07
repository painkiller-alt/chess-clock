package com.oltrysifp.chessclock.models

import com.oltrysifp.chessclock.util.Constants
import kotlinx.serialization.Serializable

@Serializable
data class TimeControl (
    val firstStart: Int,
    val secondStart: Int,
    val firstAdd: Int,
    val secondAdd: Int,
    val name: String,
    val mode: Constants.TimerMode = Constants.TimerMode.FISCHER,
)