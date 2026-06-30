package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentReminderDao {
    @Query("SELECT * FROM appointment_reminders ORDER BY dateTimeMillis ASC")
    fun getAllReminders(): Flow<List<AppointmentReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: AppointmentReminder): Long

    @Update
    suspend fun updateReminder(reminder: AppointmentReminder)

    @Delete
    suspend fun deleteReminder(reminder: AppointmentReminder)
}
