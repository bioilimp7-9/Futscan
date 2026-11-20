package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SurveyResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface SurveyDao {
    @Query("SELECT * FROM survey_responses ORDER BY timestamp DESC")
    fun getAllResponses(): Flow<List<SurveyResponse>>

    @Query("SELECT COUNT(*) FROM survey_responses")
    fun getResponseCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponse(response: SurveyResponse): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(responses: List<SurveyResponse>)

    @Query("DELETE FROM survey_responses")
    suspend fun clearAll()
}
