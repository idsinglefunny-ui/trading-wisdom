package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.data.repository.AlarmRepository
import javax.inject.Inject

class AddAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(alarm: Alarm): Long {
        return repository.addAlarm(alarm)
    }
}
