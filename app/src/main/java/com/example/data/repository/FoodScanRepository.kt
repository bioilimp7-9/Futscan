package com.example.data.repository

import com.example.data.local.FoodScan20Dao
import com.example.data.local.ProductDao
import com.example.data.local.StudentTestDao
import com.example.data.local.SurveyDao
import com.example.data.model.ChemicalPhRecord
import com.example.data.model.FreshnessVisionRecord
import com.example.data.model.MeasurementBasis
import com.example.data.model.ProductItem
import com.example.data.model.StorageMonitorRecord
import com.example.data.model.StudentTestAttempt
import com.example.data.model.SurveyResponse
import com.example.data.model.TestPhase
import com.example.data.model.VerificationStatus
import com.example.data.samples.ResearchProtocolData
import com.example.data.samples.SampleData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first

data class ResearchMetrics(
    val totalCount: Int,
    val maxAllowed: Int = 30,
    val verifiedCount: Int,
    val correctedCount: Int,
    val unverifiedCount: Int,
    val overallAccuracyPercentage: Float,
    val sugarAccuracy: Float,
    val saltAccuracy: Float,
    val fatAccuracy: Float,
    val proteinAccuracy: Float,
    val caloriesAccuracy: Float
)

data class TestPhaseSummary(
    val preCount: Int,
    val postCount: Int,
    val pairedStudentsCount: Int,
    val avgPreScore: Float,
    val avgPostScore: Float,
    val avgPrePercent: Float,
    val avgPostPercent: Float,
    val gainPercent: Float
)

