package com.example.data

import kotlinx.coroutines.flow.Flow

class MedicalRepository(
    private val hospitalVisitDao: HospitalVisitDao,
    private val appointmentReminderDao: AppointmentReminderDao
) {
    val allVisits: Flow<List<HospitalVisit>> = hospitalVisitDao.getAllVisits()
    val allReminders: Flow<List<AppointmentReminder>> = appointmentReminderDao.getAllReminders()

    suspend fun insertVisit(visit: HospitalVisit) {
        hospitalVisitDao.insertVisit(visit)
    }

    suspend fun updateVisit(visit: HospitalVisit) {
        hospitalVisitDao.updateVisit(visit)
    }

    suspend fun deleteVisit(visit: HospitalVisit) {
        hospitalVisitDao.deleteVisit(visit)
    }

    suspend fun insertReminder(reminder: AppointmentReminder): Long {
        return appointmentReminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: AppointmentReminder) {
        appointmentReminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminder: AppointmentReminder) {
        appointmentReminderDao.deleteReminder(reminder)
    }
}
