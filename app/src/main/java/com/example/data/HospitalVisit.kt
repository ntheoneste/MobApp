package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hospital_visits")
data class HospitalVisit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hospitalName: String,
    val doctorName: String,
    val visitDate: String, // format: YYYY-MM-DD
    val reason: String,
    val diagnosis: String,
    val treatmentPlan: String,
    val prescriptions: String,
    val notes: String,
    val createdAt: Long = System.currentTimeMillis()
)
