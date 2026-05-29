package com.example.data

import kotlinx.coroutines.flow.Flow

class PomodoroRepository(private val pomodoroDao: PomodoroDao) {
    val allSessions: Flow<List<PomodoroSession>> = pomodoroDao.getAllSessions()

    suspend fun insertSession(session: PomodoroSession) {
        pomodoroDao.insertSession(session)
    }

    suspend fun deleteSessionById(id: Int) {
        pomodoroDao.deleteSessionById(id)
    }

    suspend fun clearAllSessions() {
        pomodoroDao.clearAllSessions()
    }
}
