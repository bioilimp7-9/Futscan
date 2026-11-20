package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.FreshGreenMuted
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun AboutResearchScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("about_research_screen")
    ) {
        // HERO HEADER
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = NavyDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(NavyDark, NavyPrimary))
                    )
                    .padding(20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = FreshGreen
                ) {
                    Text(
                        text = "8-СЫНЫП ОҚУШЫСЫНЫҢ ЗЕРТТЕУ ЖҰМЫСЫ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "«Жасөспірімдерге арналған тағам өнімдерінің құрамын цифрлық талдау жүйесін әзірлеу және оның ақпараттық тиімділігін зерттеу»",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Автор: Қанағат Айым, 8 «А» сынып оқушысы",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = FreshGreenMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // PASSPORT CARDS
        ResearchSectionCard(
            title = "1. Жұмыстың өзектілігі",
            content = "Қазіргі таңда жасөспірімдер арасында фастфуд, қантты газдалған сусындар мен тұзы жоғары снектерді тұтыну көлемі өсуде. Жасөспірімдер тағам қаптамасындағы кіші шрифтпен жазылған тағамдық құндылық кестесін сирек оқиды немесе 100 грамм мен бір порция айырмашылығын түсінбей шатасады. Осы мәселені шешу үшін цифрлық оптикалық талдау жүйесі (FOODSCAN AI) жасалды."
        )

        Spacer(modifier = Modifier.height(10.dp))

        ResearchSectionCard(
            title = "2. Зерттеу мақсаты",
            content = "Жасөспірімдерге арналған тағам өнімдерінің жапсырмасын фото арқылы семантикалық талдап, қант, тұз, май, ақуыз және калория көрсеткіштерін визуалды түрде түсіндіретін цифрлық жүйе әзірлеу және оның оқушылардың ақпараттық сауаттылығына тигізетін әсерін тәжірибе жүзінде зерттеу."
        )

        Spacer(modifier = Modifier.height(10.dp))

        ResearchSectionCard(
            title = "3. Ғылыми гипотеза",
            content = "Егер жасөспірімдерге тағам қаптамасын цифрлық оптикалық талдау (OCR) және салыстыру құралы ұсынылса, олардың өнім құрамындағы қант пен тұздың нақты мөлшерін саналы түрде бағалау қабілеті артып, зиянды қоспаларды шектеу дағдысы қалыптасады."
        )

        Spacer(modifier = Modifier.height(10.dp))

        ResearchSectionCard(
            title = "4. Зерттеу әдістері мен зертхана көлемі",
            content = "• Салыстырмалы және статистикалық талдау әдісі.\n• Оптикалық таңба тану (OCR) және семантикалық парсинг алгоритмдері.\n• Дүкен сөрелеріндегі 30 түрлі тағам өнімінің жапсырмасын нақты тексеру (қант, тұз, май, ақуыз, калория).\n• 12–17 жастағы мектеп оқушылары арасында жүргізілген 5 сұрақты әлеуметтік сауалнама."
        )

        Spacer(modifier = Modifier.height(10.dp))

        ResearchSectionCard(
            title = "5. Ғылыми жаңалығы мен практикалық маңызы",
            content = "• Қазақ тіліндегі тағамдық құндылық жапсырмаларын арнайы танитын алгоритмдік модель құрылды.\n• Бір порция мен 100 г көрсеткіштерін қате салыстырудың алдын алатын бірыңғайлау нормализаторы енгізілді.\n• Жоба ұлттық ғылыми жарыстар мен мектеп олимпиадаларында тәжірибелік зерттеу үлгісі ретінде пайдалануға арналған."
        )

        Spacer(modifier = Modifier.height(14.dp))

        MedicalDisclaimerCard()
    }
}

@Composable
private fun ResearchSectionCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                fontSize = 12.sp,
                color = Color(0xFF334155),
                lineHeight = 17.sp
            )
        }
    }
}
