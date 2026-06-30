package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HospitalVisitDao {
    @Query("SELECT * FROM hospital_visits ORDER BY visitDate DESC")
    fun getAllVisits(): Flow<List<HospitalVisit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: HospitalVisit)

    @Update
    suspend fun updateVisit(visit: HospitalVisit)

    @Delete
    suspend fun deleteVisit(visit: HospitalVisit)
}
