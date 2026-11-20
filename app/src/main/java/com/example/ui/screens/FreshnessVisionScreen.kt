package com.example.ui.screens

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.DataSourceTag
import com.example.data.model.FreshnessVisionRecord
import com.example.data.model.VisionStatusOption
import com.example.scanner.VisionAnalysisHelper
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreshnessVisionScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val records by viewModel.freshnessRecords.collectAsState()

    var productNameInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var recognizedName by remember { mutableStateOf("") }
    var surfaceDesc by remember { mutableStateOf("") }
    var observedChanges by remember { mutableStateOf("") }
    var comparativeConclusion by remember { mutableStateOf("") }
    var confidenceLimits by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(VisionStatusOption.LOW_CHANGES) }
    var hasAnalysisDraft by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            hasAnalysisDraft = false
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val file = File(context.cacheDir, "freshness_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                selectedImageUri = Uri.fromFile(file)
                hasAnalysisDraft = false
            } catch (e: Exception) {
                Toast.makeText(context, "Сурет сақтау қатесі: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Камераға рұқсат берілмеді. Галереядан жүктей аласыз.", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("freshness_vision_screen")
    ) {
        Text(
            text = "MODULE 1 • FRESHNESS VISION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentCyan,
            letterSpacing = 1.sp
        )
        Text(
            text = "Визуалды балғындық талдау",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NavyDark
        )
        Text(
            text = "Камера немесе галерея суреті бойынша тағамның сыртқы түсін, қараю, қоңырлану, дақтар және зақымдану белгілерін сипаттау.",
            fontSize = 12.sp,
            color = Color(0xFF475569)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Critical Non-Medical & Non-Microbiological Notice
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Warning, contentDescription = "Ескерту", tint = AccentAmber, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Визуалды талдау медициналық немесе санитарлық қауіпсіздік қорытындысы ЕМЕС. Бактерия саны мен ішкі микробиологиялық қауіпсіздік фотодан анықталмайды.",
                    fontSize = 11.sp,
                    color = Color(0xFF92400E),
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = productNameInput,
                    onValueChange = { productNameInput = it },
                    label = { Text("Өнім атауы (күндер бойынша бақылау үшін)") },
                    placeholder = { Text("Мысалы: Алма немесе Сүт") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vision_product_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Тағам фотосы",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "Тағам суретін камерамен түсіріңіз немесе галереядан таңдаңыз",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) cameraLauncher.launch(null)
                            else permissionLauncher.launch(android.Manifest.permission.CAMERA)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("vision_camera_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Камера", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("vision_gallery_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Галерея", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val uri = selectedImageUri
                        if (uri == null) {
                            Toast.makeText(context, "Алдымен сурет түсіріңіз немесе жүктеңіз.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val px = VisionAnalysisHelper.inspectImageUri(context, uri)
                        recognizedName = productNameInput.ifBlank { "анықтау мүмкін болмады" }
                        if (px == null) {
                            surfaceDesc = "Сурет пиксельдерін оқу мүмкін болмады."
                            observedChanges = "анықтау мүмкін болмады"
                            comparativeConclusion = "Сурет сапасы жеткіліксіз — жарық жерде қайта түсіріңіз."
                            selectedStatus = VisionStatusOption.INSUFFICIENT_QUALITY
                        } else {
                            val poorLight = px.averageLuminance < 45 || px.averageLuminance > 240
                            surfaceDesc = "Сыртқы орташа түс реңкі: ${px.descriptiveToneKz}; жарықтық: ${px.averageLuminance}/255."
                            observedChanges = "Күңгірт/қарайған аймақ үлесі: ${px.darkPixelPercent}%. Нақты дақтар немесе зақымдану болса төменде сипаттаңыз."
                            comparativeConclusion = if (poorLight) {
                                "Жарық жеткіліксіз немесе шамадан тыс — суретті қайта түсіру ұсынылады."
                            } else {
                                "Тек сыртқы көрінетін белгілер бойынша бастапқы тексеру орындалды; өзгерісті растаңыз."
                            }
                            selectedStatus = when {
                                poorLight -> VisionStatusOption.INSUFFICIENT_QUALITY
                                px.darkPixelPercent >= 35 -> VisionStatusOption.CLEAR_CHANGES
                                px.darkPixelPercent >= 15 -> VisionStatusOption.VISIBLE_CHANGES
                                else -> VisionStatusOption.LOW_CHANGES
                            }
                        }
                        confidenceLimits = if (viewModel.isCloudAiConfigured) {
                            "Жарық және бұрышқа байланысты шектеу бар; микробиологиялық қауіпсіздік фотодан анықталмайды."
                        } else {
                            "Бұлттық Gemini Vision өшірулі (Firebase қосылмаған): құрылғыдағы түс/жарық талдауы орындалды. Бактерия мен ішкі бұзылу фотодан анықталмайды."
                        }
                        hasAnalysisDraft = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("vision_analyze_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сыртқы белгілерді талдау", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (hasAnalysisDraft) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ВИЗУАЛДЫ ТАЛДАУ НӘТИЖЕСІ (1–5)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = recognizedName,
                        onValueChange = { recognizedName = it },
                        label = { Text("1. Танылған тағам атауы") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = surfaceDesc,
                        onValueChange = { surfaceDesc = it },
                        label = { Text("2. Сыртқы көрініс сипаттамасы") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = observedChanges,
                        onValueChange = { observedChanges = it },
                        label = { Text("3. Байқалған өзгерістер (қараю, дақтар, зақымдану)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comparativeConclusion,
                        onValueChange = { comparativeConclusion = it },
                        label = { Text("4. Көрнекі белгілер бойынша салыстырмалы қорытынды") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = statusMenuExpanded,
                        onExpandedChange = { statusMenuExpanded = !statusMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedStatus.labelKz,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Қорытынды мәртебесі") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = statusMenuExpanded,
                            onDismissRequest = { statusMenuExpanded = false }
                        ) {
                            VisionStatusOption.entries.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt.labelKz) },
                                    onClick = {
                                        selectedStatus = opt
                                        statusMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC)
                    ) {
                        Text(
                            text = "5. Сенімділігі мен шектеулері: $confidenceLimits",
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val finalProduct = productNameInput.ifBlank { recognizedName }.ifBlank { "Талданған тағам" }
                            viewModel.saveFreshnessRecord(
                                FreshnessVisionRecord(
                                    productName = finalProduct,
                                    recognizedFoodName = recognizedName.ifBlank { finalProduct },
                                    surfaceDescription = surfaceDesc,
                                    observedChanges = observedChanges,
                                    comparativeConclusion = comparativeConclusion,
                                    confidenceAndLimits = confidenceLimits,
                                    statusLabel = selectedStatus.labelKz,
                                    imageUri = selectedImageUri?.toString(),
                                    dataSource = DataSourceTag.CAMERA.labelKz,
                                    measuredAtText = VisionAnalysisHelper.nowFormatted()
                                )
                            ) {
                                Toast.makeText(context, "Визуалды талдау сақталды!", Toast.LENGTH_SHORT).show()
                                hasAnalysisDraft = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("vision_save_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Тарихқа сақтау")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        OutlinedButton(
            onClick = { viewModel.navigateTo(AppScreen.UNIFIED_REPORT) },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Біріктірілген есеп және Күндер бойынша салыстыру →")
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text("САҚТАЛҒАН ВИЗУАЛДЫ ЖАЗБАЛАР (${records.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(8.dp))

        records.forEach { rec ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(rec.productName, fontWeight = FontWeight.Bold, color = NavyDark)
                        Text("${rec.statusLabel} • ${rec.measuredAtText}", fontSize = 11.sp, color = FreshGreen)
                        Text(rec.observedChanges, fontSize = 11.sp, color = Color(0xFF475569), maxLines = 2)
                    }
                    IconButton(onClick = { viewModel.deleteFreshnessRecord(rec) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Өшіру", tint = Color(0xFFDC2626))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        MedicalDisclaimerCard()
    }
}
