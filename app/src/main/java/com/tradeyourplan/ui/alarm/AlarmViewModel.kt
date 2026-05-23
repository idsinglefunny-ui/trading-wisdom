// app/src/main/java/com/tradeyourplan/ui/alarm/AlarmViewModel.kt
package com.tradeyourplan.ui.alarm

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.domain.usecase.*
import com.tradeyourplan.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val application: Application,
    private val getAlarmsUseCase: GetAlarmsUseCase,
    private val addAlarmUseCase: AddAlarmUseCase,
    private val updateAlarmUseCase: UpdateAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val toggleAlarmUseCase: ToggleAlarmUseCase
) : ViewModel() {

    private val alarmScheduler = AlarmScheduler(application)

    val alarms: StateFlow<List<Alarm>> = getAlarmsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val id = addAlarmUseCase(alarm)
            val savedAlarm = alarm.copy(id = id, isEnabled = true)
            if (savedAlarm.isEnabled) {
                alarmScheduler.scheduleAlarm(savedAlarm)
            }
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            updateAlarmUseCase(alarm)
            alarmScheduler.cancelAlarm(alarm.id)
            if (alarm.isEnabled) {
                alarmScheduler.scheduleAlarm(alarm)
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmScheduler.cancelAlarm(alarm.id)
            deleteAlarmUseCase(alarm)
        }
    }

    fun toggleAlarm(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            toggleAlarmUseCase(id, enabled)
            if (enabled) {
                val alarm = alarms.value.find { it.id == id }
                if (alarm != null) {
                    alarmScheduler.scheduleAlarm(alarm.copy(isEnabled = true))
                }
            } else {
                alarmScheduler.cancelAlarm(id)
            }
        }
    }
}
