package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.local.FoodScanDatabase
import com.example.data.model.ChemicalPhRecord
import com.example.data.model.DataSourceTag
import com.example.data.model.DisconnectedEsp32SensorProvider
import com.example.data.model.FreshnessVisionRecord
import com.example.data.model.MeasurementBasis
import com.example.data.model.StorageMonitorRecord
import com.example.data.model.StudentTestAttempt
import com.example.data.model.TestPhase
import com.example.data.model.VisionStatusOption
import com.example.data.repository.FoodScanRepository
import com.example.data.samples.ResearchProtocolData
import com.example.data.samples.SampleData
import com.example.scanner.LabelAnalysisEngine
import com.example.scanner.PlaceholderAiLabelRepository
import com.example.scanner.VisionAnalysisHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FOODSCAN AI 2.0", appName)
    }

    @Test
    fun `test label analysis parser for sugar and salt`() {
        val sampleText = "100 г: Ақуыз 6.3 г, Май 35.6 г, Қант 47.0 г, Тұз 0.05 г, 540 ккал"
        val result = LabelAnalysisEngine.analyzeText(sampleText, "Сынақ шоколад")

        assertEquals(47.0f, result.sugar.value ?: 0f, 0.01f)
        assertEquals(0.05f, result.salt.value ?: 0f, 0.01f)
        assertEquals(35.6f, result.fat.value ?: 0f, 0.01f)
        assertEquals(6.3f, result.protein.value ?: 0f, 0.01f)
        assertEquals(540.0f, result.calories.value ?: 0f, 0.01f)
        assertEquals(MeasurementBasis.PER_100G, result.measurementBasis)
    }

    @Test
    fun `test missing values display exact mandatory warning`() {
        val result = LabelAnalysisEngine.analyzeText("", "Бос сурет")
        assertTrue(result.sugar.isMissing)
        assertTrue(result.salt.isMissing)
        assertTrue(result.sugar.educationalNote.contains("Бұл көрсеткіш суреттен анық оқылмады. Қаптамадан қолмен тексеріңіз."))
    }

    @Test
    fun `test kazakhstan preset authentic packages exist`() {
        val presets = SampleData.samplePackages
        assertTrue(presets.size >= 5)
        val chocolate = presets.find { it.id == "rahat_chocolate" }
        assertNotNull(chocolate)
        assertEquals(47.0f, chocolate!!.sugar ?: 0f, 0.01f)
    }

    @Test
    fun `test placeholder ai repository fallback when firebase declined`() = runBlocking {
        val repo = PlaceholderAiLabelRepository()
        assertFalse(repo.isCloudAiConfigured)

        val fallback = repo.analyzeLabel(imageUri = null, presetId = null, manualText = null)
        assertTrue(fallback.isManualFallback)
        assertTrue(fallback.sugar.isMissing)
        assertTrue(fallback.calories.isMissing)
    }

    @Test
    fun `test 6 sheet research protocol and pre post test summary`() {
        assertEquals(6, ResearchProtocolData.instructionSteps.size)
        assertEquals(8, ResearchProtocolData.eightQuestions.size)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = FoodScanDatabase.getDatabase(context)
        val repo = FoodScanRepository(db.productDao(), db.surveyDao(), db.studentTestDao())

        val attempts = listOf(
            StudentTestAttempt(
                studentName = "Айым",
                phase = TestPhase.PRE.name,
                answersCsv = "A,B,A,C,A,B,C,C",
                score = 4,
                totalQuestions = 8
            ),
            StudentTestAttempt(
                studentName = "Айым",
                phase = TestPhase.POST.name,
                answersCsv = "B,C,B,C,A,B,C,C",
                score = 8,
                totalQuestions = 8
            )
        )
        val summary = repo.summarizeTestAttempts(attempts)
        assertEquals(1, summary.preCount)
        assertEquals(1, summary.postCount)
        assertEquals(1, summary.pairedStudentsCount)
        assertEquals(4.0f, summary.avgPreScore, 0.01f)
        assertEquals(8.0f, summary.avgPostScore, 0.01f)
        assertEquals(50.0f, summary.gainPercent, 0.01f)
    }

    @Test
    fun `test foodscan 20 modules persistence and bitmap tone analysis`() = runBlocking {
        val sensorProvider = DisconnectedEsp32SensorProvider()
        assertFalse(sensorProvider.isConnected)

        val bmp = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.rgb(220, 40, 60))
        val inspection = VisionAnalysisHelper.inspectBitmap(bmp)
        bmp.recycle()
        assertEquals(220, inspection.avgRed)
        assertTrue(inspection.descriptiveToneKz.contains("қызыл"))

        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = FoodScanDatabase.getDatabase(context)
        val repo = FoodScanRepository(
            db.productDao(),
            db.surveyDao(),
            db.studentTestDao(),
            db.foodScan20Dao()
        )

        repo.addFreshnessRecord(
            FreshnessVisionRecord(
                productName = "Алма",
                recognizedFoodName = "Алма",
                surfaceDescription = inspection.descriptiveToneKz,
                observedChanges = "Қараю жоқ",
                comparativeConclusion = "Сыртқы күйі қалыпты",
                confidenceAndLimits = "Шектеу тесті",
                statusLabel = VisionStatusOption.LOW_CHANGES.labelKz,
                measuredAtText = "2026-11-20 10:00"
            )
        )
        repo.addChemicalPhRecord(
            ChemicalPhRecord(
                productName = "Пияз",
                sampleType = "Пияздың судағы сығындысы",
                paperColor = "Сары",
                scaleVisible = false,
                estimatedPhRange = "анықтау мүмкін болмады",
                acidityExplanation = "Шкала жоқ",
                confidenceLevel = "Төмен",
                errorMarginNote = "Шамамен бағалау",
                dataSource = DataSourceTag.NONE.labelKz,
                measuredAtText = "2026-11-20 10:05"
            )
        )
        repo.addStorageRecord(
            StorageMonitorRecord(
                productName = "Алма",
                roomTempC = 21.5f,
                fridgeTempC = null,
                humidityPercent = null,
                lightCondition = "Жарық",
                storageLocation = "Бөлме",
                storageDuration = "1 күн",
                measuredAtText = "2026-11-20 10:10"
            )
        )

        val visionList = repo.allFreshnessRecords.first()
        val phList = repo.allChemicalPhRecords.first()
        val storageList = repo.allStorageRecords.first()
        assertTrue(visionList.any { it.productName == "Алма" })
        assertTrue(phList.any { it.estimatedPhRange == "анықтау мүмкін болмады" })
        val stored = storageList.first { it.productName == "Алма" }
        assertNull(stored.fridgeTempC)
        assertNull(stored.humidityPercent)
    }

    @Test
    fun `test unified report pdf exporter prepares honest summary and filename`() {
        val data = com.example.export.UnifiedReportPdfData(
            productName = "Алма (Апорт)",
            studentAuthor = "Қанағат Айым • 8-сынып",
            schoolProjectTitle = "Мектеп ғылыми жобасы",
            ocrProduct = com.example.data.model.ProductItem(
                productName = "Алма (Апорт)",
                category = "Жемістер",
                measurementBasis = MeasurementBasis.PER_100G.name,
                sugar = 10.4f,
                salt = 0.0f,
                fat = 0.2f,
                protein = 0.3f,
                calories = 52.0f
            ),
            visionRecords = listOf(
                FreshnessVisionRecord(
                    productName = "Алма (Апорт)",
                    recognizedFoodName = "Алма",
                    surfaceDescription = "Қызыл-сары қабық",
                    observedChanges = "Көрінетін өзгерістер аз",
                    comparativeConclusion = "Беткі күйі қалыпты",
                    confidenceAndLimits = "Визуалды тексеру",
                    statusLabel = VisionStatusOption.LOW_CHANGES.labelKz,
                    measuredAtText = "2026-11-20 09:00"
                )
            ),
            phRecords = listOf(
                ChemicalPhRecord(
                    productName = "Алма (Апорт)",
                    sampleType = "Шырын сығындысы",
                    paperColor = "Қызғылт сары",
                    scaleVisible = true,
                    estimatedPhRange = "3.5 - 4.0",
                    acidityExplanation = "Қышқыл орта",
                    confidenceLevel = "Орташа",
                    errorMarginNote = "Индикатор қағазы бойынша шамамен",
                    dataSource = DataSourceTag.PH_PAPER.labelKz,
                    measuredAtText = "2026-11-20 09:10"
                )
            ),
            storageRecords = listOf(
                StorageMonitorRecord(
                    productName = "Алма (Апорт)",
                    roomTempC = 21.0f,
                    fridgeTempC = 4.5f,
                    humidityPercent = null,
                    lightCondition = "Көлеңке",
                    storageLocation = "Тоңазытқыш",
                    storageDuration = "2 күн",
                    measuredAtText = "2026-11-20 09:20"
                )
            )
        )

        val summary = com.example.export.UnifiedReportPdfExporter.prepareSummary(data)
        assertEquals("Алма (Апорт)", summary.productLabel)
        assertEquals("Қанағат Айым • 8-сынып", summary.authorLabel)
        assertEquals(DataSourceTag.OCR.labelKz, summary.ocrSourceBadge)
        assertEquals(DataSourceTag.CAMERA.labelKz, summary.visionSourceBadge)
        assertEquals(DataSourceTag.PH_PAPER.labelKz, summary.phSourceBadge)
        assertEquals(DataSourceTag.USER.labelKz, summary.storageSourceBadge)
        assertTrue(summary.storageTempLine!!.contains("Ылғалдылық: Өлшенбеген"))
        assertEquals(1, summary.phPoints.size)
        assertEquals(3.75f, summary.phPoints.first().second, 0.01f)
        assertEquals(1, summary.tempPoints.size)
        assertEquals(4.5f, summary.tempPoints.first().second, 0.01f)
        assertEquals(3, summary.chronoRows.size)

        val emptySummary = com.example.export.UnifiedReportPdfExporter.prepareSummary(
            com.example.export.UnifiedReportPdfData(
                productName = "",
                studentAuthor = "",
                schoolProjectTitle = "",
                ocrProduct = null,
                visionRecords = emptyList(),
                phRecords = emptyList(),
                storageRecords = emptyList()
            )
        )
        assertEquals(DataSourceTag.NONE.labelKz, emptySummary.ocrSourceBadge)
        assertEquals(DataSourceTag.NONE.labelKz, emptySummary.phSourceBadge)
        assertNull(com.example.export.UnifiedReportPdfExporter.extractPhFloat("анықтау мүмкін болмады"))

        val filename = com.example.export.UnifiedReportPdfExporter.buildSuggestedFilename("Алма (Апорт)")
        assertTrue(filename.startsWith("FOODSCAN_2.0_"))
        assertTrue(filename.endsWith(".pdf"))
    }
}

