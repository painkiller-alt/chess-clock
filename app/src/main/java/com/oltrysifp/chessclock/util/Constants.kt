package com.oltrysifp.chessclock.util

import com.oltrysifp.chessclock.models.UserData

object Constants {
    fun userDataDefault(): UserData {
        return UserData(
            selectedTimeControl = null,
            customTimeControls = mutableListOf()
        )
    }

    enum class TimerMode {
        FISCHER, BRONSTEIN, DELAY
    }

    val TimerModesDescr = mapOf(
        TimerMode.FISCHER to
                "Полное добавочное время прибавляется после каждого хода",
        TimerMode.BRONSTEIN to
                "Использованная часть добавочного времени прибавляется после каждого хода",
        TimerMode.DELAY to
                "Время начинает отниматься после задержки",
    )
}