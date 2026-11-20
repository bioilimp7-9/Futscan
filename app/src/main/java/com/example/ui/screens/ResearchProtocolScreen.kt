package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductItem
import com.example.data.model.StudentTestAttempt
import com.example.data.model.VerificationStatus
import com.example.data.repository.ResearchMetrics
import com.example.data.samples.ResearchProtocolData
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.FreshGreenDark
import com.example.ui.theme.FreshGreenMuted
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ResearchProtocolScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.productsList.collectAsState()
    val metrics by viewModel.metrics.collectAsState()
    val testAttempts by viewModel.testAttemptsList.collectAsState()

    var selectedSheet by remember { mutableIntStateOf(0) }
    val sheetTitles = listOf(
        "1. Нұсқаулық",
        "2. 30 өнім",
        "3. Оқушы тесті",
        "4. Тест сұрақтары",
        "5. Жауап кілті",
        "6. Қорытынды"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("research_protocol_screen")
    ) {
        // Header banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(NavyDark, NavyPrimary)))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "ҒЫЛЫМИ ЗЕРТТЕУ ХАТТАМАСЫ (6 ПАРАҚ)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = FreshGreenMuted,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "Деректер жинау, 30 өнім, Pre/Post тест және қорытынды",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // 6-sheet ScrollableTabRow
        ScrollableTabRow(
            selectedTabIndex = selectedSheet,
            containerColor = Color.White,
            contentColor = NavyPrimary,
            edgePadding = 8.dp,
            modifier = Modifier.testTag("protocol_sheet_tabs")
        ) {
            sheetTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSheet == index,
                    onClick = { selectedSheet = index },
                    modifier = Modifier.testTag("protocol_tab_$index"),
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedSheet == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedSheet == index) FreshGreenDark else Color(0xFF64748B)
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (selectedSheet) {
                0 -> InstructionsSheet(onStartTest = { selectedSheet = 3 }, onOpenScanner = { viewModel.navigateTo(AppScreen.SCANNER) })
                1 -> ProductsComparisonSheet(products = products, onAddMore = { viewModel.navigateTo(AppScreen.SCANNER) })
                2 -> StudentPrePostSheet(
                    attempts = testAttempts,
                    onTakeTest = { selectedSheet = 3 },
                    onDeleteAttempt = { viewModel.deleteTestAttempt(it) }
                )
                3 -> EightQuestionTestSheet(
                    onSubmit = { name, age, grade, phase, selected ->
                        viewModel.submitKnowledgeTest(name, age, grade, phase, selected) {
                            selectedSheet = 2
                        }
                    }
                )
                4 -> AnswerKeySheet()
                5 -> SummarySheet(
                    viewModel = viewModel,
                    products = products,
                    metrics = metrics,
                    attempts = testAttempts
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            MedicalDisclaimerCard()
        }
    }
}

