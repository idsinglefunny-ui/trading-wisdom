package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.model.Alarm
import com.tradeyourplan.data.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlarmsUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    operator fun invoke(): Flow<List<Alarm>> {
        return repository.getAllAlarms()
    }

    suspend fun getEnabledAlarms(): List<Alarm> {
        return repository.getEnabledAlarms()
    }
}
