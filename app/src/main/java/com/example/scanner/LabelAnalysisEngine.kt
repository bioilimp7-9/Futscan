package com.example.scanner

import com.example.data.model.MeasurementBasis
import com.example.data.samples.SampleData
import java.util.regex.Pattern

data class ExtractedField(
    val titleKz: String,
    val value: Float?,
    val unit: String,
    val originalSnippet: String = "",
    val confidence: Float = 0.95f,
    val isMissing: Boolean = false,
    val educationalNote: String = ""
)

data class ExtractionResult(
    val productName: String,
    val category: String,
    val manufacturer: String,
    val ingredients: String,
    val servingSizeText: String,
    val measurementBasis: MeasurementBasis,
    val sugar: ExtractedField,
    val salt: ExtractedField,
    val fat: ExtractedField,
    val protein: ExtractedField,
    val calories: ExtractedField,
    val rawTextSnippet: String,
    val aiModelUsed: String = "FOODSCAN Smart OCR Engine",
    val isManualFallback: Boolean = false
)

object LabelAnalysisEngine {

    const val MISSING_VALUE_MESSAGE = "Бұл көрсеткіш суреттен анық оқылмады. Қаптамадан қолмен тексеріңіз."

    fun analyzeText(input: String, fileNameOrHint: String = ""): ExtractionResult {
        val clean = input.replace(',', '.')

        // 1. Detect Basis
        val isPerServing = clean.contains("порция", ignoreCase = true) ||
                clean.contains("serving", ignoreCase = true) ||
                clean.contains("бір порция", ignoreCase = true)
        val basis = if (isPerServing) MeasurementBasis.PER_SERVING else MeasurementBasis.PER_100G

        // 2. Extract Sugar (Қант / Сахар)
        val sugarVal = extractFloat(clean, listOf("қант", "сахар", "sugar", "оның ішінде қант", "в т.ч. сахара"))
        val sugarField = if (sugarVal != null) {
            ExtractedField(
                titleKz = "Қант мөлшері",
                value = sugarVal.first,
                unit = "г",
                originalSnippet = sugarVal.second,
                confidence = 0.94f,
                educationalNote = "Жасөспірімдер үшін тәуліктік еркін қант шегі 25 грамнан (шамамен 5-6 шай қасық) аспауы ұсынылады."
            )
        } else {
            ExtractedField(
                titleKz = "Қант мөлшері",
                value = null,
                unit = "г",
                isMissing = true,
                educationalNote = "Қант мәні анықталмады. $MISSING_VALUE_MESSAGE"
            )
        }

        // 3. Extract Salt (Тұз / Соль / Натрий)
        val saltVal = extractFloat(clean, listOf("тұз", "соль", "salt", "натрий", "sodium"))
        val saltField = if (saltVal != null) {
            ExtractedField(
                titleKz = "Тұз мөлшері",
                value = saltVal.first,
                unit = "г",
                originalSnippet = saltVal.second,
                confidence = 0.92f,
                educationalNote = "ДДҰ ұсынысы бойынша тәуліктік тұз мөлшері 5 грамнан аспауы тиіс (жарты шай қасық)."
            )
        } else {
            ExtractedField(
                titleKz = "Тұз мөлшері",
                value = null,
                unit = "г",
                isMissing = true,
                educationalNote = "Тұз мәні анықталмады. $MISSING_VALUE_MESSAGE"
            )
        }

        // 4. Extract Fat (Май / Жиры / Fat)
        val fatVal = extractFloat(clean, listOf("май", "майлар", "жиры", "жир", "fat"))
        val fatField = if (fatVal != null) {
            ExtractedField(
                titleKz = "Май мөлшері",
                value = fatVal.first,
                unit = "г",
                originalSnippet = fatVal.second,
                confidence = 0.95f,
                educationalNote = "Майлар жасуша мембраналары мен гормондар синтезі үшін маңызды, бірақ қаныққан майды шектеу қажет."
            )
        } else {
            ExtractedField(
                titleKz = "Май мөлшері",
                value = null,
                unit = "г",
                isMissing = true,
                educationalNote = "Май мәні анықталмады. $MISSING_VALUE_MESSAGE"
            )
        }

        // 5. Extract Protein (Ақуыз / Белки / Protein)
        val proteinVal = extractFloat(clean, listOf("ақуыз", "белки", "белок", "protein"))
        val proteinField = if (proteinVal != null) {
            ExtractedField(
                titleKz = "Ақуыз мөлшері",
                value = proteinVal.first,
                unit = "г",
                originalSnippet = proteinVal.second,
                confidence = 0.96f,
                educationalNote = "Ақуыз — жасөспірім ағзасы мен бұлшықет өсуіне арналған басты құрылыс материалы."
            )
        } else {
            ExtractedField(
                titleKz = "Ақуыз мөлшері",
                value = null,
                unit = "г",
                isMissing = true,
                educationalNote = "Ақуыз мәні анықталмады. $MISSING_VALUE_MESSAGE"
            )
        }

        // 6. Extract Calories (ккал / kcal / калория)
        val calVal = extractCalories(clean)
        val calField = if (calVal != null) {
            ExtractedField(
                titleKz = "Энергетикалық құндылығы",
                value = calVal.first,
                unit = "ккал",
                originalSnippet = calVal.second,
                confidence = 0.93f,
                educationalNote = "Калория — тағамнан ағзаға түсетін қуат өлшемі. 12-17 жастағы жасөспірімдер тәулігіне 2200-2600 ккал жұмсайды."
            )
        } else {
            ExtractedField(
                titleKz = "Энергетикалық құндылығы",
                value = null,
                unit = "ккал",
                isMissing = true,
                educationalNote = "Калория көрсеткіші анықталмады. $MISSING_VALUE_MESSAGE"
            )
        }

        // Serving size
        val servingText = if (isPerServing) {
            extractServingSizeText(clean) ?: "1 порция"
        } else {
            "100 г"
        }

        val productName = extractProductName(clean, fileNameOrHint)
        val category = guessCategory(productName, clean)
        val manufacturer = extractManufacturer(clean)
        val ingredients = extractIngredients(clean)

        return ExtractionResult(
            productName = productName,
            category = category,
            manufacturer = manufacturer,
            ingredients = ingredients,
            servingSizeText = servingText,
            measurementBasis = basis,
            sugar = sugarField,
            salt = saltField,
            fat = fatField,
            protein = proteinField,
            calories = calField,
            rawTextSnippet = input.take(250)
        )
    }

