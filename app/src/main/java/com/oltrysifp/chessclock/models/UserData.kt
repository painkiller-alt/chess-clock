package com.oltrysifp.chessclock.models

import kotlinx.serialization.Serializable

@Serializable
data class UserData (
    var selectedTimeControl: TimeControl?,
    var customTimeControls: MutableList<TimeControl>
)