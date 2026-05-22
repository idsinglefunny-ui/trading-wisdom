package com.tradeyourplan.domain.usecase

import com.tradeyourplan.data.repository.AlarmRepository
import javax.inject.Inject

class ToggleAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(id: Long, enabled: Boolean) {
        repository.setAlarmEnabled(id, enabled)
    }
}
