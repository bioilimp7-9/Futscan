package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TestPhase(val titleKz: String, val badgeKz: String) {
    PRE("Пайдалануға дейін (Pre-test)", "Дейін"),
    POST("Пайдаланудан кейін (Post-test)", "Кейін");

    companion object {
        fun fromString(value: String?): TestPhase {
            return if (value == POST.name) POST else PRE
        }
    }
}

@Entity(tableName = "student_test_attempts")
data class StudentTestAttempt(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentName: String,
    val studentAge: Int = 14,
    val studentGrade: String = "8 сынып",
    val phase: String = TestPhase.PRE.name,
    val answersCsv: String,
    val score: Int,
    val totalQuestions: Int = 8,
    val timestamp: Long = System.currentTimeMillis()
)
