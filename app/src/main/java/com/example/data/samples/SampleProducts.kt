package com.example.data.samples

import com.example.data.model.MeasurementBasis
import com.example.data.model.ProductItem
import com.example.data.model.SurveyResponse
import com.example.data.model.VerificationStatus

data class SampleFoodPackage(
    val id: String,
    val name: String,
    val category: String,
    val manufacturer: String,
    val ingredients: String,
    val servingSize: String,
    val basis: MeasurementBasis,
    val sugar: Float?,
    val salt: Float?,
    val fat: Float?,
    val protein: Float?,
    val calories: Float?,
    val rawLabelText: String,
    val simulatedOcrText: String,
    val iconDescription: String
)

object SampleData {
    val samplePackages = listOf(
        SampleFoodPackage(
            id = "rahat_chocolate",
            name = "«Қазақстан» шоколады",
            category = "Тәттілер мен шоколад",
            manufacturer = "«Рахат» АҚ, Алматы қ.",
            ingredients = "Қант, какао ұнтағы, какао майы, эмульгатор (соя лецитині), табиғи ваниль сығындысы. Какао өнімдері кемінде 53%.",
            servingSize = "100 г",
            basis = MeasurementBasis.PER_100G,
            sugar = 47.0f,
            salt = 0.05f,
            fat = 35.6f,
            protein = 6.3f,
            calories = 540.0f,
            rawLabelText = "100 г өнімдегі тағамдық құндылығы: ақуыздар - 6,3 г; майлар - 35,6 г; көмірсулар - 48,5 г, оның ішінде қант - 47,0 г; тұз - 0,05 г. Энергетикалық құндылығы: 2260 кДж / 540 ккал.",
            simulatedOcrText = "100g: Белки/Ақуыз 6.3g Жиры/Май 35.6g Углеводы 48.5g Сахар/Қант 47.0g Тұз/Соль 0.05g 540 kcal / ккал АО Рахат Казахстан",
            iconDescription = "Шоколад тақтасы"
        ),
        SampleFoodPackage(
            id = "foodmaster_yogurt",
            name = "«Ainaline» табиғи йогурты",
            category = "Сүт өнімдері",
            manufacturer = "«ФудМастер-Продукция» ЖШС, Есік қ.",
            ingredients = "Нормализацияланған сүт, құрғақ майсыздандырылған сүт, тірі йогурт ашытқылары (Lactobacillus bulgaricus, Streptococcus thermophilus).",
            servingSize = "100 г",
            basis = MeasurementBasis.PER_100G,
            sugar = 4.2f,
            salt = 0.10f,
            fat = 2.5f,
            protein = 3.6f,
            calories = 62.0f,
            rawLabelText = "100 г өнімнің тағамдық құндылығы: ақуыз - 3,6 г; май - 2,5 г; көмірсулар - 4,2 г (табиғи сүт қанты - лактоза); тұз - 0,1 г. Калориялылығы: 260 кДж / 62 ккал.",
            simulatedOcrText = "100 g: Ақуыз 3.6 г, Май 2.5 г, Қант 4.2 г, Тұз 0.1 г, Энергетикалық құндылығы 62 ккал. FoodMaster Ainaline",
            iconDescription = "Ақ йогурт құтысы"
        ),
        SampleFoodPackage(
            id = "lays_chips",
            name = "«Lay's» қаймақ және жуа чипсыі",
            category = "Снектер мен чипсы",
            manufacturer = "«Фрито Лей» ЖШС",
            ingredients = "Картоп, өсімдік майлары (күнбағыс, рапс), хош иістендіргіш (құрғақ сүт сарысуы, тұз, қант, қаймақ ұнтағы, жуа ұнтағы).",
            servingSize = "1 порция (30 г)",
            basis = MeasurementBasis.PER_SERVING,
            sugar = 0.9f,
            salt = 0.48f,
            fat = 9.6f,
            protein = 1.8f,
            calories = 153.0f,
            rawLabelText = "Бір порцияда (30 г): энергетикалық құндылығы 640 кДж / 153 ккал, ақуыз 1,8 г, майлар 9,6 г (қаныққан 1,1 г), көмірсулар 15 г, оның ішінде қант 0,9 г, тұз 0,48 г. (100 г үшін: май 32 г, тұз 1,6 г, калория 510 ккал).",
            simulatedOcrText = "Бір порция (30 г): 153 ккал, ақуыз 1.8 г, май 9.6 г, қант 0.9 г, тұз 0.48 г. Lay's картофельные чипсы",
            iconDescription = "Чипсы қаптамасы"
        ),
        SampleFoodPackage(
            id = "juicy_apple",
            name = "«Juicy» алма-жүзім табиғи шырыны",
            category = "Сусындар",
            manufacturer = "«RG Brands Kazakhstan» ЖШС",
            ingredients = "Концентратталған алма шырыны, концентратталған ақ жүзім шырыны. Қант қосылмаған. Табиғи жеміс қанттары бар.",
            servingSize = "100 мл",
            basis = MeasurementBasis.PER_100G,
            sugar = 11.2f,
            salt = 0.01f,
            fat = 0.0f,
            protein = 0.3f,
            calories = 46.0f,
            rawLabelText = "100 мл шырында: көмірсулар - 11,5 г, оның ішінде табиғи жеміс қанты - 11,2 г; ақуыз - 0,3 г; май - 0 г; натрий/тұз - 0,01 г. Энергетикалық құндылығы: 195 кДж / 46 ккал.",
            simulatedOcrText = "100 мл: Ақуыз 0.3 г, Май 0.0 г, Қант 11.2 г, Тұз 0.01 г, Калория 46 ккал. RG Brands Juicy",
            iconDescription = "Шырын қорабы"
        ),
        SampleFoodPackage(
            id = "amiran_milk",
            name = "«Амиран» балалар сүті 3.2%",
            category = "Сүт өнімдері",
            manufacturer = "«Амиран» зауыты, Алматы облысы",
            ingredients = "Тұтас табиғи сиыр сүті, майсыздандырылған сүт.",
            servingSize = "100 г",
            basis = MeasurementBasis.PER_100G,
            sugar = 4.7f,
            salt = 0.12f,
            fat = 3.2f,
            protein = 3.0f,
            calories = 60.0f,
            rawLabelText = "100 г өнімде: май - 3,2 г; ақуыз - 3,0 г; көмірсулар (лактоза) - 4,7 г; тұз - 0,12 г. Энергетикалық құндылығы: 250 кДж / 60 ккал.",
            simulatedOcrText = "100 г: Ақуыз 3.0 г, Май 3.2 г, Қант 4.7 г (лактоза), Тұз 0.12 г, 60 ккал. Амиран балалар тағамы",
            iconDescription = "Сүт бөтелкесі"
        ),
        SampleFoodPackage(
            id = "alpen_gold",
            name = "«Alpen Gold» сүтті шоколады",
            category = "Тәттілер мен шоколад",
            manufacturer = "«Мон'дэлис Русь» ЖШҚ",
            ingredients = "Қант, какао ұнтағы, какао майы, құрғақ сүт сарысуы, тұтас құрғақ сүт, сүт майы, эмульгаторлар.",
            servingSize = "1 порция (25 г)",
            basis = MeasurementBasis.PER_SERVING,
            sugar = 14.2f,
            salt = 0.06f,
            fat = 7.1f,
            protein = 1.4f,
            calories = 131.0f,
            rawLabelText = "Бір порцияда (25 г): Энергетикалық құндылығы: 548 кДж / 131 ккал; ақуыздар - 1,4 г; майлар - 7,1 г; көмірсулар - 15 г, оның ішінде қант - 14,2 г; тұз - 0,06 г.",
            simulatedOcrText = "1 порция (25g): 131 ккал, Белки 1.4g, Жиры 7.1g, Сахар 14.2g, Соль 0.06g. Alpen Gold",
            iconDescription = "Шоколад қаптамасы"
        )
    )

