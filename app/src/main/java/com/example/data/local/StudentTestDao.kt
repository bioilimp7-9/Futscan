package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.StudentTestAttempt
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentTestDao {
    @Query("SELECT * FROM student_test_attempts ORDER BY timestamp DESC")
    fun getAllAttempts(): Flow<List<StudentTestAttempt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: StudentTestAttempt): Long

    @Delete
    suspend fun deleteAttempt(attempt: StudentTestAttempt)
}
