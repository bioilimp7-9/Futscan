package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MeasurementBasis(val titleKz: String, val shortKz: String) {
    PER_100G("100 г үшін", "100 г"),
    PER_SERVING("1 порция үшін", "порция");

    companion object {
        fun fromString(value: String?): MeasurementBasis {
            return when (value) {
                "PER_SERVING" -> PER_SERVING
                else -> PER_100G
            }
        }
    }
}

enum class VerificationStatus(val titleKz: String) {
    UNVERIFIED("Тексерілмеген"),
    VERIFIED("Тексерілді (сәйкес)"),
    CORRECTED("Түзетілді");

    companion object {
        fun fromString(value: String?): VerificationStatus {
            return when (value) {
                "VERIFIED" -> VERIFIED
                "CORRECTED" -> CORRECTED
                else -> UNVERIFIED
            }
        }
    }
}

@Entity(tableName = "research_products")
data class ProductItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productNumber: Int = 1,
    val productName: String,
    val category: String,
    val manufacturer: String = "",
    val ingredients: String = "",
    val servingSizeText: String = "100 г",
    val measurementBasis: String = MeasurementBasis.PER_100G.name,

    // Actual verified values (confirmed from physical packaging label)
    val sugar: Float? = null,
    val salt: Float? = null,
    val fat: Float? = null,
    val protein: Float? = null,
    val calories: Float? = null,

    // Values extracted by AI/OCR
    val extractedSugar: Float? = null,
    val extractedSalt: Float? = null,
    val extractedFat: Float? = null,
    val extractedProtein: Float? = null,
    val extractedCalories: Float? = null,

    // Image/Preset reference
    val imageUri: String? = null,
    val samplePresetId: String? = null,

    // Scientific verification metadata
    val verificationStatus: String = VerificationStatus.UNVERIFIED.name,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
