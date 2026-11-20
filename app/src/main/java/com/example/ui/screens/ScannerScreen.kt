package com.example.ui.screens

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.MeasurementBasis
import com.example.data.model.ProductItem
import com.example.data.samples.SampleData
import com.example.scanner.ExtractionResult
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.ScanUiState
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.components.ScientificNutrientCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentRed
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.FreshGreenDark
import com.example.ui.theme.FreshGreenMuted
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SlateBorder
import java.io.File
import java.io.FileOutputStream

@Composable
fun ScannerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scanState by viewModel.scanState.collectAsState()
    val selectedUri by viewModel.selectedImageUri.collectAsState()
    val selectedPresetId by viewModel.selectedPresetId.collectAsState()

    // 1. Gallery Picker (Android Photo Picker)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setImageUri(uri)
            Toast.makeText(context, "Фото таңдалды. «Талдау» түймесін басыңыз.", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Camera Capture Launcher (Native Android Camera)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val photoFile = File(context.cacheDir, "foodscan_camera_${System.currentTimeMillis()}.jpg")
                FileOutputStream(photoFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                val uri = Uri.fromFile(photoFile)
                viewModel.setImageUri(uri)
                Toast.makeText(context, "Камера фотосы қабылданды! Енді талдаңыз.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Суретті сақтау қатесі: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Camera Permission Requester
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Камераға рұқсат берілмеді. Баптаулардан рұқсат беріңіз немесе галереяны пайдаланыңыз.", Toast.LENGTH_LONG).show()
        }
    }

    // OCR transcription / text input helper state
    var ocrCustomText by remember { mutableStateOf("") }

    // Manual OCR Fallback Dialog
    var showManualInputDialog by remember { mutableStateOf(false) }
    var manualLabelText by remember { mutableStateOf("") }

    // Edit and Confirm Dialog before saving
    var showEditConfirmDialog by remember { mutableStateOf(false) }
    var resultToSave by remember { mutableStateOf<ExtractionResult?>(null) }

    // Guided Post-Save Flow Dialog (Scan -> Verify -> Save -> Compare)
    var showSavedFlowDialog by remember { mutableStateOf(false) }
    var recentlySavedProduct by remember { mutableStateOf<ProductItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("scanner_screen")
    ) {
        // HEADER TITLE
        Text(
            text = "ТАҒАМ ЖАПСЫРМАСЫН СЕМАНТИКАЛЫҚ ТАЛДАУ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 1.sp
        )
        Text(
            text = "Өнімнің фотосын жүктеу",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NavyDark
        )
        Text(
            text = "Телефон камерасымен қаптаманы түсіріңіз, галереядан жүктеңіз немесе дайын үлгілерді пайдаланыңыз.",
            fontSize = 13.sp,
            color = Color(0xFF475569)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // UPLOAD & PREVIEW CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("image_upload_container"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SlateBorder, Color(0xFFCBD5E1))))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (selectedUri != null) {
                    // PREVIEW OF USER UPLOADED / CAMERA IMAGE
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = selectedUri,
                            contentDescription = "Жүктелген өнім фотосы",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Close / delete photo button
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    viewModel.clearImage()
                                    ocrCustomText = ""
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Өшіру", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                } else if (selectedPresetId != null) {
                    // PREVIEW OF PRESET
                    val sample = SampleData.samplePackages.find { it.id == selectedPresetId }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0F172A), NavyPrimary)
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Cookie, contentDescription = null, tint = FreshGreenMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = sample?.name ?: "Үлгі өнім",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${sample?.category} • ${sample?.manufacturer}",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                } else {
                    // EMPTY STATE
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Қаптама суреті жүктелмеген",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "Камерамен түсіріңіз, галереядан таңдаңыз немесе үлгіні таңдаңыз",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ACTIONS ROW: CAMERA, GALLERY, DIRECT TEXT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Native Camera Button
                    Button(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("upload_camera_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Камера", fontSize = 12.sp)
                    }

                    // Gallery Button
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("upload_gallery_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Галерея", fontSize = 12.sp)
                    }

                    // Direct text button
                    OutlinedButton(
                        onClick = { showManualInputDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_entry_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Мәтін енгізу", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PRESET SAMPLES QUICK SELECTOR
        Text(
            text = "НЕГІЗГІ СЫНАҚ ҮЛГІЛЕРІ (ҚАЗАҚСТАН ТАҒАМДАРЫ):",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(SampleData.samplePackages) { sample ->
                val isSelected = selectedPresetId == sample.id
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) FreshGreen else Color.White,
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .clickable { viewModel.selectPreset(sample.id) }
                        .testTag("preset_${sample.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sample.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else NavyDark
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // OPTIONAL OCR TEXT INPUT (If photo is loaded)
        if (selectedUri != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Қаптамадағы тағамдық мәтін (OCR тану немесе енгізу):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = ocrCustomText,
                        onValueChange = { ocrCustomText = it },
                        placeholder = { Text("Мысалы: 100 г: Ақуыз 6г, Май 14г, Қант 28г, Тұз 0.4г, 390 ккал...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        maxLines = 3
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // ANALYZE BUTTON
        if (selectedUri != null && scanState !is ScanUiState.Scanning) {
            Button(
                onClick = { viewModel.startAnalysis(ocrCustomText.ifBlank { null }) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_analysis_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Өнім жапсырмасын талдау", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // SCANNING PROGRESS ANIMATION
        if (scanState is ScanUiState.Scanning) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scanning_progress_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = FreshGreen)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Семантикалық OCR және тағамдық талдау жүруде...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Қант, тұз, май, ақуыз және калория мөлшері анықталуда",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = FreshGreen
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ANALYSIS RESULTS DISPLAY
        if (scanState is ScanUiState.ResultReady) {
            val result = (scanState as ScanUiState.ResultReady).result
            resultToSave = result

            Text(
                text = "ТАЛДАУ НӘТИЖЕЛЕРІ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = FreshGreen,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            // PRODUCT SUMMARY CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_summary_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = result.productName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Санаты: ${result.category}",
                                fontSize = 12.sp,
                                color = FreshGreenMuted
                            )
                            if (result.manufacturer.isNotBlank()) {
                                Text(
                                    text = "Өндіруші: ${result.manufacturer}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }

                        // Measurement Basis Badge (100g vs serving)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (result.measurementBasis == MeasurementBasis.PER_100G) FreshGreen else AccentCyan
                        ) {
                            Text(
                                text = result.measurementBasis.titleKz,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Порция көлемі: ${result.servingSizeText}",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    if (result.ingredients.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Құрамы: ${result.ingredients}",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 15.sp,
                            maxLines = 3
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (result.isManualFallback) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firebase_ai_notice_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFFFCD34D), Color(0xFFF59E0B)))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Firebase AI ескертуі",
                            tint = AccentAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Бұлттық AI тану режимі өшірулі (Firebase қосылмаған)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Android нұсқасында фотосуреттен автоматты Gemini AI талдауы Firebase AI Logic қызметін талап етеді. Мәндер ойдан шығарылмайды — төмендегі «Мәндерді түзету» түймесі арқылы қаптамадағы нақты сандарды енгізіңіз немесе «Қаптама мәтінін» қолданыңыз.",
                                fontSize = 12.sp,
                                color = Color(0xFF78350F),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 5 SCIENTIFIC INDICATOR CARDS
            ScientificNutrientCard(
                title = "1. Қант мөлшері",
                value = result.sugar.value,
                unit = result.sugar.unit,
                isMissing = result.sugar.isMissing,
                note = result.sugar.educationalNote,
                originalSnippet = result.sugar.originalSnippet,
                icon = Icons.Default.Grain,
                accentColor = Color(0xFFEC4899)
            )
            Spacer(modifier = Modifier.height(8.dp))

            ScientificNutrientCard(
                title = "2. Тұз мөлшері",
                value = result.salt.value,
                unit = result.salt.unit,
                isMissing = result.salt.isMissing,
                note = result.salt.educationalNote,
                originalSnippet = result.salt.originalSnippet,
                icon = Icons.Default.Opacity,
                accentColor = Color(0xFF3B82F6)
            )
            Spacer(modifier = Modifier.height(8.dp))

            ScientificNutrientCard(
                title = "3. Май мөлшері",
                value = result.fat.value,
                unit = result.fat.unit,
                isMissing = result.fat.isMissing,
                note = result.fat.educationalNote,
                originalSnippet = result.fat.originalSnippet,
                icon = Icons.Default.Egg,
                accentColor = AccentAmber
            )
            Spacer(modifier = Modifier.height(8.dp))

            ScientificNutrientCard(
                title = "4. Ақуыз мөлшері",
                value = result.protein.value,
                unit = result.protein.unit,
                isMissing = result.protein.isMissing,
                note = result.protein.educationalNote,
                originalSnippet = result.protein.originalSnippet,
                icon = Icons.Default.FitnessCenter,
                accentColor = FreshGreen
            )
            Spacer(modifier = Modifier.height(8.dp))

            ScientificNutrientCard(
                title = "5. Энергетикалық құндылығы",
                value = result.calories.value,
                unit = result.calories.unit,
                isMissing = result.calories.isMissing,
                note = result.calories.educationalNote,
                originalSnippet = result.calories.originalSnippet,
                icon = Icons.Default.Bolt,
                accentColor = Color(0xFF8B5CF6)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // MACRONUTRIENT VISUAL DISTRIBUTION CHART
            MacronutrientBarChart(
                sugar = result.sugar.value ?: 0f,
                fat = result.fat.value ?: 0f,
                protein = result.protein.value ?: 0f,
                basisText = result.measurementBasis.titleKz
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ACTIONS: EDIT & CONFIRM, SAVE TO RESEARCH
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showEditConfirmDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("edit_extracted_values_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Мәндерді түзету", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        viewModel.saveAnalyzedToResearch(
                            name = result.productName,
                            category = result.category,
                            basis = result.measurementBasis,
                            sugar = result.sugar.value,
                            salt = result.salt.value,
                            fat = result.fat.value,
                            protein = result.protein.value,
                            calories = result.calories.value,
                            extractedSugar = result.sugar.value,
                            extractedSalt = result.salt.value,
                            extractedFat = result.fat.value,
                            extractedProtein = result.protein.value,
                            extractedCalories = result.calories.value,
                            notes = "FOODSCAN AI сканері арқылы талданды."
                        ) { success, item ->
                            if (success) {
                                recentlySavedProduct = item
                                showSavedFlowDialog = true
                            } else {
                                Toast.makeText(context, "Зертхана лимиті (30 өнім) толды!", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(50.dp)
                        .testTag("save_to_research_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                ) {
                    Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Зертханаға сақтау", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // MEDICAL DISCLAIMER CARD AT BOTTOM
        MedicalDisclaimerCard()

        // DIALOG 1: MANUAL OCR INPUT
        if (showManualInputDialog) {
            AlertDialog(
                onDismissRequest = { showManualInputDialog = false },
                title = { Text("Қаптама мәтінін қолмен енгізу", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Қаптамадағы тағамдық құндылық мәтінін көшіріп қойыңыз немесе жазыңыз:",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = manualLabelText,
                            onValueChange = { manualLabelText = it },
                            placeholder = { Text("Мысалы: 100 г: Ақуыз 5 г, Май 10 г, Қант 20 г, Тұз 0.2 г, 350 ккал...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .testTag("manual_label_input"),
                            maxLines = 5
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (manualLabelText.isNotBlank()) {
                                viewModel.startAnalysis(manualLabelText)
                                showManualInputDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                    ) {
                        Text("Талдау")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showManualInputDialog = false }) {
                        Text("Бас тарту")
                    }
                }
            )
        }

        // DIALOG 2: EDIT EXTRACTED VALUES BEFORE CONFIRMING & SAVING
        if (showEditConfirmDialog && resultToSave != null) {
            val res = resultToSave!!
            var editName by remember { mutableStateOf(res.productName) }
            var editSugar by remember { mutableStateOf(res.sugar.value?.toString() ?: "") }
            var editSalt by remember { mutableStateOf(res.salt.value?.toString() ?: "") }
            var editFat by remember { mutableStateOf(res.fat.value?.toString() ?: "") }
            var editProtein by remember { mutableStateOf(res.protein.value?.toString() ?: "") }
            var editCal by remember { mutableStateOf(res.calories.value?.toString() ?: "") }

            AlertDialog(
                onDismissRequest = { showEditConfirmDialog = false },
                title = { Text("Мәндерді қаптама бойынша түзету", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "Оқушы қаптамадағы нақты мәндерді тексеріп, қолмен растайды:",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Өнім атауы") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = editSugar,
                            onValueChange = { editSugar = it },
                            label = { Text("Қант мөлшері (г)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = editSalt,
                            onValueChange = { editSalt = it },
                            label = { Text("Тұз мөлшері (г)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = editFat,
                            onValueChange = { editFat = it },
                            label = { Text("Май мөлшері (г)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = editProtein,
                            onValueChange = { editProtein = it },
                            label = { Text("Ақуыз мөлшері (г)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = editCal,
                            onValueChange = { editCal = it },
                            label = { Text("Калория (ккал)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.saveAnalyzedToResearch(
                                name = editName,
                                category = res.category,
                                basis = res.measurementBasis,
                                sugar = editSugar.toFloatOrNull(),
                                salt = editSalt.toFloatOrNull(),
                                fat = editFat.toFloatOrNull(),
                                protein = editProtein.toFloatOrNull(),
                                calories = editCal.toFloatOrNull(),
                                extractedSugar = res.sugar.value,
                                extractedSalt = res.salt.value,
                                extractedFat = res.fat.value,
                                extractedProtein = res.protein.value,
                                extractedCalories = res.calories.value,
                                notes = "Оқушы тарапынан қолмен тексеріліп түзетілді."
                            ) { success, item ->
                                showEditConfirmDialog = false
                                if (success) {
                                    recentlySavedProduct = item
                                    showSavedFlowDialog = true
                                } else {
                                    Toast.makeText(context, "Зертхана лимиті (30 өнім) толды!", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                    ) {
                        Text("Растау және сақтау")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditConfirmDialog = false }) {
                        Text("Бас тарту")
                    }
                }
            )
        }

        // DIALOG 3: GUIDED FLOW AFTER SAVING (Scan -> Verify -> Save -> Compare)
        if (showSavedFlowDialog && recentlySavedProduct != null) {
            val saved = recentlySavedProduct!!
            AlertDialog(
                onDismissRequest = { showSavedFlowDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = FreshGreen,
                        modifier = Modifier.size(44.dp)
                    )
                },
                title = {
                    Text(
                        text = "Өнім сәтті сақталды!",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "«${saved.productName}» (#${saved.productNumber}) ғылыми зертхана дерекқорына сәтті қосылды.",
                            fontSize = 13.sp,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Compare button (direct next step in user flow!)
                        Button(
                            onClick = {
                                showSavedFlowDialog = false
                                viewModel.selectForComparison1(saved)
                                viewModel.navigateTo(AppScreen.COMPARISON)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("flow_compare_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Басқа өніммен салыстыру", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                showSavedFlowDialog = false
                                viewModel.navigateTo(AppScreen.RESEARCH)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("flow_view_research_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Зертхана тізімін көру")
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        TextButton(
                            onClick = {
                                showSavedFlowDialog = false
                                viewModel.clearImage()
                                ocrCustomText = ""
                            }
                        ) {
                            Text("Тағы бір өнімді сканерлеу", color = Color(0xFF64748B))
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }
    }
}

@Composable
fun MacronutrientBarChart(
    sugar: Float,
    fat: Float,
    protein: Float,
    basisText: String,
    modifier: Modifier = Modifier
) {
    val total = (sugar + fat + protein).coerceAtLeast(0.1f)
    val sugarPct = (sugar / total) * 100f
    val fatPct = (fat / total) * 100f
    val proteinPct = (protein / total) * 100f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "МАКРОНУТРИЕНТТЕР ҚАТЫНАСЫ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = basisText,
                    fontSize = 11.sp,
                    color = FreshGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Composite visual bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                if (sugar > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(sugar.coerceAtLeast(0.01f))
                            .fillMaxSize()
                            .background(Color(0xFFEC4899))
                    )
                }
                if (fat > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(fat.coerceAtLeast(0.01f))
                            .fillMaxSize()
                            .background(AccentAmber)
                    )
                }
                if (protein > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(protein.coerceAtLeast(0.01f))
                            .fillMaxSize()
                            .background(FreshGreen)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendItem(label = "Қант: ${String.format(java.util.Locale.US, "%.1fg (%.0f%%)", sugar, sugarPct)}", color = Color(0xFFEC4899))
                LegendItem(label = "Май: ${String.format(java.util.Locale.US, "%.1fg (%.0f%%)", fat, fatPct)}", color = AccentAmber)
                LegendItem(label = "Ақуыз: ${String.format(java.util.Locale.US, "%.1fg (%.0f%%)", protein, proteinPct)}", color = FreshGreen)
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
    }
}