    fun initialResearchProducts(): List<ProductItem> {
        return samplePackages.take(4).mapIndexed { index, sample ->
            ProductItem(
                productNumber = index + 1,
                productName = sample.name,
                category = sample.category,
                manufacturer = sample.manufacturer,
                ingredients = sample.ingredients,
                servingSizeText = sample.servingSize,
                measurementBasis = sample.basis.name,
                sugar = sample.sugar,
                salt = sample.salt,
                fat = sample.fat,
                protein = sample.protein,
                calories = sample.calories,
                extractedSugar = sample.sugar,
                extractedSalt = sample.salt,
                extractedFat = sample.fat,
                extractedProtein = sample.protein,
                extractedCalories = sample.calories,
                samplePresetId = sample.id,
                verificationStatus = VerificationStatus.VERIFIED.name,
                notes = "Зертханалық бастапқы үлгі. Қаптама деректерімен 100% сәйкес тексерілді.",
                timestamp = System.currentTimeMillis() - (index * 3600_000L)
            )
        }
    }

    fun initialSurveyResponses(): List<SurveyResponse> {
        return listOf(
            SurveyResponse(
                respondentAge = 14,
                q1ReadsLabel = "Кейде",
                q2FindsSugar = "Қиындықпен",
                q3Knows100gVsServing = "Шамамен білемін",
                q4CanCompare = "Кейде шатасамын",
                q5HelpedUnderstand = "Өте пайдалы болды",
                comment = "FOODSCAN AI арқылы шырында қант тым көп екенін көрдім."
            ),
            SurveyResponse(
                respondentAge = 13,
                q1ReadsLabel = "Жоқ",
                q2FindsSugar = "Таба алмаймын",
                q3Knows100gVsServing = "Білмеймін",
                q4CanCompare = "Жоқ",
                q5HelpedUnderstand = "Өте пайдалы болды",
                comment = "Бұрын 100 грамм мен бір порцияны бірдей деп ойлайтынмын."
            ),
            SurveyResponse(
                respondentAge = 15,
                q1ReadsLabel = "Иә",
                q2FindsSugar = "Оңай табамын",
                q3Knows100gVsServing = "Иә, жақсы білемін",
                q4CanCompare = "Иә",
                q5HelpedUnderstand = "Біраз көмектесті",
                comment = "Салыстыру кестесі өте ыңғайлы."
            ),
            SurveyResponse(
                respondentAge = 14,
                q1ReadsLabel = "Кейде",
                q2FindsSugar = "Қиындықпен",
                q3Knows100gVsServing = "Білмеймін",
                q4CanCompare = "Кейде шатасамын",
                q5HelpedUnderstand = "Өте пайдалы болды",
                comment = "Чипсыде тұздың көптігін анық түсіндім."
            ),
            SurveyResponse(
                respondentAge = 14,
                q1ReadsLabel = "Жоқ",
                q2FindsSugar = "Таба алмаймын",
                q3Knows100gVsServing = "Білмеймін",
                q4CanCompare = "Жоқ",
                q5HelpedUnderstand = "Өте пайдалы болды",
                comment = "Диаграммалар қарапайым әрі түсінікті."
            ),
            SurveyResponse(
                respondentAge = 16,
                q1ReadsLabel = "Иә",
                q2FindsSugar = "Оңай табамын",
                q3Knows100gVsServing = "Шамамен білемін",
                q4CanCompare = "Иә",
                q5HelpedUnderstand = "Өте пайдалы болды",
                comment = "Ғылыми жоба ретінде керемет жасалған."
            ),
            SurveyResponse(
                respondentAge = 13,
                q1ReadsLabel = "Кейде",
                q2FindsSugar = "Таба алмаймын",
                q3Knows100gVsServing = "Білмеймін",
                q4CanCompare = "Кейде шатасамын",
                q5HelpedUnderstand = "Өте пайдалы болды",
                comment = "Бұдан былай дүкенде қаптаманы қарайтын боламын."
            ),
            SurveyResponse(
                respondentAge = 14,
                q1ReadsLabel = "Жоқ",
                q2FindsSugar = "Қиындықпен",
                q3Knows100gVsServing = "Шамамен білемін",
                q4CanCompare = "Жоқ",
                q5HelpedUnderstand = "Біраз көмектесті",
                comment = "Жақсы түсіндірілген."
            )
        )
    }
}
