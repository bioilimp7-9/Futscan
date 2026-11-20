package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DataSourceTag
import com.example.data.model.StorageMonitorRecord
import com.example.scanner.VisionAnalysisHelper
import com.example.ui.MainViewModel
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageMonitorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val records by viewModel.storageRecords.collectAsState()

    var productName by remember { mutableStateOf("") }
    var roomTempInput by remember { mutableStateOf("") }
    var fridgeTempInput by remember { mutableStateOf("") }
    var humidityInput by remember { mutableStateOf("") }
    var lightCondition by remember { mutableStateOf("Өлшенбеген") }
    var storageLocation by remember { mutableStateOf("Тоңазытқыш") }
    var storageDuration by remember { mutableStateOf("") }
    var measuredAt by remember { mutableStateOf(VisionAnalysisHelper.nowFormatted()) }

    var lightMenuOpen by remember { mutableStateOf(false) }
    var locationMenuOpen by remember { mutableStateOf(false) }

    val lightOptions = listOf("Өлшенбеген", "Жарық", "Көлеңке", "Қараңғы")
    val locationOptions = listOf("Тоңазытқыш", "Бөлме", "Басқа")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("storage_monitor_screen")
    ) {
        Text(
            text = "MODULE 3 • STORAGE MONITOR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = FreshGreen,
            letterSpacing = 1.sp
        )
        Text(
            text = "Сақтау жағдайын бақылау",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NavyDark
        )
        Text(
            text = "Қазір физикалық датчиктер жоқ, сондықтан нақты өлшенген мәндерді қолмен енгізіңіз.",
            fontSize = 12.sp,
            color = Color(0xFF475569)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
        ) {
            Text(
                text = "${viewModel.sensorProvider.providerStatusKz} Толтырылмаған өрістер «Өлшенбеген» / «Дерек жоқ» ретінде сақталады.",
                fontSize = 11.sp,
                color = Color(0xFF1E3A8A),
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Өнім атауы *") },
                    placeholder = { Text("Мысалы: Табиғи сүт") },
                    modifier = Modifier.fillMaxWidth().testTag("storage_product_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = roomTempInput,
                        onValueChange = { roomTempInput = it },
                        label = { Text("Бөлме t° (°C)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fridgeTempInput,
                        onValueChange = { fridgeTempInput = it },
                        label = { Text("Тоңазытқыш t° (°C)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = humidityInput,
                    onValueChange = { humidityInput = it },
                    label = { Text("Ауа ылғалдылығы (%, бар болса)") },
                    placeholder = { Text("Өлшенбесе бос қалдырыңыз") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = lightMenuOpen,
                        onExpandedChange = { lightMenuOpen = !lightMenuOpen },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = lightCondition,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Жарық жағдайы") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lightMenuOpen) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = lightMenuOpen,
                            onDismissRequest = { lightMenuOpen = false }
                        ) {
                            lightOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        lightCondition = opt
                                        lightMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = locationMenuOpen,
                        onExpandedChange = { locationMenuOpen = !locationMenuOpen },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = storageLocation,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Сақтау орны") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationMenuOpen) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = locationMenuOpen,
                            onDismissRequest = { locationMenuOpen = false }
                        ) {
                            locationOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        storageLocation = opt
                                        locationMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = storageDuration,
                    onValueChange = { storageDuration = it },
                    label = { Text("Сақтау ұзақтығы") },
                    placeholder = { Text("Мысалы: 24 сағат немесе 2 күн") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = measuredAt,
                    onValueChange = { measuredAt = it },
                    label = { Text("Өлшеу күні мен уақыты") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (productName.isBlank()) {
                            Toast.makeText(context, "Өнім атауын енгізіңіз.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val record = StorageMonitorRecord(
                            productName = productName.trim(),
                            roomTempC = roomTempInput.replace(',', '.').toFloatOrNull(),
                            fridgeTempC = fridgeTempInput.replace(',', '.').toFloatOrNull(),
                            humidityPercent = humidityInput.replace(',', '.').toFloatOrNull(),
                            lightCondition = lightCondition,
                            storageLocation = storageLocation,
                            storageDuration = storageDuration.trim().ifBlank { "Дерек жоқ" },
                            dataSource = DataSourceTag.USER.labelKz,
                            measuredAtText = measuredAt.trim().ifBlank { VisionAnalysisHelper.nowFormatted() }
                        )
                        viewModel.saveStorageRecord(record) {
                            Toast.makeText(context, "Сақтау жағдайы сақталды!", Toast.LENGTH_SHORT).show()
                            roomTempInput = ""
                            fridgeTempInput = ""
                            humidityInput = ""
                            storageDuration = ""
                            measuredAt = VisionAnalysisHelper.nowFormatted()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("storage_save_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Сақтау жағдайын сақтау", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text("САҚТАЛҒАН ЖАЗБАЛАР (${records.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(8.dp))

        records.forEach { r ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(r.productName, fontWeight = FontWeight.Bold, color = NavyDark)
                        Text(
                            text = "Бөлме: ${r.roomTempC?.let { "$it°C" } ?: "Өлшенбеген"} • Тоңазытқыш: ${r.fridgeTempC?.let { "$it°C" } ?: "Өлшенбеген"} • Ылғ: ${r.humidityPercent?.let { "$it%" } ?: "Өлшенбеген"}",
                            fontSize = 11.sp,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = "${r.storageLocation} • ${r.lightCondition} • ${r.storageDuration} • ${r.measuredAtText}",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    IconButton(onClick = { viewModel.deleteStorageRecord(r) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Өшіру", tint = Color(0xFFDC2626))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        MedicalDisclaimerCard()
    }
}
