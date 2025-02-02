package com.oltrysifp.chessclock.models

import kotlinx.serialization.Serializable

@Serializable
data class UserData (
    val selectedTimeControl: TimeControl,
    val customTimeControls: List<TimeControl>
)