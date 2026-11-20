package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkBg
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.FreshGreenDark
import com.example.ui.theme.FreshGreenMuted
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SlateBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScanTopBar(
    title: String,
    slogan: String? = "SMART FOOD ANALYSIS SYSTEM",
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Unique FOODSCAN AI Mini Logo
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(FreshGreen, FreshGreenDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "FOODSCAN Logo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!slogan.isNullOrBlank()) {
                    Text(
                        text = slogan,
                        fontSize = 11.sp,
                        color = FreshGreenMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("nav_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Артқа қайту",
                        tint = Color.White
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = NavyPrimary,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

@Composable
fun MedicalDisclaimerCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("medical_disclaimer_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFF6FF)
        ),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFFBFDBFE), Color(0xFF93C5FD))))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDBEAFE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Ақпарат",
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "МЕДИЦИНАЛЫҚ ЕСКЕРТУ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E40AF)
                )
                Text(
                    text = "FOODSCAN AI — білім беру және ақпараттық мақсаттағы құрал. Ол медициналық кеңес бермейді.",
                    fontSize = 12.sp,
                    color = Color(0xFF1E3A8A),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun FoodScanBottomNav(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = NavyPrimary,
        contentColor = Color.White,
        modifier = Modifier.testTag("main_bottom_nav")
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.DASHBOARD,
            onClick = { onScreenSelected(AppScreen.DASHBOARD) },
            icon = { Icon(Icons.Default.Science, contentDescription = "Басты") },
            label = { Text("Басты", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                selectedTextColor = FreshGreenMuted,
                indicatorColor = FreshGreen,
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_item_dashboard")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.SCANNER,
            onClick = { onScreenSelected(AppScreen.SCANNER) },
            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Сканер") },
            label = { Text("Сканер", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                selectedTextColor = FreshGreenMuted,
                indicatorColor = FreshGreen,
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_item_scanner")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.COMPARISON,
            onClick = { onScreenSelected(AppScreen.COMPARISON) },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Салыстыру") },
            label = { Text("Салыстыру", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                selectedTextColor = FreshGreenMuted,
                indicatorColor = FreshGreen,
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_item_comparison")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.RESEARCH,
            onClick = { onScreenSelected(AppScreen.RESEARCH) },
            icon = { Icon(Icons.Default.Science, contentDescription = "Зертхана") },
            label = { Text("Зертхана", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                selectedTextColor = FreshGreenMuted,
                indicatorColor = FreshGreen,
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_item_research")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.EDUCATION,
            onClick = { onScreenSelected(AppScreen.EDUCATION) },
            icon = { Icon(Icons.Default.MenuBook, contentDescription = "Білім") },
            label = { Text("Білім", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                selectedTextColor = FreshGreenMuted,
                indicatorColor = FreshGreen,
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            ),
            modifier = Modifier.testTag("nav_item_education")
        )
    }
}

@Composable
fun FoodScanNavigationRail(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    val railColors = NavigationRailItemDefaults.colors(
        selectedIconColor = NavyDark,
        selectedTextColor = FreshGreenMuted,
        indicatorColor = FreshGreen,
        unselectedIconColor = Color(0xFF94A3B8),
        unselectedTextColor = Color(0xFF94A3B8)
    )
    NavigationRail(
        containerColor = NavyPrimary,
        contentColor = Color.White,
        modifier = Modifier
            .fillMaxHeight()
            .testTag("main_navigation_rail")
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        NavigationRailItem(
            selected = currentScreen == AppScreen.DASHBOARD,
            onClick = { onScreenSelected(AppScreen.DASHBOARD) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Басты") },
            label = { Text("Басты", fontSize = 11.sp) },
            colors = railColors,
            modifier = Modifier.testTag("rail_item_dashboard")
        )
        NavigationRailItem(
            selected = currentScreen == AppScreen.SCANNER,
            onClick = { onScreenSelected(AppScreen.SCANNER) },
            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Сканер") },
            label = { Text("Сканер", fontSize = 11.sp) },
            colors = railColors,
            modifier = Modifier.testTag("rail_item_scanner")
        )
        NavigationRailItem(
            selected = currentScreen == AppScreen.COMPARISON,
            onClick = { onScreenSelected(AppScreen.COMPARISON) },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Салыстыру") },
            label = { Text("Салыстыру", fontSize = 11.sp) },
            colors = railColors,
            modifier = Modifier.testTag("rail_item_comparison")
        )
        NavigationRailItem(
            selected = currentScreen == AppScreen.RESEARCH,
            onClick = { onScreenSelected(AppScreen.RESEARCH) },
            icon = { Icon(Icons.Default.Science, contentDescription = "Зертхана") },
            label = { Text("Зертхана", fontSize = 11.sp) },
            colors = railColors,
            modifier = Modifier.testTag("rail_item_research")
        )
        NavigationRailItem(
            selected = currentScreen == AppScreen.UNIFIED_REPORT,
            onClick = { onScreenSelected(AppScreen.UNIFIED_REPORT) },
            icon = { Icon(Icons.Default.Science, contentDescription = "Есеп") },
            label = { Text("Есеп", fontSize = 11.sp) },
            colors = railColors,
            modifier = Modifier.testTag("rail_item_unified")
        )
        NavigationRailItem(
            selected = currentScreen == AppScreen.EDUCATION,
            onClick = { onScreenSelected(AppScreen.EDUCATION) },
            icon = { Icon(Icons.Default.MenuBook, contentDescription = "Білім") },
            label = { Text("Білім", fontSize = 11.sp) },
            colors = railColors,
            modifier = Modifier.testTag("rail_item_education")
        )
        NavigationRailItem(
            selected = currentScreen == AppScreen.PWA_GUIDE,
            onClick = { onScreenSelected(AppScreen.PWA_GUIDE) },
            icon = { Icon(Icons.Default.Devices, contentDescription = "PWA") },
            label = { Text("PWA", fontSize = 11.sp) },
            colors = railColors,
            modifier = Modifier.testTag("rail_item_pwa")
        )
    }
}

@Composable
fun ScientificNutrientCard(
    title: String,
    value: Float?,
    unit: String,
    isMissing: Boolean,
    note: String,
    originalSnippet: String = "",
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("nutrient_card_${title.lowercase()}"),
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyDark
                    )
                }

                if (isMissing) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Анықталмады", tint = AccentAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Анықталмады", fontSize = 11.sp, color = AccentAmber, fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f", value ?: 0f),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unit,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isMissing) {
                Text(
                    text = "«Бұл көрсеткіш суреттен анық оқылмады. Қаптамадан қолмен тексеріңіз».",
                    fontSize = 12.sp,
                    color = Color(0xFFB45309),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
            } else {
                if (originalSnippet.isNotBlank()) {
                    Text(
                        text = "Жапсырмадағы мәтін: \"$originalSnippet\"",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = note,
                    fontSize = 12.sp,
                    color = Color(0xFF334155),
                    lineHeight = 16.sp
                )
            }
        }
    }
}
