package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.data.repository.AlarmRepository
import javax.inject.Inject

class UpdateAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(alarm: Alarm) {
        repository.updateAlarm(alarm)
    }
}
