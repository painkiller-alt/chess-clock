package com.oltrysifp.chessclock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import com.oltrysifp.chessclock.models.UserData
import com.oltrysifp.chessclock.util.DataStoreManager
import com.oltrysifp.chessclock.util.log

@Composable
fun LoadUserData(
    dataStoreManager: DataStoreManager,
    userData: UserData,
    dataLoaded: MutableState<Boolean>
) {
    LaunchedEffect(key1 = true) {
        dataStoreManager.getUserData().collect {
            userData.selectedTimeControl = it.selectedTimeControl
            userData.customTimeControls = it.customTimeControls

            log("user data reloaded: $userData")

            dataLoaded.value = true
        }
    }
}