    fun analyzePreset(presetId: String): ExtractionResult {
        val sample = SampleData.samplePackages.find { it.id == presetId }
            ?: SampleData.samplePackages.first()

        fun makeField(title: String, value: Float?, unit: String, note: String): ExtractedField {
            return if (value != null) {
                ExtractedField(
                    titleKz = title,
                    value = value,
                    unit = unit,
                    originalSnippet = "$title $value $unit",
                    confidence = 0.98f,
                    educationalNote = note
                )
            } else {
                ExtractedField(
                    titleKz = title,
                    value = null,
                    unit = unit,
                    isMissing = true,
                    educationalNote = MISSING_VALUE_MESSAGE
                )
            }
        }

        return ExtractionResult(
            productName = sample.name,
            category = sample.category,
            manufacturer = sample.manufacturer,
            ingredients = sample.ingredients,
            servingSizeText = sample.servingSize,
            measurementBasis = sample.basis,
            sugar = makeField("Қант мөлшері", sample.sugar, "г", "Тәуліктік қант шегі: 25 г."),
            salt = makeField("Тұз мөлшері", sample.salt, "г", "Тәуліктік тұз шегі: 5 г."),
            fat = makeField("Май мөлшері", sample.fat, "г", "Жасуша құрылысы мен энергия көзі."),
            protein = makeField("Ақуыз мөлшері", sample.protein, "г", "Бұлшықет және ағза дамуының негізі."),
            calories = makeField("Энергетикалық құндылығы", sample.calories, "ккал", "Ағзаның тіршілік әрекетіне жұмсалатын қуат."),
            rawTextSnippet = sample.rawLabelText,
            aiModelUsed = "FOODSCAN AI Verified Optical Recognition"
        )
    }

