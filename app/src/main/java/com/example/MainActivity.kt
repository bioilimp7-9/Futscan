package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.FoodScanBottomNav
import com.example.ui.components.FoodScanNavigationRail
import com.example.ui.components.FoodScanTopBar
import com.example.ui.screens.AboutResearchScreen
import com.example.ui.screens.ChemicalPhScreen
import com.example.ui.screens.ComparisonScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EducationScreen
import com.example.ui.screens.FreshnessVisionScreen
import com.example.ui.screens.PwaGuideScreen
import com.example.ui.screens.ResearchProtocolScreen
import com.example.ui.screens.ResearchScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.StorageMonitorScreen
import com.example.ui.screens.SurveyScreen
import com.example.ui.screens.UnifiedReportScreen
import com.example.ui.theme.FoodScanAiTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FoodScanAiTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()

                // BackHandler to handle custom backstack
                BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
                    viewModel.navigateBack()
                }

                val screenTitle = when (currentScreen) {
                    AppScreen.DASHBOARD -> "FOODSCAN AI 2.0"
                    AppScreen.SCANNER -> "Құрамын сканерлеу"
                    AppScreen.FRESHNESS_VISION -> "Балғындықты талдау"
                    AppScreen.CHEMICAL_PH -> "Химиялық pH талдау"
                    AppScreen.STORAGE_MONITOR -> "Сақтау жағдайы"
                    AppScreen.UNIFIED_REPORT -> "Біріктірілген есеп (A–D)"
                    AppScreen.COMPARISON -> "Өнімдерді салыстыру"
                    AppScreen.RESEARCH -> "Ғылыми зертхана"
                    AppScreen.RESEARCH_PROTOCOL -> "Зерттеу хаттамасы (6 парақ)"
                    AppScreen.SURVEY -> "Оқушылар сауалнамасы"
                    AppScreen.EDUCATION -> "Тағам туралы білім"
                    AppScreen.ABOUT_RESEARCH -> "Зерттеу жобасы"
                    AppScreen.PWA_GUIDE -> "PWA (Android & iOS)"
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isExpandedScreen = maxWidth >= 600.dp

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets.safeDrawing,
                        topBar = {
                            FoodScanTopBar(
                                title = screenTitle,
                                canNavigateBack = currentScreen != AppScreen.DASHBOARD,
                                onNavigateBack = { viewModel.navigateBack() },
                                actions = {
                                    if (currentScreen != AppScreen.PWA_GUIDE) {
                                        IconButton(
                                            onClick = { viewModel.navigateTo(AppScreen.PWA_GUIDE) },
                                            modifier = Modifier.testTag("topbar_pwa_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Devices,
                                                contentDescription = "Android & iOS PWA",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                    if (currentScreen != AppScreen.ABOUT_RESEARCH) {
                                        IconButton(
                                            onClick = { viewModel.navigateTo(AppScreen.ABOUT_RESEARCH) },
                                            modifier = Modifier.testTag("topbar_about_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.School,
                                                contentDescription = "Жоба туралы",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            if (!isExpandedScreen) {
                                FoodScanBottomNav(
                                    currentScreen = currentScreen,
                                    onScreenSelected = { screen -> viewModel.navigateTo(screen) }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (isExpandedScreen) {
                                FoodScanNavigationRail(
                                    currentScreen = currentScreen,
                                    onScreenSelected = { screen -> viewModel.navigateTo(screen) }
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                val contentModifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 840.dp)

                                when (currentScreen) {
                                    AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel, modifier = contentModifier)
                                    AppScreen.SCANNER -> ScannerScreen(viewModel = viewModel, modifier = contentModifier)
                                    AppScreen.FRESHNESS_VISION -> FreshnessVisionScreen(viewModel = viewModel, modifier = contentModifier)
                                    AppScreen.CHEMICAL_PH -> ChemicalPhScreen(viewModel = viewModel, modifier = contentModifier)
                                    AppScreen.STORAGE_MONITOR -> StorageMonitorScreen(viewModel = viewModel, modifier = contentModifier)
                                    AppScreen.UNIFIED_REPORT -> UnifiedReportScreen(viewModel = viewModel, modifier = contentModifier)
                                    AppScreen.COMPARISON -> ComparisonScreen(viewModel = viewModel, modifier = contentModifier)
                                    AppScreen.RESEARCH -> ResearchScreen(viewModel = viewModel, modifier = contentModifier)
                                    AppScreen.RESEARCH_PROTOCOL -> ResearchProtocolScreen(viewModel = viewModel, modifier = contentModifier)
                                    AppScreen.SURVEY -> SurveyScreen(viewModel = viewModel, modifier = contentModifier)
                                    AppScreen.EDUCATION -> EducationScreen(modifier = contentModifier)
                                    AppScreen.ABOUT_RESEARCH -> AboutResearchScreen(modifier = contentModifier)
                                    AppScreen.PWA_GUIDE -> PwaGuideScreen(modifier = contentModifier)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
