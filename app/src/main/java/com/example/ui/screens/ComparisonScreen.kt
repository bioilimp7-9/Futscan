package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MeasurementBasis
import com.example.data.model.ProductItem
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

@Composable
fun ComparisonScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.productsList.collectAsState()
    val product1 by viewModel.comparisonProduct1.collectAsState()
    val product2 by viewModel.comparisonProduct2.collectAsState()

    var autoNormalizeTo100g by remember { mutableStateOf(false) }

    // Dialog state for selecting product 1 or product 2
    var showSelectP1Dialog by remember { mutableStateOf(false) }
    var showSelectP2Dialog by remember { mutableStateOf(false) }

    // Auto-select initial 2 if not selected
    val p1 = product1 ?: products.getOrNull(0)
    val p2 = product2 ?: products.getOrNull(1)

    val basis1 = p1?.measurementBasis
    val basis2 = p2?.measurementBasis
    val basesMatch = basis1 == basis2

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("comparison_screen")
    ) {
        Text(
            text = "САЛЫСТЫРМАЛЫ ТАҒАМДЫҚ ТАЛДАУ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 1.sp
        )
        Text(
            text = "Өнімдерді салыстыру",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NavyDark
        )
        Text(
            text = "Екі өнімнің қант, тұз, май, ақуыз және калория көрсеткіштерін бірдей өлшем негізінде салыстырыңыз.",
            fontSize = 13.sp,
            color = Color(0xFF475569)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // SELECTED PRODUCTS CARDS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card P1
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showSelectP1Dialog = true }
                    .testTag("card_p1_select"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NavyPrimary, Color(0xFF3B82F6))))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1-ӨНІМ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Text(
                            text = "Өзгерту",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2563EB)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = p1?.productName ?: "Таңдалмаған",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NavyPrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = MeasurementBasis.fromString(p1?.measurementBasis).titleKz,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Card P2
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showSelectP2Dialog = true }
                    .testTag("card_p2_select"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(FreshGreen, FreshGreenDark)))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2-ӨНІМ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FreshGreen
                        )
                        Text(
                            text = "Өзгерту",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FreshGreenDark
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = p2?.productName ?: "Таңдалмаған",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = FreshGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = MeasurementBasis.fromString(p2?.measurementBasis).titleKz,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FreshGreenDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // UNITS & MEASUREMENT BASIS VALIDATION WARNING
        if (p1 != null && p2 != null) {
            if (!basesMatch && !autoNormalizeTo100g) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("basis_mismatch_warning"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFFFCD34D), Color(0xFFF59E0B))))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Ескерту",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ӨЛШЕМ НЕГІЗДЕРІ СӘЙКЕС ЕМЕС!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Бірінші өнім «${MeasurementBasis.fromString(basis1).titleKz}», ал екіншісі «${MeasurementBasis.fromString(basis2).titleKz}» негізінде есептелген. Бірыңғай негізге (100 г) келтірмей тікелей салыстыру ғылыми тұрғыдан дұрыс емес! Төмендегі ауыстырғышты қосыңыз.",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // NORMALIZE TOGGLE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "100 грамға бірыңғай келтіру",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NavyDark
                        )
                        Text(
                            text = if (autoNormalizeTo100g) "Барлық көрсеткіштер 100 г негізіне есептелді" else "Қаптамадағы бастапқы көрсеткіштер",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Switch(
                        checked = autoNormalizeTo100g,
                        onCheckedChange = { autoNormalizeTo100g = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = FreshGreen
                        ),
                        modifier = Modifier.testTag("toggle_normalize_100g")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Factors to normalize to 100g if user toggles
            fun getFactor(p: ProductItem): Float {
                if (!autoNormalizeTo100g) return 1.0f
                if (p.measurementBasis == MeasurementBasis.PER_100G.name) return 1.0f
                val num = p.servingSizeText.filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 30f
                return if (num > 0f) 100f / num else 1.0f
            }

            val f1 = getFactor(p1)
            val f2 = getFactor(p2)

            // SIDE BY SIDE SCIENTIFIC COMPARISON TABLE
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("comparison_table_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "САЛЫСТЫРУ КЕСТЕСІ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Көрсеткіш",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            modifier = Modifier.weight(1.1f)
                        )
                        Text(
                            text = p1.productName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = p2.productName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FreshGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Row: Basis
                    ComparisonTableRow(
                        label = "Өлшем негізі",
                        val1 = if (autoNormalizeTo100g) "100 г (келтірілген)" else MeasurementBasis.fromString(p1.measurementBasis).shortKz,
                        val2 = if (autoNormalizeTo100g) "100 г (келтірілген)" else MeasurementBasis.fromString(p2.measurementBasis).shortKz,
                        isHeader = true
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Row: Sugar
                    ComparisonTableRow(
                        label = "Қант (г)",
                        val1 = formatVal(p1.sugar?.times(f1)),
                        val2 = formatVal(p2.sugar?.times(f2))
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Row: Salt
                    ComparisonTableRow(
                        label = "Тұз (г)",
                        val1 = formatVal(p1.salt?.times(f1)),
                        val2 = formatVal(p2.salt?.times(f2))
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Row: Fat
                    ComparisonTableRow(
                        label = "Май (г)",
                        val1 = formatVal(p1.fat?.times(f1)),
                        val2 = formatVal(p2.fat?.times(f2))
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Row: Protein
                    ComparisonTableRow(
                        label = "Ақуыз (г)",
                        val1 = formatVal(p1.protein?.times(f1)),
                        val2 = formatVal(p2.protein?.times(f2))
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Row: Calories
                    ComparisonTableRow(
                        label = "Калория (ккал)",
                        val1 = formatVal(p1.calories?.times(f1)),
                        val2 = formatVal(p2.calories?.times(f2))
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // VISUAL COMPARATIVE BAR CHARTS
            Text(
                text = "ВИЗУАЛДЫ ДИАГРАММАЛАР",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            ComparativeBarItem(
                indicatorName = "Қант мөлшері (г)",
                p1Name = p1.productName,
                val1 = (p1.sugar ?: 0f) * f1,
                p2Name = p2.productName,
                val2 = (p2.sugar ?: 0f) * f2,
                color = Color(0xFFEC4899)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ComparativeBarItem(
                indicatorName = "Тұз мөлшері (г)",
                p1Name = p1.productName,
                val1 = (p1.salt ?: 0f) * f1,
                p2Name = p2.productName,
                val2 = (p2.salt ?: 0f) * f2,
                color = Color(0xFF3B82F6)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ComparativeBarItem(
                indicatorName = "Май мөлшері (г)",
                p1Name = p1.productName,
                val1 = (p1.fat ?: 0f) * f1,
                p2Name = p2.productName,
                val2 = (p2.fat ?: 0f) * f2,
                color = AccentAmber
            )

            Spacer(modifier = Modifier.height(8.dp))

            ComparativeBarItem(
                indicatorName = "Ақуыз мөлшері (г)",
                p1Name = p1.productName,
                val1 = (p1.protein ?: 0f) * f1,
                p2Name = p2.productName,
                val2 = (p2.protein ?: 0f) * f2,
                color = FreshGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            ComparativeBarItem(
                indicatorName = "Калория (ккал)",
                p1Name = p1.productName,
                val1 = (p1.calories ?: 0f) * f1,
                p2Name = p2.productName,
                val2 = (p2.calories ?: 0f) * f2,
                color = Color(0xFF8B5CF6)
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Салыстыру үшін кемінде 2 өнім қажет.",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = {
                if (p1 != null) viewModel.selectUnifiedProduct(p1.productName)
                viewModel.navigateTo(AppScreen.UNIFIED_REPORT)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("comparison_to_unified_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Бір өнімді күндер бойынша салыстыру (фото, pH, t°) →")
        }

        Spacer(modifier = Modifier.height(12.dp))

        MedicalDisclaimerCard()

        // SELECT PRODUCT 1 DIALOG
        if (showSelectP1Dialog) {
            ProductPickerDialog(
                title = "1-өнімді таңдау",
                products = products,
                currentSelectedId = p1?.id,
                onSelect = { selected ->
                    viewModel.selectForComparison1(selected)
                    showSelectP1Dialog = false
                },
                onDismiss = { showSelectP1Dialog = false }
            )
        }

        // SELECT PRODUCT 2 DIALOG
        if (showSelectP2Dialog) {
            ProductPickerDialog(
                title = "2-өнімді таңдау",
                products = products,
                currentSelectedId = p2?.id,
                onSelect = { selected ->
                    viewModel.selectForComparison2(selected)
                    showSelectP2Dialog = false
                },
                onDismiss = { showSelectP2Dialog = false }
            )
        }
    }
}

@Composable
private fun ProductPickerDialog(
    title: String,
    products: List<ProductItem>,
    currentSelectedId: Int?,
    onSelect: (ProductItem) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold, color = NavyDark) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products) { item ->
                    val isSelected = item.id == currentSelectedId
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                if (isSelected) listOf(Color(0xFF3B82F6), Color(0xFF2563EB)) else listOf(SlateBorder, SlateBorder)
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(item) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) NavyPrimary else Color(0xFFCBD5E1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${item.productNumber}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.productName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark
                                )
                                Text(
                                    text = "${item.category} • ${MeasurementBasis.fromString(item.measurementBasis).shortKz} (${item.servingSizeText})",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Жабу")
            }
        }
    )
}

@Composable
private fun ComparisonTableRow(
    label: String,
    val1: String,
    val2: String,
    isHeader: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isHeader) Color(0xFF64748B) else NavyDark,
            modifier = Modifier.weight(1.1f)
        )
        Text(
            text = val1,
            fontSize = 13.sp,
            fontWeight = if (isHeader) FontWeight.Medium else FontWeight.Bold,
            color = NavyPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = val2,
            fontSize = 13.sp,
            fontWeight = if (isHeader) FontWeight.Medium else FontWeight.Bold,
            color = FreshGreen,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ComparativeBarItem(
    indicatorName: String,
    p1Name: String,
    val1: Float,
    p2Name: String,
    val2: Float,
    color: Color
) {
    val maxVal = maxOf(val1, val2, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = indicatorName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Bar 1 (Product 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = p1Name.take(14),
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.width(90.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (val1 / maxVal).coerceIn(0.02f, 1f))
                            .height(14.dp)
                            .background(NavyPrimary)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format(java.util.Locale.US, "%.1f", val1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    modifier = Modifier.width(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bar 2 (Product 2)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = p2Name.take(14),
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.width(90.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (val2 / maxVal).coerceIn(0.02f, 1f))
                            .height(14.dp)
                            .background(FreshGreen)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format(java.util.Locale.US, "%.1f", val2),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FreshGreen,
                    modifier = Modifier.width(40.dp)
                )
            }
        }
    }
}

private fun formatVal(value: Float?): String {
    return if (value != null) {
        String.format(java.util.Locale.US, "%.1f", value)
    } else {
        "—"
    }
}
