package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DataSourceTag(val labelKz: String) {
    OCR("Қаптамадан оқылды"),
    CAMERA("Камера арқылы талданды"),
    PH_PAPER("pH қағазы арқылы бағаланды"),
    USER("Пайдаланушы енгізді"),
    NONE("Дерек жоқ")
}

enum class VisionStatusOption(val labelKz: String) {
    LOW_CHANGES("Көрінетін өзгерістер аз"),
    VISIBLE_CHANGES("Сыртқы өзгерістер байқалды"),
    CLEAR_CHANGES("Айқын сыртқы өзгерістер байқалды"),
    INSUFFICIENT_QUALITY("Сурет сапасы талдауға жеткісіз")
}

@Entity(tableName = "freshness_vision_records")
data class FreshnessVisionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productName: String,
    val recognizedFoodName: String,
    val surfaceDescription: String,
    val observedChanges: String,
    val comparativeConclusion: String,
    val confidenceAndLimits: String,
    val statusLabel: String,
    val imageUri: String? = null,
    val dataSource: String = DataSourceTag.CAMERA.labelKz,
    val measuredAtText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chemical_ph_records")
data class ChemicalPhRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productName: String,
    val sampleType: String,
    val paperColor: String,
    val scaleVisible: Boolean,
    val estimatedPhRange: String,
    val acidityExplanation: String,
    val confidenceLevel: String,
    val errorMarginNote: String,
    val imageUri: String? = null,
    val dataSource: String = DataSourceTag.PH_PAPER.labelKz,
    val measuredAtText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "storage_monitor_records")
data class StorageMonitorRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productName: String,
    val roomTempC: Float? = null,
    val fridgeTempC: Float? = null,
    val humidityPercent: Float? = null,
    val lightCondition: String = "Өлшенбеген",
    val storageLocation: String = "Өлшенбеген",
    val storageDuration: String = "Дерек жоқ",
    val dataSource: String = DataSourceTag.USER.labelKz,
    val measuredAtText: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Future external hardware sensor integration contract (ESP32 / BLE).
 * Currently no physical sensors are connected; all readings are entered manually.
 */
interface StorageSensorProvider {
    val isConnected: Boolean
    val providerStatusKz: String
}

class DisconnectedEsp32SensorProvider : StorageSensorProvider {
    override val isConnected: Boolean = false
    override val providerStatusKz: String =
        "Физикалық датчиктер қосылмаған. Параметрлер қолмен енгізіледі (ESP32 қосуға дайын архитектура)."
}