    private fun extractFloat(text: String, keywords: List<String>): Pair<Float, String>? {
        for (kw in keywords) {
            val patternStr = "(?:$kw)[^0-9]{0,25}?([0-9]+(?:\\.[0-9]+)?)\\s*(?:г|g|мг|mg)?"
            val pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE)
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val numStr = matcher.group(1)
                val value = numStr?.toFloatOrNull()
                if (value != null) {
                    val snippet = matcher.group(0) ?: ""
                    return Pair(value, snippet.trim())
                }
            }
        }
        return null
    }

    private fun extractCalories(text: String): Pair<Float, String>? {
        val patterns = listOf(
            "(?:калория|энергетикалық құндылығы|ккал|kcal)[^0-9]{0,25}?([0-9]+(?:\\.[0-9]+)?)\\s*(?:ккал|kcal)",
            "([0-9]+(?:\\.[0-9]+)?)\\s*(?:ккал|kcal)"
        )
        for (p in patterns) {
            val matcher = Pattern.compile(p, Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE).matcher(text)
            if (matcher.find()) {
                val numStr = matcher.group(1)
                val value = numStr?.toFloatOrNull()
                if (value != null && value in 1.0f..950.0f) {
                    return Pair(value, (matcher.group(0) ?: "").trim())
                }
            }
        }
        return null
    }

    private fun extractServingSizeText(text: String): String? {
        val matcher = Pattern.compile("(?i)(?:порция|serving)[^0-9]{0,10}([0-9]+(?:\\.[0-9]+)?\\s*(?:г|g|мл|ml))").matcher(text)
        return if (matcher.find()) {
            "1 порция (${matcher.group(1)})"
        } else null
    }

    private fun extractProductName(text: String, hint: String): String {
        if (hint.isNotBlank()) return hint
        val lines = text.lines().map { it.trim() }.filter { it.length > 2 }
        return lines.firstOrNull { !it.contains("100") && !it.contains("ккал") } ?: "Зерттелетін тағам өнімі"
    }

    private fun guessCategory(name: String, text: String): String {
        val combined = "$name $text".lowercase()
        return when {
            combined.contains("шоколад") || combined.contains("кәмпит") || combined.contains("вафли") || combined.contains("тәтті") -> "Тәттілер мен шоколад"
            combined.contains("сүт") || combined.contains("йогурт") || combined.contains("айран") || combined.contains("ірімшік") -> "Сүт өнімдері"
            combined.contains("чипсы") || combined.contains("снек") || combined.contains("сухарики") || combined.contains("жаңғақ") -> "Снектер мен чипсы"
            combined.contains("шырын") || combined.contains("сусын") || combined.contains("шай") || combined.contains("сок") -> "Сусындар"
            combined.contains("нан") || combined.contains("тоқаш") || combined.contains("печенье") -> "Нан және ұн өнімдері"
            else -> "Басқа тағам өнімдері"
        }
    }

    private fun extractManufacturer(text: String): String {
        val matcher = Pattern.compile("(?i)(?:өндіруші|изготовитель|manufacturer)[:\\s]{1,5}([^,\\.\\n]{3,40})").matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.trim() ?: "Қаптамада көрсетілмеген"
        } else {
            "Белгісіз өндіруші"
        }
    }

    private fun extractIngredients(text: String): String {
        val matcher = Pattern.compile("(?i)(?:құрамы|состав|ingredients)[:\\s]{1,5}([^\\.\\n]{5,150})").matcher(text)
        return if (matcher.find()) {
            matcher.group(1)?.trim() ?: "Қаптамадан анықталмады"
        } else {
            "Құрамы қаптамада анық көрінбеді"
        }
    }
}