@Composable
private fun InstructionsSheet(
    onStartTest: () -> Unit,
    onOpenScanner: () -> Unit
) {
    Text(
        text = "1-ПАРАҚ: НҰСҚАУЛЫҚ — ДЕРЕКТЕРДІ ЖИНАУ ТӘРТІБІ",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = FreshGreenDark
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "Тәжірибелік зерттеуді жүргізу реті",
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        color = NavyDark
    )
    Spacer(modifier = Modifier.height(10.dp))

    ResearchProtocolData.instructionSteps.forEach { step ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(NavyPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${step.stepNumber}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = step.sheetTitle.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = FreshGreenDark
                    )
                    Text(
                        text = step.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = step.description,
                        fontSize = 12.sp,
                        color = Color(0xFF475569),
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(6.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onOpenScanner,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("protocol_go_scanner_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Өнім сканерлеу", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onStartTest,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("protocol_go_test_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("8-сұрақ тестін өту", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProductsComparisonSheet(
    products: List<ProductItem>,
    onAddMore: () -> Unit
) {
    Text(
        text = "2-ПАРАҚ: 30 ӨНІМ — ҚАПТАМА МЕН AI МӘНДЕРІН САЛЫСТЫРУ",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = FreshGreenDark
    )
    Spacer(modifier = Modifier.height(6.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Жинақталған: ${products.size} / 30",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NavyDark
        )
        OutlinedButton(
            onClick = onAddMore,
            modifier = Modifier.height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Қосу", fontSize = 12.sp)
        }
    }
    Spacer(modifier = Modifier.height(10.dp))

    products.forEach { p ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "#${p.productNumber} ${p.productName}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        Text(
                            text = "${p.category} • ${p.servingSizeText}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    val (bg, txt, fg) = when (p.verificationStatus) {
                        VerificationStatus.VERIFIED.name -> Triple(Color(0xFFD1FAE5), "Сәйкес", FreshGreenDark)
                        VerificationStatus.CORRECTED.name -> Triple(Color(0xFFFEF3C7), "Түзетілді", AccentAmber)
                        else -> Triple(Color(0xFFF1F5F9), "Күтуде", Color(0xFF64748B))
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
                        Text(
                            text = txt,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = fg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text("Көрсеткіш", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDark, modifier = Modifier.weight(1.2f))
                    Text("Қаптама", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDark, modifier = Modifier.weight(1f))
                    Text("AI оқуы", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDark, modifier = Modifier.weight(1f))
                    Text("Δ Айырма", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyDark, modifier = Modifier.weight(1f))
                }
                NutrientCompareRow("Қант (г)", p.sugar, p.extractedSugar)
                NutrientCompareRow("Тұз (г)", p.salt, p.extractedSalt)
                NutrientCompareRow("Май (г)", p.fat, p.extractedFat)
                NutrientCompareRow("Ақуыз (г)", p.protein, p.extractedProtein)
                NutrientCompareRow("Ккал", p.calories, p.extractedCalories)
            }
        }
    }
}

@Composable
private fun NutrientCompareRow(label: String, actual: Float?, ai: Float?) {
    val diffText = if (actual != null && ai != null) {
        val diff = ai - actual
        if (kotlin.math.abs(diff) < 0.05f) "0.0" else String.format(Locale.US, "%+.2f", diff)
    } else "—"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.weight(1.2f))
        Text(actual?.let { String.format(Locale.US, "%.1f", it) } ?: "—", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NavyDark, modifier = Modifier.weight(1f))
        Text(ai?.let { String.format(Locale.US, "%.1f", it) } ?: "—", fontSize = 11.sp, color = AccentCyan, modifier = Modifier.weight(1f))
        Text(
            text = diffText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (diffText == "0.0") FreshGreenDark else AccentAmber,
            modifier = Modifier.weight(1f)
        )
    }
    HorizontalDivider(color = Color(0xFFF1F5F9))
}

@Composable
private fun SummarySheet(
    viewModel: MainViewModel,
    products: List<ProductItem>,
    metrics: ResearchMetrics?,
    attempts: List<StudentTestAttempt>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    var showCsvDialog by remember { mutableStateOf(false) }
    var csvText by remember { mutableStateOf("") }

    val testSummary = remember(attempts) { viewModel.summarizeTestAttempts(attempts) }

    Text(
        text = "6-ПАРАҚ: ҚОРЫТЫНДЫ — АВТОМАТТЫ ЕСЕПТЕУ",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = FreshGreenDark
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "OCR дәлдігі мен тест нәтижелері",
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        color = NavyDark
    )
    Spacer(modifier = Modifier.height(10.dp))

    // A. OCR Summary Card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("protocol_summary_ocr_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "А. ЖҮЙЕНІҢ OCR ДӘЛДІГІ (${products.size} / 30 ӨНІМ)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = FreshGreenMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = String.format(Locale.US, "%.1f%%", metrics?.overallAccuracyPercentage ?: 100f),
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Тексерілді: ${metrics?.verifiedCount ?: 0} • Түзетілді: ${metrics?.correctedCount ?: 0} • Күтуде: ${metrics?.unverifiedCount ?: 0}",
                fontSize = 11.sp,
                color = Color(0xFFCBD5E1)
            )
            Spacer(modifier = Modifier.height(12.dp))
            AccuracyProgressRow("Қант дәлдігі", metrics?.sugarAccuracy ?: 100f)
            AccuracyProgressRow("Тұз дәлдігі", metrics?.saltAccuracy ?: 100f)
            AccuracyProgressRow("Май дәлдігі", metrics?.fatAccuracy ?: 100f)
            AccuracyProgressRow("Ақуыз дәлдігі", metrics?.proteinAccuracy ?: 100f)
            AccuracyProgressRow("Калория дәлдігі", metrics?.caloriesAccuracy ?: 100f)
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // B. Pre/Post Knowledge Test Summary Card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("protocol_summary_test_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ә. ОҚУШЫ ТЕСТІНІҢ АҚПАРАТТЫҚ ТИІМДІЛІГІ (8 ТАПСЫРМА)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AccentCyan
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryStatTile(
                    title = "Дейін (Pre)",
                    value = if (testSummary.preCount > 0) String.format(Locale.US, "%.1f / 8", testSummary.avgPreScore) else "—",
                    sub = "${testSummary.preCount} тест (${String.format(Locale.US, "%.0f%%", testSummary.avgPrePercent)})",
                    color = AccentAmber,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatTile(
                    title = "Кейін (Post)",
                    value = if (testSummary.postCount > 0) String.format(Locale.US, "%.1f / 8", testSummary.avgPostScore) else "—",
                    sub = "${testSummary.postCount} тест (${String.format(Locale.US, "%.0f%%", testSummary.avgPostPercent)})",
                    color = FreshGreen,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatTile(
                    title = "Өсім (Δ)",
                    value = if (testSummary.preCount > 0 && testSummary.postCount > 0) {
                        String.format(Locale.US, "%+.1f%%", testSummary.gainPercent)
                    } else "—",
                    sub = "Жұп: ${testSummary.pairedStudentsCount} оқушы",
                    color = NavyPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            if (attempts.isEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Қорытынды есептелуі үшін «4. Тест сұрақтары» парағында кемінде 1 «Дейін» және 1 «Кейін» тестін тапсырыңыз.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = {
            scope.launch {
                csvText = viewModel.exportProtocolSummaryCsv()
                showCsvDialog = true
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("export_protocol_csv_button"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
    ) {
        Icon(Icons.Default.FileDownload, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("6 парақ қорытындысын CSV экспорттау", fontWeight = FontWeight.Bold)
    }

    if (showCsvDialog) {
        AlertDialog(
            onDismissRequest = { showCsvDialog = false },
            title = { Text("Хаттама қорытындысы (CSV)", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = csvText,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = NavyDark
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, csvText)
                            type = "text/csv"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Қорытынды CSV бөлісу"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Бөлісу")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(csvText))
                    Toast.makeText(context, "Көшірілді!", Toast.LENGTH_SHORT).show()
                    showCsvDialog = false
                }) {
                    Text("Көшіру")
                }
            }
        )
    }
}

@Composable
private fun AccuracyProgressRow(label: String, pct: Float) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 11.sp, color = Color(0xFFE2E8F0))
            Text(String.format(Locale.US, "%.1f%%", pct), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FreshGreenMuted)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { (pct / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = FreshGreen,
            trackColor = Color(0xFF1E3A5F)
        )
    }
}

@Composable
private fun SummaryStatTile(
    title: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = NavyDark)
        Text(sub, fontSize = 10.sp, color = Color(0xFF64748B))
    }
}
