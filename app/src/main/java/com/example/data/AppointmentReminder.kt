package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointment_reminders")
data class AppointmentReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val hospitalName: String,
    val dateTimeStr: String, // format: YYYY-MM-DD HH:MM
    val dateTimeMillis: Long, // trigger time
    val notes: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
