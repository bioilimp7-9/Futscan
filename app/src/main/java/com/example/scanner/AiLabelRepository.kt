package com.example.scanner

import android.net.Uri

/**
 * Unified AI label recognition interface.
 * Backed by PlaceholderAiLabelRepository when Firebase AI Logic is not enabled.
 */
interface AiLabelRepository {
    val isCloudAiConfigured: Boolean

    suspend fun analyzeLabel(
        imageUri: Uri?,
        presetId: String?,
        manualText: String?
    ): ExtractionResult
}

class PlaceholderAiLabelRepository : AiLabelRepository {
    override val isCloudAiConfigured: Boolean = false

    override suspend fun analyzeLabel(
        imageUri: Uri?,
        presetId: String?,
        manualText: String?
    ): ExtractionResult {
        if (!presetId.isNullOrBlank()) {
            return LabelAnalysisEngine.analyzePreset(presetId)
        }
        if (!manualText.isNullOrBlank()) {
            return LabelAnalysisEngine.analyzeText(manualText, "Қаптама мәтіні")
        }
        // When an image is provided without Firebase AI Logic enabled,
        // do NOT invent nutritional values; return missing-value markers for manual confirmation.
        return LabelAnalysisEngine.analyzeText("", "Фотосурет жапсырмасы").copy(
            aiModelUsed = "Қолмен тексеру режимі (Firebase AI қосылмаған)",
            isManualFallback = true
        )
    }
}
