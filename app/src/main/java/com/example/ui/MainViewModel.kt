package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FoodScanDatabase
import com.example.data.model.ChemicalPhRecord
import com.example.data.model.DisconnectedEsp32SensorProvider
import com.example.data.model.FreshnessVisionRecord
import com.example.data.model.MeasurementBasis
import com.example.data.model.ProductItem
import com.example.data.model.StorageMonitorRecord
import com.example.data.model.StorageSensorProvider
import com.example.data.model.StudentTestAttempt
import com.example.data.model.SurveyResponse
import com.example.data.model.TestPhase
import com.example.data.model.VerificationStatus
import com.example.data.repository.FoodScanRepository
import com.example.data.repository.ResearchMetrics
import com.example.data.repository.TestPhaseSummary
import com.example.data.samples.ResearchProtocolData
import com.example.data.samples.SampleData
import com.example.scanner.AiLabelRepository
import com.example.scanner.ExtractionResult
import com.example.scanner.LabelAnalysisEngine
import com.example.scanner.PlaceholderAiLabelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    DASHBOARD,
    SCANNER,
    FRESHNESS_VISION,
    CHEMICAL_PH,
    STORAGE_MONITOR,
    UNIFIED_REPORT,
    COMPARISON,
    RESEARCH,
    RESEARCH_PROTOCOL,
    SURVEY,
    EDUCATION,
    ABOUT_RESEARCH,
    PWA_GUIDE
}

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data object Scanning : ScanUiState
    data class ResultReady(val result: ExtractionResult) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FoodScanRepository
    private val aiRepository: AiLabelRepository = PlaceholderAiLabelRepository()
    val isCloudAiConfigured: Boolean = aiRepository.isCloudAiConfigured

    val sensorProvider: StorageSensorProvider = DisconnectedEsp32SensorProvider()

    init {
        val database = FoodScanDatabase.getDatabase(application)
        repository = FoodScanRepository(
            productDao = database.productDao(),
            surveyDao = database.surveyDao(),
            studentTestDao = database.studentTestDao(),
            foodScan20Dao = database.foodScan20Dao()
        )
        viewModelScope.launch {
            repository.initializeWithDefaultsIfEmpty()
            refreshMetrics()
        }
    }

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Navigation back stack support
    private val screenHistory = mutableListOf<AppScreen>()

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenHistory.isNotEmpty()) {
            val prev = screenHistory.removeAt(screenHistory.lastIndex)
            _currentScreen.value = prev
            return true
        } else if (_currentScreen.value != AppScreen.DASHBOARD) {
            _currentScreen.value = AppScreen.DASHBOARD
            return true
        }
        return false
    }

    // Session stats
    private val _sessionScanCount = MutableStateFlow(0)
    val sessionScanCount: StateFlow<Int> = _sessionScanCount.asStateFlow()

    // Products from Room
    val productsList: StateFlow<List<ProductItem>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Survey responses from Room
    val surveyList: StateFlow<List<SurveyResponse>> = repository.allSurveyResponses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Student 8-question Pre/Post Test Attempts from Room
    val testAttemptsList: StateFlow<List<StudentTestAttempt>> = repository.allTestAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // FOODSCAN AI 2.0 Flows (Freshness Vision, Chemical pH, Storage Monitor)
    val freshnessRecords: StateFlow<List<FreshnessVisionRecord>> = repository.allFreshnessRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chemicalPhRecords: StateFlow<List<ChemicalPhRecord>> = repository.allChemicalPhRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val storageRecords: StateFlow<List<StorageMonitorRecord>> = repository.allStorageRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedUnifiedProductName = MutableStateFlow("")
    val selectedUnifiedProductName: StateFlow<String> = _selectedUnifiedProductName.asStateFlow()

    fun selectUnifiedProduct(name: String) {
        _selectedUnifiedProductName.value = name
    }

    // Research metrics
    private val _metrics = MutableStateFlow<ResearchMetrics?>(null)
    val metrics: StateFlow<ResearchMetrics?> = _metrics.asStateFlow()

    fun refreshMetrics() {
        viewModelScope.launch {
            _metrics.value = repository.calculateAccuracy()
        }
    }

    // Scanner state
    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _selectedPresetId = MutableStateFlow<String?>(null)
    val selectedPresetId: StateFlow<String?> = _selectedPresetId.asStateFlow()

    fun setImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
        _selectedPresetId.value = null
        if (uri == null) {
            _scanState.value = ScanUiState.Idle
        }
    }

    fun selectPreset(presetId: String) {
        _selectedPresetId.value = presetId
        _selectedImageUri.value = null
        startAnalysis()
    }

    fun clearImage() {
        _selectedImageUri.value = null
        _selectedPresetId.value = null
        _scanState.value = ScanUiState.Idle
    }

    fun startAnalysis(customOcrText: String? = null) {
        viewModelScope.launch {
            _scanState.value = ScanUiState.Scanning
            kotlinx.coroutines.delay(1000L) // Realistic neural OCR processing

            val preset = _selectedPresetId.value
            val result = aiRepository.analyzeLabel(
                imageUri = _selectedImageUri.value,
                presetId = preset,
                manualText = customOcrText
            )

            _scanState.value = ScanUiState.ResultReady(result)
            _sessionScanCount.value += 1
        }
    }

    // Save analyzed product to research database
    fun saveAnalyzedToResearch(
        name: String,
        category: String,
        basis: MeasurementBasis,
        sugar: Float?,
        salt: Float?,
        fat: Float?,
        protein: Float?,
        calories: Float?,
        extractedSugar: Float?,
        extractedSalt: Float?,
        extractedFat: Float?,
        extractedProtein: Float?,
        extractedCalories: Float?,
        notes: String,
        onResult: ((Boolean, ProductItem) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val isMatch = (sugar == extractedSugar && salt == extractedSalt && fat == extractedFat && protein == extractedProtein && calories == extractedCalories)
            val status = if (isMatch) VerificationStatus.VERIFIED.name else VerificationStatus.CORRECTED.name

            val product = ProductItem(
                productName = name.ifBlank { "Талданған өнім" },
                category = category.ifBlank { "Басқа өнімдер" },
                measurementBasis = basis.name,
                sugar = sugar,
                salt = salt,
                fat = fat,
                protein = protein,
                calories = calories,
                extractedSugar = extractedSugar,
                extractedSalt = extractedSalt,
                extractedFat = extractedFat,
                extractedProtein = extractedProtein,
                extractedCalories = extractedCalories,
                verificationStatus = status,
                notes = notes,
                imageUri = _selectedImageUri.value?.toString(),
                samplePresetId = _selectedPresetId.value
            )

            val success = repository.addProduct(product)
            if (success) {
                _comparisonProduct1.value = product
            }
            refreshMetrics()
            onResult?.invoke(success, product)
        }
    }

    fun deleteProduct(product: ProductItem) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            refreshMetrics()
        }
    }

    fun updateProductVerification(product: ProductItem, status: VerificationStatus, actualSugar: Float?, actualSalt: Float?, actualFat: Float?, actualProtein: Float?, actualCal: Float?) {
        viewModelScope.launch {
            val updated = product.copy(
                verificationStatus = status.name,
                sugar = actualSugar,
                salt = actualSalt,
                fat = actualFat,
                protein = actualProtein,
                calories = actualCal
            )
            repository.updateProduct(updated)
            refreshMetrics()
        }
    }

    suspend fun exportCsv(): String {
        return repository.exportProductsToCsv()
    }

    // Product Comparison Selection
    private val _comparisonProduct1 = MutableStateFlow<ProductItem?>(null)
    val comparisonProduct1: StateFlow<ProductItem?> = _comparisonProduct1.asStateFlow()

    private val _comparisonProduct2 = MutableStateFlow<ProductItem?>(null)
    val comparisonProduct2: StateFlow<ProductItem?> = _comparisonProduct2.asStateFlow()

    fun selectForComparison1(product: ProductItem) {
        _comparisonProduct1.value = product
    }

    fun selectForComparison2(product: ProductItem) {
        _comparisonProduct2.value = product
    }

    // Submit Survey
    fun submitSurvey(
        age: Int,
        q1: String,
        q2: String,
        q3: String,
        q4: String,
        q5: String,
        comment: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val response = SurveyResponse(
                respondentAge = age,
                q1ReadsLabel = q1,
                q2FindsSugar = q2,
                q3Knows100gVsServing = q3,
                q4CanCompare = q4,
                q5HelpedUnderstand = q5,
                comment = comment
            )
            repository.addSurveyResponse(response)
            onDone()
        }
    }

    fun summarizeTestAttempts(attempts: List<StudentTestAttempt>): TestPhaseSummary {
        return repository.summarizeTestAttempts(attempts)
    }

    fun submitKnowledgeTest(
        studentName: String,
        studentAge: Int,
        studentGrade: String,
        phase: TestPhase,
        selectedIndices: List<Int>,
        onDone: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val questions = ResearchProtocolData.eightQuestions
            val score = questions.indices.count { i ->
                selectedIndices.getOrNull(i) == questions[i].correctOptionIndex
            }
            val letters = listOf("A", "B", "C", "D")
            val csvAnswers = selectedIndices.joinToString(",") { idx ->
                letters.getOrElse(idx) { "—" }
            }
            val attempt = StudentTestAttempt(
                studentName = studentName.trim().ifBlank { "Оқушы" },
                studentAge = studentAge.coerceIn(10, 18),
                studentGrade = studentGrade.trim().ifBlank { "8 сынып" },
                phase = phase.name,
                answersCsv = csvAnswers,
                score = score,
                totalQuestions = questions.size
            )
            repository.addTestAttempt(attempt)
            onDone(score)
        }
    }

    fun deleteTestAttempt(attempt: StudentTestAttempt) {
        viewModelScope.launch {
            repository.deleteTestAttempt(attempt)
        }
    }

    suspend fun exportProtocolSummaryCsv(): String {
        return repository.exportProtocolSummaryCsv()
    }

    fun saveFreshnessRecord(record: FreshnessVisionRecord, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addFreshnessRecord(record)
            _selectedUnifiedProductName.value = record.productName
            onDone()
        }
    }

    fun deleteFreshnessRecord(record: FreshnessVisionRecord) {
        viewModelScope.launch {
            repository.deleteFreshnessRecord(record)
        }
    }

    fun saveChemicalPhRecord(record: ChemicalPhRecord, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addChemicalPhRecord(record)
            _selectedUnifiedProductName.value = record.productName
            onDone()
        }
    }

    fun deleteChemicalPhRecord(record: ChemicalPhRecord) {
        viewModelScope.launch {
            repository.deleteChemicalPhRecord(record)
        }
    }

    fun saveStorageRecord(record: StorageMonitorRecord, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addStorageRecord(record)
            _selectedUnifiedProductName.value = record.productName
            onDone()
        }
    }

    fun deleteStorageRecord(record: StorageMonitorRecord) {
        viewModelScope.launch {
            repository.deleteStorageRecord(record)
        }
    }
}
