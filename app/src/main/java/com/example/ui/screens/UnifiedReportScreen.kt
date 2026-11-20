package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.model.DataSourceTag
import com.example.export.UnifiedReportPdfData
import com.example.export.UnifiedReportPdfExporter
import com.example.ui.MainViewModel
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedReportScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val products by viewModel.productsList.collectAsState()
    val freshnessList by viewModel.freshnessRecords.collectAsState()
    val phList by viewModel.chemicalPhRecords.collectAsState()
    val storageList by viewModel.storageRecords.collectAsState()
    val selectedNameState by viewModel.selectedUnifiedProductName.collectAsState()

    val allProductNames = remember(products, freshnessList, phList, storageList) {
        linkedSetOf<String>().apply {
            products.forEach { add(it.productName.trim()) }
            freshnessList.forEach { add(it.productName.trim()) }
            phList.forEach { add(it.productName.trim()) }
            storageList.forEach { add(it.productName.trim()) }
        }.filter { it.isNotBlank() }.toList()
    }

    val activeName = selectedNameState.ifBlank { allProductNames.firstOrNull() ?: "" }
    var dropdownOpen by remember { mutableStateOf(false) }

    var studentAuthor by remember { mutableStateOf("") }
    var projectTitle by remember { mutableStateOf("Мектеп ғылыми жобасы: Тағамды кешенді талдау") }
    var isExportingPdf by remember { mutableStateOf(false) }

    val norm: (String) -> String = { it.trim().lowercase() }
    val ocrProduct = products.firstOrNull { norm(it.productName) == norm(activeName) }
    val visionMatches = freshnessList.filter { norm(it.productName) == norm(activeName) }
    val phMatches = phList.filter { norm(it.productName) == norm(activeName) }
    val storageMatches = storageList.filter { norm(it.productName) == norm(activeName) }

    val latestVision = visionMatches.firstOrNull()
    val latestPh = phMatches.firstOrNull()
    val latestStorage = storageMatches.firstOrNull()

    val phValues = phMatches.reversed().mapNotNull { extractPhFloat(it.estimatedPhRange) }
    val tempValues = storageMatches.reversed().mapNotNull { it.fridgeTempC ?: it.roomTempC }

    val buildPdfData = {
        UnifiedReportPdfData(
            productName = activeName.ifBlank { "Зерттелетін өнім" },
            studentAuthor = studentAuthor,
            schoolProjectTitle = projectTitle,
            ocrProduct = ocrProduct,
            visionRecords = visionMatches,
            phRecords = phMatches,
            storageRecords = storageMatches
        )
    }

    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isExportingPdf = true
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { stream ->
                            UnifiedReportPdfExporter.writePdfToStream(context, stream, buildPdfData())
                        } ?: error("Файл ағынын ашу мүмкін болмады")
                    }
                }
                isExportingPdf = false
                if (result.isSuccess) {
                    Toast.makeText(context, "PDF есеп сәтті сақталды!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "PDF сақтау қатесі: ${result.exceptionOrNull()?.localizedMessage ?: ""}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("unified_report_screen")
    ) {
        Text(
            text = "SMART FOOD ANALYSIS SYSTEM • A–D ЕСЕП",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary,
            letterSpacing = 1.sp
        )
        Text(
            text = "Біріктірілген есеп және динамика",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NavyDark
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = dropdownOpen,
            onExpandedChange = { dropdownOpen = !dropdownOpen }
        ) {
            OutlinedTextField(
                value = activeName.ifBlank { "Өнім таңдаңыз" },
                onValueChange = {},
                readOnly = true,
                label = { Text("Зерттелетін өнім") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownOpen) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .testTag("unified_product_selector")
            )
            ExposedDropdownMenu(
                expanded = dropdownOpen,
                onDismissRequest = { dropdownOpen = false }
            ) {
                allProductNames.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            viewModel.selectUnifiedProduct(name)
                            dropdownOpen = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SCHOOL PROJECT PDF EXPORT CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("unified_pdf_export_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "МЕКТЕП ЖОБАСЫНА PDF ЭКСПОРТТАУ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FreshGreen,
                    letterSpacing = 0.7.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = studentAuthor,
                    onValueChange = { studentAuthor = it },
                    label = { Text("Оқушы аты-жөні / Сыныбы (PDF тақырыбына)") },
                    placeholder = { Text("Мысалы: Қанағат Айым • 8-сынып") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pdf_student_author_input")
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = projectTitle,
                    onValueChange = { projectTitle = it },
                    label = { Text("Ғылыми жоба тақырыбы") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pdf_project_title_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            createPdfLauncher.launch(UnifiedReportPdfExporter.buildSuggestedFilename(activeName))
                        },
                        enabled = !isExportingPdf,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_unified_pdf_save_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        if (isExportingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF сақтау", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isExportingPdf = true
                                val fileResult = withContext(Dispatchers.IO) {
                                    runCatching {
                                        UnifiedReportPdfExporter.createShareablePdfFile(context, buildPdfData())
                                    }
                                }
                                isExportingPdf = false
                                fileResult.onSuccess { pdfFile ->
                                    val contentUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        pdfFile
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, contentUri)
                                        putExtra(Intent.EXTRA_SUBJECT, "FOODSCAN AI 2.0 — $activeName есебі")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "PDF есепті жіберу / басып шығару"))
                                }.onFailure { err ->
                                    Toast.makeText(context, "PDF дайындау қатесі: ${err.localizedMessage ?: ""}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !isExportingPdf,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_unified_pdf_share_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF бөлісу", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // A. Food Composition
        ReportSectionCard(
            title = "A. Қоректік құрам (OCR)",
            source = if (ocrProduct != null) DataSourceTag.OCR.labelKz else DataSourceTag.NONE.labelKz
        ) {
            if (ocrProduct != null) {
                Text("Өлшем негізі: ${ocrProduct.servingSizeText}", fontSize = 12.sp, color = Color(0xFF475569))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ккал: ${ocrProduct.calories ?: "Дерек жоқ"} • Қант: ${ocrProduct.sugar ?: "Дерек жоқ"} г • Май: ${ocrProduct.fat ?: "Дерек жоқ"} г • Ақуыз: ${ocrProduct.protein ?: "Дерек жоқ"} г • Тұз: ${ocrProduct.salt ?: "Дерек жоқ"} г",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyDark
                )
            } else {
                Text("Дерек жоқ", fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }

        // B. Visual Analysis
        ReportSectionCard(
            title = "B. Визуалды талдау",
            source = latestVision?.dataSource ?: DataSourceTag.NONE.labelKz
        ) {
            if (latestVision != null) {
                Text("Мәртебе: ${latestVision.statusLabel}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FreshGreen)
                Text("Сипаттама: ${latestVision.surfaceDescription}", fontSize = 12.sp, color = Color(0xFF334155))
                Text("Өзгерістер: ${latestVision.observedChanges}", fontSize = 12.sp, color = Color(0xFF334155))
                Text("Уақыты: ${latestVision.measuredAtText}", fontSize = 11.sp, color = Color(0xFF64748B))
            } else {
                Text("Дерек жоқ", fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }

        // C. Chemical Analysis
        ReportSectionCard(
            title = "C. Химиялық талдау (pH)",
            source = latestPh?.dataSource ?: DataSourceTag.NONE.labelKz
        ) {
            if (latestPh != null) {
                Text("Үлгі: ${latestPh.sampleType}", fontSize = 12.sp, color = Color(0xFF334155))
                Text("Индикатор түсі: ${latestPh.paperColor}", fontSize = 12.sp, color = Color(0xFF334155))
                Text("Болжамды pH: ${latestPh.estimatedPhRange}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                Text("Уақыты: ${latestPh.measuredAtText}", fontSize = 11.sp, color = Color(0xFF64748B))
            } else {
                Text("Дерек жоқ", fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }

        // D. Storage Conditions
        ReportSectionCard(
            title = "D. Сақтау жағдайы",
            source = latestStorage?.dataSource ?: DataSourceTag.NONE.labelKz
        ) {
            if (latestStorage != null) {
                Text(
                    text = "Бөлме t°: ${latestStorage.roomTempC?.let { "$it °C" } ?: "Өлшенбеген"} • Тоңазытқыш t°: ${latestStorage.fridgeTempC?.let { "$it °C" } ?: "Өлшенбеген"}",
                    fontSize = 12.sp,
                    color = NavyDark
                )
                Text(
                    text = "Ылғалдылық: ${latestStorage.humidityPercent?.let { "$it%" } ?: "Өлшенбеген"} • Жарық: ${latestStorage.lightCondition} • Ұзақтығы: ${latestStorage.storageDuration}",
                    fontSize = 12.sp,
                    color = Color(0xFF334155)
                )
                Text("Уақыты: ${latestStorage.measuredAtText}", fontSize = 11.sp, color = Color(0xFF64748B))
            } else {
                Text("Дерек жоқ", fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
        ) {
            Text(
                text = "Ескерту: Сурет жарығы әртүрлі болса, түс салыстыру нәтижесінің дәлдігі төмендеуі мүмкін.",
                fontSize = 11.sp,
                color = Color(0xFF92400E),
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("КҮНДЕР БОЙЫНША ФОТО ЖӘНЕ БЕЛГІЛЕР (${visionMatches.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(6.dp))

        if (visionMatches.isEmpty()) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Text("Бұл өнім бойынша көпкүндік фото жазбалар әлі жоқ.", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(14.dp))
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(visionMatches) { item ->
                    Card(
                        modifier = Modifier.width(180.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (!item.imageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = Uri.parse(item.imageUri),
                                    contentDescription = item.productName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            Text(item.measuredAtText, fontSize = 10.sp, color = Color(0xFF64748B))
                            Text(item.statusLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                            Text(item.observedChanges, fontSize = 11.sp, color = Color(0xFF475569), maxLines = 2)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        TrendLineCard(title = "pH өзгерісінің графигі", values = phValues, color = Color(0xFF7C3AED), minY = 2f, maxY = 12f)
        Spacer(modifier = Modifier.height(10.dp))
        TrendLineCard(title = "Сақтау температурасының графигі (°C)", values = tempValues, color = AccentCyan, minY = -5f, maxY = 35f)

        Spacer(modifier = Modifier.height(14.dp))
        MedicalDisclaimerCard()
    }
}

@Composable
private fun ReportSectionCard(
    title: String,
    source: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark, modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE0F2FE)) {
                    Text(
                        text = source,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0369A1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun TrendLineCard(
    title: String,
    values: List<Float>,
    color: Color,
    minY: Float,
    maxY: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyDark)
            Spacer(modifier = Modifier.height(8.dp))
            if (values.isEmpty()) {
                Text("График құру үшін кемінде 1 өлшенген мән қажет.", fontSize = 12.sp, color = Color(0xFF64748B))
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                ) {
                    val pad = 20f
                    val w = size.width - pad * 2
                    val h = size.height - pad * 2
                    val span = (maxY - minY).coerceAtLeast(1f)
                    val points = values.mapIndexed { idx, v ->
                        val x = if (values.size == 1) size.width / 2f else pad + (idx.toFloat() / (values.size - 1)) * w
                        val y = size.height - pad - (((v.coerceIn(minY, maxY) - minY) / span) * h)
                        Offset(x, y)
                    }
                    if (points.size > 1) {
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            points.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(path, color = color, style = Stroke(width = 5f))
                    }
                    points.forEach { pt ->
                        drawCircle(color = color, radius = 9f, center = pt)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Мәндер реті: " + values.joinToString(" → ") { String.format(java.util.Locale.US, "%.1f", it) },
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                )
            }
        }
    }
}

private fun extractPhFloat(text: String): Float? {
    if (text.isBlank() || text == "анықтау мүмкін болмады") return null
    val regex = Regex("[0-9]+(?:\\.[0-9]+)?")
    val nums = regex.findAll(text.replace(',', '.')).mapNotNull { it.value.toFloatOrNull() }.filter { it in 0f..14f }.toList()
    return if (nums.isNotEmpty()) nums.average().toFloat() else null
}
