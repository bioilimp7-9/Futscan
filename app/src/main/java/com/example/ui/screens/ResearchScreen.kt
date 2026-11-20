package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MeasurementBasis
import com.example.data.model.ProductItem
import com.example.data.model.VerificationStatus
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.FreshGreenDark
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SlateBorder
import kotlinx.coroutines.launch

@Composable
fun ResearchScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val products by viewModel.productsList.collectAsState()
    val metrics by viewModel.metrics.collectAsState()

    var showVerificationDialog by remember { mutableStateOf(false) }
    var selectedProductForVerify by remember { mutableStateOf<ProductItem?>(null) }
    var showCsvPreviewDialog by remember { mutableStateOf(false) }
    var csvContent by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .testTag("research_screen")
    ) {
        // MODULE TITLE
        Text(
            text = "ҒЫЛЫМИ ЗЕРТТЕУ ЗЕРТХАНАСЫ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = FreshGreen,
            letterSpacing = 1.sp
        )
        Text(
            text = "Зерттеу дерекқоры",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NavyDark
        )
        Text(
            text = "Қанағат Айымның 8-сынып ғылыми жобасына жиналған тағам өнімдері (лимит: 30 өнім).",
            fontSize = 12.sp,
            color = Color(0xFF475569)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ACCURACY & LIMIT METRICS CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("research_metrics_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ЖИНАҚТАЛҒАН ӨНІМДЕР",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "${products.size} / 30 өнім",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "OCR ДӘЛДІК ДЕҢГЕЙІ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f%%", metrics?.overallAccuracyPercentage ?: 100f),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = FreshGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { (products.size / 30f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = FreshGreen,
                    trackColor = Color(0xFF334155)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Тексерілген: ${metrics?.verifiedCount ?: 0}",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "Түзетілген: ${metrics?.correctedCount ?: 0}",
                        fontSize = 11.sp,
                        color = Color(0xFFFCD34D)
                    )
                    Text(
                        text = "Тексерілмеген: ${metrics?.unverifiedCount ?: 0}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ACTIONS: EXPORT CSV & ADD PRODUCT
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        val csv = viewModel.exportCsv()
                        csvContent = csv
                        showCsvPreviewDialog = true
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("export_csv_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("CSV экспорттау", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { viewModel.navigateTo(AppScreen.SCANNER) },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("add_product_to_research"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Жаңа өнім қосу", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { viewModel.navigateTo(AppScreen.RESEARCH_PROTOCOL) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("open_protocol_from_research"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.FactCheck, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("6 парақты хаттама: Нұсқаулық, 8-сұрақ тесті, Қорытынды", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // CATEGORY FILTER CHIPS
        var selectedCategoryFilter by remember { mutableStateOf("Барлығы") }
        val categoryFilters = listOf("Барлығы", "Тәттілер", "Сүт", "Снектер", "Сусындар")

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categoryFilters) { cat ->
                val isSelected = selectedCategoryFilter == cat
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) NavyPrimary else Color.White,
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.clickable { selectedCategoryFilter = cat }
                ) {
                    Text(
                        text = cat,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else NavyDark,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // PRODUCTS LIST (1..30)
        Text(
            text = "ЗЕРТТЕУ ЖАЗБАЛАРЫ ТІЗІМІ:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(6.dp))

        val filteredProducts = remember(products, selectedCategoryFilter) {
            if (selectedCategoryFilter == "Барлығы") {
                products
            } else {
                products.filter { it.category.contains(selectedCategoryFilter, ignoreCase = true) }
            }
        }

        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Бұл санатта зерттеу өнімдері жоқ.\nСканер бөлімінен жаңа өнім қосыңыз.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts) { product ->
                    ResearchProductCard(
                        product = product,
                        onVerify = {
                            selectedProductForVerify = product
                            showVerificationDialog = true
                        },
                        onCompare = {
                            viewModel.selectForComparison1(product)
                            viewModel.navigateTo(AppScreen.COMPARISON)
                        },
                        onDelete = { viewModel.deleteProduct(product) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    MedicalDisclaimerCard()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // CSV PREVIEW & SHARE DIALOG
        if (showCsvPreviewDialog) {
            AlertDialog(
                onDismissRequest = { showCsvPreviewDialog = false },
                title = { Text("CSV зерттеу деректері", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "Деректер Excel бағдарламасында ашылатын UTF-8 форматында дайын:",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = csvContent,
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = NavyDark
                            )
                        }
                    }
                },
                confirmButton = {
                    Row {
                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, csvContent)
                                    type = "text/csv"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "FOODSCAN AI зерттеу деректерін жіберу")
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Жіберу / Бөлісу")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        clipboardManager.setText(AnnotatedString(csvContent))
                        Toast.makeText(context, "CSV алмасу буферіне көшірілді!", Toast.LENGTH_SHORT).show()
                        showCsvPreviewDialog = false
                    }) {
                        Text("Көшіру")
                    }
                }
            )
        }

        // VERIFICATION / ACCURACY CHECK DIALOG
        if (showVerificationDialog && selectedProductForVerify != null) {
            val prod = selectedProductForVerify!!
            var editSugar by remember { mutableStateOf(prod.sugar?.toString() ?: "") }
            var editSalt by remember { mutableStateOf(prod.salt?.toString() ?: "") }
            var editFat by remember { mutableStateOf(prod.fat?.toString() ?: "") }
            var editProtein by remember { mutableStateOf(prod.protein?.toString() ?: "") }
            var editCal by remember { mutableStateOf(prod.calories?.toString() ?: "") }

            AlertDialog(
                onDismissRequest = { showVerificationDialog = false },
                title = { Text("Қаптамамен ғылыми тексеру", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "Өнім: ${prod.productName} (#${prod.productNumber})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AI анықтаған мәндерді физикалық қаптамамен салыстырып, нақты көрсеткіштерді енгізіңіз:",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = editSugar,
                            onValueChange = { editSugar = it },
                            label = { Text("Қант (AI: ${prod.extractedSugar ?: "—"})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = editSalt,
                            onValueChange = { editSalt = it },
                            label = { Text("Тұз (AI: ${prod.extractedSalt ?: "—"})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = editFat,
                            onValueChange = { editFat = it },
                            label = { Text("Май (AI: ${prod.extractedFat ?: "—"})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = editProtein,
                            onValueChange = { editProtein = it },
                            label = { Text("Ақуыз (AI: ${prod.extractedProtein ?: "—"})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = editCal,
                            onValueChange = { editCal = it },
                            label = { Text("Калория (AI: ${prod.extractedCalories ?: "—"})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newSugar = editSugar.toFloatOrNull()
                            val newSalt = editSalt.toFloatOrNull()
                            val newFat = editFat.toFloatOrNull()
                            val newProtein = editProtein.toFloatOrNull()
                            val newCal = editCal.toFloatOrNull()

                            val isIdentical = (newSugar == prod.extractedSugar && newSalt == prod.extractedSalt && newFat == prod.extractedFat && newProtein == prod.extractedProtein && newCal == prod.extractedCalories)
                            val status = if (isIdentical) VerificationStatus.VERIFIED else VerificationStatus.CORRECTED

                            viewModel.updateProductVerification(
                                product = prod,
                                status = status,
                                actualSugar = newSugar,
                                actualSalt = newSalt,
                                actualFat = newFat,
                                actualProtein = newProtein,
                                actualCal = newCal
                            )
                            showVerificationDialog = false
                            Toast.makeText(context, "Деректер жаңартылып, OCR дәлдігі қайта есептелді!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                    ) {
                        Text("Тексеруді сақтау")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showVerificationDialog = false }) {
                        Text("Бас тарту")
                    }
                }
            )
        }
    }
}

@Composable
private fun ResearchProductCard(
    product: ProductItem,
    onVerify: () -> Unit,
    onCompare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("research_card_${product.productNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${product.productNumber}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = product.productName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        Text(
                            text = "${product.category} • ${product.servingSizeText}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Verification Status Badge
                val (badgeBg, badgeText, badgeColor) = when (product.verificationStatus) {
                    VerificationStatus.VERIFIED.name -> Triple(Color(0xFFD1FAE5), "Тексерілді", FreshGreenDark)
                    VerificationStatus.CORRECTED.name -> Triple(Color(0xFFFEF3C7), "Түзетілді", AccentAmber)
                    else -> Triple(Color(0xFFF1F5F9), "Тексерілмеген", Color(0xFF64748B))
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Values grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                NutrientMicroItem(label = "Қант", value = "${product.sugar ?: "—"}г")
                NutrientMicroItem(label = "Тұз", value = "${product.salt ?: "—"}г")
                NutrientMicroItem(label = "Май", value = "${product.fat ?: "—"}г")
                NutrientMicroItem(label = "Ақуыз", value = "${product.protein ?: "—"}г")
                NutrientMicroItem(label = "Калория", value = "${product.calories ?: "—"}ккал")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI: қант ${product.extractedSugar ?: "—"}г, май ${product.extractedFat ?: "—"}г",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )

                Row {
                    IconButton(
                        onClick = onCompare,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("compare_product_${product.productNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "Салыстыру",
                            tint = FreshGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onVerify,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("verify_product_${product.productNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Тексеру",
                            tint = AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_product_${product.productNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Өшіру",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NutrientMicroItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = Color(0xFF64748B))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyDark)
    }
}