class FoodScanRepository(
    private val productDao: ProductDao,
    private val surveyDao: SurveyDao,
    private val studentTestDao: StudentTestDao? = null,
    private val foodScan20Dao: FoodScan20Dao? = null
) {
    val allProducts: Flow<List<ProductItem>> = productDao.getAllProducts()
    val productCount: Flow<Int> = productDao.getProductCount()

    val allSurveyResponses: Flow<List<SurveyResponse>> = surveyDao.getAllResponses()
    val surveyCount: Flow<Int> = surveyDao.getResponseCount()

    val allTestAttempts: Flow<List<StudentTestAttempt>> =
        studentTestDao?.getAllAttempts() ?: emptyFlow()

    val allFreshnessRecords: Flow<List<FreshnessVisionRecord>> =
        foodScan20Dao?.getAllFreshnessRecords() ?: emptyFlow()

    val allChemicalPhRecords: Flow<List<ChemicalPhRecord>> =
        foodScan20Dao?.getAllChemicalPhRecords() ?: emptyFlow()

    val allStorageRecords: Flow<List<StorageMonitorRecord>> =
        foodScan20Dao?.getAllStorageRecords() ?: emptyFlow()

    suspend fun initializeWithDefaultsIfEmpty() {
        val currentProductCount = productDao.getAllProducts().first().size
        if (currentProductCount == 0) {
            productDao.insertAll(SampleData.initialResearchProducts())
        }

        val currentSurveyCount = surveyDao.getAllResponses().first().size
        if (currentSurveyCount == 0) {
            surveyDao.insertAll(SampleData.initialSurveyResponses())
        }
    }

    suspend fun addProduct(product: ProductItem): Boolean {
        val count = productDao.getAllProducts().first().size
        if (count >= 30) {
            return false // Maximum 30 allowed in student research scope
        }
        val nextNumber = count + 1
        productDao.insertProduct(product.copy(productNumber = nextNumber))
        return true
    }

    suspend fun updateProduct(product: ProductItem) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductItem) {
        productDao.deleteProduct(product)
        // Renumber remaining products to maintain 1..N order
        val remaining = productDao.getAllProducts().first()
        remaining.forEachIndexed { index, item ->
            if (item.productNumber != index + 1) {
                productDao.updateProduct(item.copy(productNumber = index + 1))
            }
        }
    }

    suspend fun addSurveyResponse(response: SurveyResponse): Long {
        return surveyDao.insertResponse(response)
    }

    suspend fun addTestAttempt(attempt: StudentTestAttempt): Long {
        return studentTestDao?.insertAttempt(attempt) ?: -1L
    }

    suspend fun deleteTestAttempt(attempt: StudentTestAttempt) {
        studentTestDao?.deleteAttempt(attempt)
    }

    suspend fun addFreshnessRecord(record: FreshnessVisionRecord): Long {
        return foodScan20Dao?.insertFreshnessRecord(record) ?: -1L
    }

    suspend fun deleteFreshnessRecord(record: FreshnessVisionRecord) {
        foodScan20Dao?.deleteFreshnessRecord(record)
    }

    suspend fun addChemicalPhRecord(record: ChemicalPhRecord): Long {
        return foodScan20Dao?.insertChemicalPhRecord(record) ?: -1L
    }

    suspend fun deleteChemicalPhRecord(record: ChemicalPhRecord) {
        foodScan20Dao?.deleteChemicalPhRecord(record)
    }

    suspend fun addStorageRecord(record: StorageMonitorRecord): Long {
        return foodScan20Dao?.insertStorageRecord(record) ?: -1L
    }

    suspend fun deleteStorageRecord(record: StorageMonitorRecord) {
        foodScan20Dao?.deleteStorageRecord(record)
    }

    fun summarizeTestAttempts(attempts: List<StudentTestAttempt>): TestPhaseSummary {
        val pre = attempts.filter { it.phase == TestPhase.PRE.name }
        val post = attempts.filter { it.phase == TestPhase.POST.name }

        val preByStudent = pre.groupBy { it.studentName.trim().lowercase() }
        val postByStudent = post.groupBy { it.studentName.trim().lowercase() }
        val pairedCount = preByStudent.keys.count { it.isNotBlank() && postByStudent.containsKey(it) }

        val avgPreScore = if (pre.isNotEmpty()) pre.map { it.score.toFloat() }.average().toFloat() else 0f
        val avgPostScore = if (post.isNotEmpty()) post.map { it.score.toFloat() }.average().toFloat() else 0f
        val totalQ = ResearchProtocolData.eightQuestions.size.toFloat().coerceAtLeast(1f)
        val avgPrePct = if (pre.isNotEmpty()) (avgPreScore / totalQ) * 100f else 0f
        val avgPostPct = if (post.isNotEmpty()) (avgPostScore / totalQ) * 100f else 0f
        val gain = if (pre.isNotEmpty() && post.isNotEmpty()) avgPostPct - avgPrePct else 0f

        return TestPhaseSummary(
            preCount = pre.size,
            postCount = post.size,
            pairedStudentsCount = pairedCount,
            avgPreScore = avgPreScore,
            avgPostScore = avgPostScore,
            avgPrePercent = avgPrePct,
            avgPostPercent = avgPostPct,
            gainPercent = gain
        )
    }

    suspend fun calculateAccuracy(): ResearchMetrics {
        val products = productDao.getAllProducts().first()
        val total = products.size
        var verified = 0
        var corrected = 0
        var unverified = 0

        var checkedAttributes = 0
        var matchingAttributes = 0

        var sugarChecked = 0
        var sugarMatches = 0
        var saltChecked = 0
        var saltMatches = 0
        var fatChecked = 0
        var fatMatches = 0
        var proteinChecked = 0
        var proteinMatches = 0
        var calChecked = 0
        var calMatches = 0

        fun isClose(a: Float?, b: Float?): Boolean {
            if (a == null && b == null) return true
            if (a == null || b == null) return false
            return kotlin.math.abs(a - b) <= 0.15f
        }

        for (p in products) {
            when (p.verificationStatus) {
                VerificationStatus.VERIFIED.name -> verified++
                VerificationStatus.CORRECTED.name -> corrected++
                else -> unverified++
            }

            // Only compute OCR extraction accuracy if student has actively verified or corrected
            if (p.verificationStatus != VerificationStatus.UNVERIFIED.name) {
                if (p.extractedSugar != null && p.sugar != null) {
                    checkedAttributes++
                    sugarChecked++
                    if (isClose(p.extractedSugar, p.sugar)) {
                        matchingAttributes++
                        sugarMatches++
                    }
                }
                if (p.extractedSalt != null && p.salt != null) {
                    checkedAttributes++
                    saltChecked++
                    if (isClose(p.extractedSalt, p.salt)) {
                        matchingAttributes++
                        saltMatches++
                    }
                }
                if (p.extractedFat != null && p.fat != null) {
                    checkedAttributes++
                    fatChecked++
                    if (isClose(p.extractedFat, p.fat)) {
                        matchingAttributes++
                        fatMatches++
                    }
                }
                if (p.extractedProtein != null && p.protein != null) {
                    checkedAttributes++
                    proteinChecked++
                    if (isClose(p.extractedProtein, p.protein)) {
                        matchingAttributes++
                        proteinMatches++
                    }
                }
                if (p.extractedCalories != null && p.calories != null) {
                    checkedAttributes++
                    calChecked++
                    if (isClose(p.extractedCalories, p.calories)) {
                        matchingAttributes++
                        calMatches++
                    }
                }
            }
        }

        fun pct(num: Int, denom: Int): Float {
            return if (denom > 0) (num.toFloat() / denom.toFloat()) * 100f else 100f
        }

        val overall = pct(matchingAttributes, checkedAttributes)

        return ResearchMetrics(
            totalCount = total,
            maxAllowed = 30,
            verifiedCount = verified,
            correctedCount = corrected,
            unverifiedCount = unverified,
            overallAccuracyPercentage = overall,
            sugarAccuracy = pct(sugarMatches, sugarChecked),
            saltAccuracy = pct(saltMatches, saltChecked),
            fatAccuracy = pct(fatMatches, fatChecked),
            proteinAccuracy = pct(proteinMatches, proteinChecked),
            caloriesAccuracy = pct(calMatches, calChecked)
        )
    }

    suspend fun exportProductsToCsv(): String {
        val products = productDao.getAllProducts().first()
        val sb = StringBuilder()
        // UTF-8 BOM so Excel opens Kazakh / Cyrillic characters correctly
        sb.append('\uFEFF')
        sb.append("№;Өнім атауы;Санаты;Өндіруші;Өлшем негізі;Қант (г);Тұз (г);Май (г);Ақуыз (г);Калория (ккал);AI Қант;AI Тұз;AI Май;AI Ақуыз;AI Калория;Тексеру күйі;Ескертпелер\n")

        for (p in products) {
            val basisText = if (p.measurementBasis == MeasurementBasis.PER_SERVING.name) "Порция" else "100 г"
            val statusText = when (p.verificationStatus) {
                VerificationStatus.VERIFIED.name -> "Тексерілді"
                VerificationStatus.CORRECTED.name -> "Түзетілді"
                else -> "Тексерілмеген"
            }
            sb.append("${p.productNumber};")
            sb.append("\"${p.productName.replace("\"", "\"\"")}\";")
            sb.append("\"${p.category}\";")
            sb.append("\"${p.manufacturer.replace("\"", "\"\"")}\";")
            sb.append("\"$basisText (${p.servingSizeText})\";")
            sb.append("${p.sugar ?: ""};")
            sb.append("${p.salt ?: ""};")
            sb.append("${p.fat ?: ""};")
            sb.append("${p.protein ?: ""};")
            sb.append("${p.calories ?: ""};")
            sb.append("${p.extractedSugar ?: ""};")
            sb.append("${p.extractedSalt ?: ""};")
            sb.append("${p.extractedFat ?: ""};")
            sb.append("${p.extractedProtein ?: ""};")
            sb.append("${p.extractedCalories ?: ""};")
            sb.append("\"$statusText\";")
            sb.append("\"${p.notes.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }

    suspend fun exportProtocolSummaryCsv(): String {
        val metrics = calculateAccuracy()
        val attempts = allTestAttempts.first()
        val testSummary = summarizeTestAttempts(attempts)

        val sb = StringBuilder()
        sb.append('\uFEFF')
        sb.append("FOODSCAN AI — 6 парақты зерттеу хаттамасының қорытындысы\n")
        sb.append("Өнімдер саны;${metrics.totalCount} / ${metrics.maxAllowed}\n")
        sb.append("Тексерілді;${metrics.verifiedCount}\n")
        sb.append("Түзетілді;${metrics.correctedCount}\n")
        sb.append("Тексерілмеген;${metrics.unverifiedCount}\n")
        sb.append("Жалпы OCR дәлдігі (%);${String.format(java.util.Locale.US, "%.1f", metrics.overallAccuracyPercentage)}\n")
        sb.append("Қант OCR дәлдігі (%);${String.format(java.util.Locale.US, "%.1f", metrics.sugarAccuracy)}\n")
        sb.append("Тұз OCR дәлдігі (%);${String.format(java.util.Locale.US, "%.1f", metrics.saltAccuracy)}\n")
        sb.append("Май OCR дәлдігі (%);${String.format(java.util.Locale.US, "%.1f", metrics.fatAccuracy)}\n")
        sb.append("Ақуыз OCR дәлдігі (%);${String.format(java.util.Locale.US, "%.1f", metrics.proteinAccuracy)}\n")
        sb.append("Калория OCR дәлдігі (%);${String.format(java.util.Locale.US, "%.1f", metrics.caloriesAccuracy)}\n\n")
        sb.append("Оқушы тесті (Дейін / Кейін)\n")
        sb.append("Пайдалануға дейінгі тест саны;${testSummary.preCount}\n")
        sb.append("Пайдаланудан кейінгі тест саны;${testSummary.postCount}\n")
        sb.append("Жұпталған оқушылар;${testSummary.pairedStudentsCount}\n")
        sb.append("Орташа балл (Дейін);${String.format(java.util.Locale.US, "%.2f", testSummary.avgPreScore)} / 8 (${String.format(java.util.Locale.US, "%.1f", testSummary.avgPrePercent)}%)\n")
        sb.append("Орташа балл (Кейін);${String.format(java.util.Locale.US, "%.2f", testSummary.avgPostScore)} / 8 (${String.format(java.util.Locale.US, "%.1f", testSummary.avgPostPercent)}%)\n")
        sb.append("Білім өсімі (Δ%);${String.format(java.util.Locale.US, "%+.1f", testSummary.gainPercent)}%\n\n")
        if (attempts.isNotEmpty()) {
            sb.append("Оқушы;Жасы;Сыныбы;Кезең;Балл;Жалпы сұрақ;Жауаптар\n")
            for (a in attempts) {
                val phaseKz = TestPhase.fromString(a.phase).badgeKz
                sb.append("\"${a.studentName.replace("\"", "\"\"")}\";${a.studentAge};\"${a.studentGrade}\";\"$phaseKz\";${a.score};${a.totalQuestions};\"${a.answersCsv}\"\n")
            }
        }
        return sb.toString()
    }
}
