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
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.ChemicalPhRecord
import com.example.data.model.DataSourceTag
import com.example.scanner.VisionAnalysisHelper
import com.example.ui.MainViewModel
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import java.io.File
import java.io.FileOutputStream

@Composable
fun ChemicalPhScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val records by viewModel.chemicalPhRecords.collectAsState()

    var productName by remember { mutableStateOf("") }
    var sampleType by remember { mutableStateOf("") }
    var scaleVisibleInPhoto by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    var hasDraft by remember { mutableStateOf(false) }
    var paperColorText by remember { mutableStateOf("") }
    var estimatedPhRange by remember { mutableStateOf("анықтау мүмкін болмады") }
    var acidityExplanation by remember { mutableStateOf("") }
    var confidenceLevel by remember { mutableStateOf("Төмен") }
    var errorMarginNote by remember { mutableStateOf("") }
    var measuredAt by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            hasDraft = false
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val file = File(context.cacheDir, "ph_paper_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                selectedUri = Uri.fromFile(file)
                hasDraft = false
            } catch (e: Exception) {
                Toast.makeText(context, "Сурет сақтау қатесі: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(null)
        else Toast.makeText(context, "Камераға рұқсат берілмеді. Галереяны пайдаланыңыз.", Toast.LENGTH_LONG).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("chemical_ph_screen")
    ) {
        Text(
            text = "MODULE 2 • CHEMICAL ANALYSIS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7C3AED),
            letterSpacing = 1.sp
        )
        Text(
            text = "Химиялық pH талдау",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NavyDark
        )
        Text(
            text = "Универсалды pH индикатор қағазын телефон камерасымен бағалау (шамамен бағалау).",
            fontSize = 12.sp,
            color = Color(0xFF475569)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Warning, contentDescription = "Ескерту", tint = AccentAmber, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "pH қағазының нәтижесі зертханалық pH-метр өлшемі ЕМЕС, тек шамамен бағалау. Егер шкала көрінбесе, нақты pH саны ойдан шығарылмайды.",
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
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Зерттелетін тағамның атауы *") },
                    placeholder = { Text("Мысалы: Пияз немесе Сүт") },
                    modifier = Modifier.fillMaxWidth().testTag("ph_product_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = sampleType,
                    onValueChange = { sampleType = it },
                    label = { Text("Үлгі түрі") },
                    placeholder = { Text("Мысалы: Пияздың судағы сығындысы") },
                    modifier = Modifier.fillMaxWidth().testTag("ph_sample_input"),
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Checkbox(
                        checked = scaleVisibleInPhoto,
                        onCheckedChange = { scaleVisibleInPhoto = it }
                    )
                    Text(
                        text = "Суретте pH түстер шкаласы бар қаптама бірге түсірілді",
                        fontSize = 12.sp,
                        color = NavyDark
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri != null) {
                        AsyncImage(
                            model = selectedUri,
                            contentDescription = "pH қағазының суреті",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "pH қағазы мен шкаланың фотосын түсіріңіз",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                        modifier = Modifier.weight(1f).height(48.dp).testTag("ph_camera_button"),
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
                        modifier = Modifier.weight(1f).height(48.dp).testTag("ph_gallery_button"),
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
                        if (productName.isBlank()) {
                            Toast.makeText(context, "Зерттелетін тағам атауын енгізіңіз.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val uri = selectedUri
                        if (uri == null) {
                            Toast.makeText(context, "pH қағазының суретін таңдаңыз.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val px = VisionAnalysisHelper.inspectImageUri(context, uri)
                        paperColorText = px?.centerToneKz ?: "анықтау мүмкін болмады"
                        estimatedPhRange = "анықтау мүмкін болмады"
                        confidenceLevel = "Төмен"
                        measuredAt = VisionAnalysisHelper.nowFormatted()
                        acidityExplanation = if (!scaleVisibleInPhoto) {
                            "Суретте pH түстер шкаласы белгіленбеді. Нақты pH санын ойдан шығаруға болмайды — қағазды pH шкаласымен бірге қайта суретке түсіріңіз."
                        } else {
                            "Құрылғыда бұлттық Gemini Vision AI өшірулі болғандықтан, қағаз ортасының RGB түсі ғана алынды. Қаптама шкаласына қарап шамамен pH аралығын төменде қолмен растаңыз."
                        }
                        errorMarginNote = "Жарық жағдайына байланысты ықтимал қателік (±0.5–1.0 pH). Зертханалық pH-метр емес, шамамен бағалау."
                        hasDraft = true
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("ph_analyze_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Icon(Icons.Default.Science, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("pH индикатор қағазын талдау", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (hasDraft) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("pH ТАЛДАУ НӘТИЖЕСІ (ШАМАМЕН БАҒАЛАУ)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Өнім: $productName", fontSize = 12.sp)
                    Text("• Үлгі түрі: ${sampleType.ifBlank { "Көрсетілмеген" }}", fontSize = 12.sp)
                    Text("• Индикатор түсі: $paperColorText", fontSize = 12.sp)
                    Text("• Өлшеу уақыты: $measuredAt • Сенімділік: $confidenceLevel", fontSize = 12.sp, color = Color(0xFF64748B))

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = estimatedPhRange,
                        onValueChange = { estimatedPhRange = it },
                        label = { Text("Болжамды pH мәні/аралығы (тек шкала бойынша салыстырсаңыз)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = acidityExplanation,
                        onValueChange = { acidityExplanation = it },
                        label = { Text("Қышқыл / бейтарап / сілтілі орта түсіндірмесі") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ықтимал қателік: $errorMarginNote", fontSize = 11.sp, color = Color(0xFF92400E))

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val isMeasured = estimatedPhRange.isNotBlank() && estimatedPhRange != "анықтау мүмкін болмады"
                            viewModel.saveChemicalPhRecord(
                                ChemicalPhRecord(
                                    productName = productName.trim(),
                                    sampleType = sampleType.ifBlank { "Көрсетілмеген" },
                                    paperColor = paperColorText,
                                    scaleVisible = scaleVisibleInPhoto,
                                    estimatedPhRange = if (isMeasured) estimatedPhRange.trim() else "анықтау мүмкін болмады",
                                    acidityExplanation = acidityExplanation,
                                    confidenceLevel = confidenceLevel,
                                    errorMarginNote = errorMarginNote,
                                    imageUri = selectedUri?.toString(),
                                    dataSource = if (isMeasured) DataSourceTag.PH_PAPER.labelKz else DataSourceTag.NONE.labelKz,
                                    measuredAtText = measuredAt.ifBlank { VisionAnalysisHelper.nowFormatted() }
                                )
                            ) {
                                Toast.makeText(context, "pH өлшемі сақталды!", Toast.LENGTH_SHORT).show()
                                hasDraft = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("ph_save_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("pH нәтижесін сақтау")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text("САҚТАЛҒАН pH ЖАЗБАЛАРЫ (${records.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(8.dp))

        records.forEach { rec ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${rec.productName} — ${rec.estimatedPhRange}", fontWeight = FontWeight.Bold, color = NavyDark)
                        Text("${rec.sampleType} • ${rec.measuredAtText}", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(rec.dataSource, fontSize = 11.sp, color = Color(0xFF7C3AED))
                    }
                    IconButton(onClick = { viewModel.deleteChemicalPhRecord(rec) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Өшіру", tint = Color(0xFFDC2626))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        MedicalDisclaimerCard()
    }
}
