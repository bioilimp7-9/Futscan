package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "survey_responses")
data class SurveyResponse(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val respondentAge: Int = 14,
    // 1. Тағам өнімін сатып алғанда құрамын оқисың ба?
    val q1ReadsLabel: String,
    // 2. Қант мөлшерін қаптамадан таба аласың ба?
    val q2FindsSugar: String,
    // 3. 100 грамм және бір порция көрсеткіштерінің айырмашылығын білесің бе?
    val q3Knows100gVsServing: String,
    // 4. Екі өнімнің құрамын салыстыра аласың ба?
    val q4CanCompare: String,
    // 5. FOODSCAN AI жүйесі ақпаратты түсінуге көмектесті ме?
    val q5HelpedUnderstand: String,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
