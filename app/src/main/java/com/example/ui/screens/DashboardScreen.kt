package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.FreshGreenDark
import com.example.ui.theme.FreshGreenMuted
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SlateBorder

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val sessionCount by viewModel.sessionScanCount.collectAsState()
    val products by viewModel.productsList.collectAsState()
    val metrics by viewModel.metrics.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dashboard_screen")
    ) {
        // 1. BRAND HERO HEADER & LOGO
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("brand_hero_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyPrimary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(NavyPrimary, NavyDark)
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Unique FOODSCAN AI Logo: Food leaf + Scanning frame + AI digital symbol
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(FreshGreen, FreshGreenDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "FOODSCAN AI Logo",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FOODSCAN AI 2.0",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FreshGreen
                            ) {
                                Text(
                                    text = "SMART",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "SMART FOOD ANALYSIS SYSTEM",
                            fontSize = 12.sp,
                            color = FreshGreenMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Тағамды кешенді талдау жүйесі",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Қоректік құрамды, сыртқы визуалды белгілерді, химиялық pH көрсеткіштерін және сақтау жағдайларын бір цифрлық жүйеде жинау, талдау және салыстыру.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. STATISTICS PANEL
        Text(
            text = "АҒЫМДАҒЫ СЕССИЯ ЖӘНЕ ЗЕРТХАНА СТАТИСТИКАСЫ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Осы сессияда",
                value = "$sessionCount өнім",
                subtitle = "сканерленді",
                icon = Icons.Default.QrCodeScanner,
                color = AccentCyan,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Зертханада",
                value = "${products.size} / 30",
                subtitle = "өнім жазылды",
                icon = Icons.Default.Science,
                color = FreshGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "OCR дәлдігі",
                value = String.format(java.util.Locale.US, "%.1f%%", metrics?.overallAccuracyPercentage ?: 100f),
                subtitle = "тексерілген",
                icon = Icons.Default.CheckCircle,
                color = AccentAmber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. PRIMARY MENU: FOUR CORE MODULES
        Text(
            text = "НЕГІЗГІ ТАЛДАУ МОДУЛЬДЕРІ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.navigateTo(AppScreen.SCANNER) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("scan_product_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "1. Құрамын сканерлеу",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionTile(
                title = "2. Балғындықты талдау",
                description = "Камера арқылы сыртқы белгілерді бағалау",
                icon = Icons.Default.Visibility,
                iconColor = AccentCyan,
                onClick = { viewModel.navigateTo(AppScreen.FRESHNESS_VISION) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("freshness_vision_button")
            )
            ActionTile(
                title = "3. Химиялық pH талдау",
                description = "Индикатор қағазы мен шкаланы салыстыру",
                icon = Icons.Default.Science,
                iconColor = Color(0xFF7C3AED),
                onClick = { viewModel.navigateTo(AppScreen.CHEMICAL_PH) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chemical_ph_button")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionTile(
                title = "4. Сақтау жағдайы",
                description = "Температура, ылғалдылық және жарық енгізу",
                icon = Icons.Default.Thermostat,
                iconColor = NavyPrimary,
                onClick = { viewModel.navigateTo(AppScreen.STORAGE_MONITOR) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("storage_monitor_button")
            )
            ActionTile(
                title = "Біріктірілген есеп (A–D)",
                description = "Барлық 4 модуль және күндер бойынша динамика",
                icon = Icons.Default.FactCheck,
                iconColor = FreshGreenDark,
                onClick = { viewModel.navigateTo(AppScreen.UNIFIED_REPORT) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("unified_report_button")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. SECONDARY ACTION CARDS (GRID/COLUMN)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionTile(
                title = "Өнімдерді салыстыру",
                description = "2 өнім құрамын қатар салыстыру кестесі",
                icon = Icons.Default.BarChart,
                iconColor = AccentCyan,
                onClick = { viewModel.navigateTo(AppScreen.COMPARISON) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("compare_products_button")
            )

            ActionTile(
                title = "Менің зерттеуім",
                description = "Ғылыми зертхана (30 өнім, дәлдік, CSV)",
                icon = Icons.Default.Science,
                iconColor = NavyPrimary,
                onClick = { viewModel.navigateTo(AppScreen.RESEARCH) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("my_research_button")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionTile(
                title = "Тағам туралы білім",
                description = "Ақуыз, қант, тұз және калория анықтамалығы",
                icon = Icons.Default.MenuBook,
                iconColor = FreshGreen,
                onClick = { viewModel.navigateTo(AppScreen.EDUCATION) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("nutrition_knowledge_button")
            )

            ActionTile(
                title = "Жасөспірімдер сауалнамасы",
                description = "5 сұрақтан тұратын әлеуметтік сауалнама",
                icon = Icons.Default.Assignment,
                iconColor = AccentAmber,
                onClick = { viewModel.navigateTo(AppScreen.SURVEY) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("teen_survey_button")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4.5. UNIVERSAL PWA (ANDROID & iPHONE SAFARI) CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateTo(AppScreen.PWA_GUIDE) }
                .testTag("dashboard_pwa_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(FreshGreenMuted, FreshGreen)))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(FreshGreen.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = "Android және iPhone PWA",
                        tint = FreshGreenDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Android + iPhone (Safari) PWA нұсқасы",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Text(
                        text = "Басты экранға орнату нұсқаулығы және веб-қосымшаны ашып көру",
                        fontSize = 11.sp,
                        color = Color(0xFF065F46)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Ашу",
                    tint = FreshGreenDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4.6. 6-SHEET RESEARCH PROTOCOL ENTRY CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateTo(AppScreen.RESEARCH_PROTOCOL) }
                .testTag("dashboard_protocol_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AccentCyan, NavyPrimary)))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentCyan.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FactCheck,
                        contentDescription = "Зерттеу хаттамасы",
                        tint = NavyPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Зерттеу хаттамасы (6 парақ)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Text(
                        text = "Нұсқаулық • 30 өнім • Оқушы тесті • 8 сұрақ • Жауап кілті • Қорытынды",
                        fontSize = 11.sp,
                        color = Color(0xFF1E3A8A)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Ашу",
                    tint = NavyPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. 8th GRADE STUDENT RESEARCH CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateTo(AppScreen.ABOUT_RESEARCH) }
                .testTag("research_about_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SlateBorder, FreshGreenMuted)))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Зерттеу",
                        tint = NavyPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "8-СЫНЫП ОҚУШЫСЫНЫҢ ЗЕРТТЕУ ЖОБАСЫ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Қанағат Айым (8 «А» сынып)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Text(
                        text = "«Жасөспірімдерге арналған тағам өнімдерінің құрамын цифрлық талдау жүйесін әзірлеу және оның ақпараттық тиімділігін зерттеу»",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        maxLines = 2
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Толығырақ",
                    tint = FreshGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. MEDICAL DISCLAIMER
        MedicalDisclaimerCard()
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NavyDark
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NavyDark,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                lineHeight = 15.sp,
                maxLines = 2
            )
        }
    }
}
