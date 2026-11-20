package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.FreshGreenMuted
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PwaGuideScreen(
    modifier: Modifier = Modifier
) {
    var showLiveWebView by remember { mutableStateOf(false) }
    var previewTitle by remember { mutableStateOf("FOODSCAN AI 2.0 — Responsive PWA алдын ала қарау") }
    var previewAssetPath by remember { mutableStateOf("file:///android_asset/pwa/index.html") }

    BackHandler(enabled = showLiveWebView) {
        showLiveWebView = false
    }

    if (showLiveWebView) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .testTag("pwa_webview_container")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyDark)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = previewTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = { showLiveWebView = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Жабу", fontSize = 11.sp, color = FreshGreenMuted)
                }
            }
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = true
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        loadUrl(previewAssetPath)
                    }
                },
                update = { webView ->
                    if (webView.url != previewAssetPath) {
                        webView.loadUrl(previewAssetPath)
                    }
                }
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("pwa_guide_screen")
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = NavyDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(NavyDark, NavyPrimary)))
                    .padding(18.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = FreshGreen
                ) {
                    Text(
                        text = "ANDROID + iOS (iPHONE SAFARI) PWA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Әмбебап Responsive PWA нұсқасы",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "FOODSCAN AI жобасының Android Chrome және iPhone/iPad Safari браузерлеріне бейімделген Progressive Web App бумасы (pwa/manifest.json, service-worker.js, apple-touch-icon) жобаға қосылды.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        previewTitle = "FOODSCAN AI 2.0 — Responsive PWA алдын ала қарау"
                        previewAssetPath = "file:///android_asset/pwa/index.html"
                        showLiveWebView = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("open_pwa_preview_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PWA веб-интерфейсін ашып көру", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        previewTitle = "FreshGuard iOS — Тағам балғындығы (iPhone)"
                        previewAssetPath = "file:///android_asset/pwa/freshness/index.html"
                        showLiveWebView = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("open_freshguard_ios_preview_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FreshGuard iOS (Тағам балғындығы) ашып көру",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        GuideInfoCard(
            title = "iPhone (iOS Safari) құрылғысында орнату",
            icon = Icons.Default.PhoneIphone,
            color = AccentCyan,
            body = "1. Хостингтегі (HTTPS) PWA мекенжайын iPhone-дағы Safari браузерімен ашыңыз.\n" +
                    "2. Төменгі панельдегі «Бөлісу» (Share) батырмасын басыңыз.\n" +
                    "3. «Басты экранға қосу» (Add to Home Screen) → «Қосу» таңдаңыз.\n" +
                    "4. Safari safe-area, 16px енгізу өрістері (авто-зумсыз) және <input capture=\"environment\"> камерасы толық қолдау табады."
        )

        Spacer(modifier = Modifier.height(10.dp))

        GuideInfoCard(
            title = "Android (Chrome) құрылғысында орнату",
            icon = Icons.Default.InstallMobile,
            color = FreshGreen,
            body = "1. Chrome браузерінде PWA мекенжайын ашыңыз.\n" +
                    "2. Жоғарғы панельдегі «Орнату» түймесін немесе Chrome мәзірінен «Қосымшаны орнату» (Install app) батырмасын басыңыз.\n" +
                    "3. Қосымша жеке терезеде (standalone) белгішемен орнатылады."
        )

        Spacer(modifier = Modifier.height(10.dp))

        GuideInfoCard(
            title = "iPad және компьютер экрандарына бейімделу",
            icon = Icons.Default.Devices,
            color = NavyPrimary,
            body = "• Смартфон (< 600dp / < 768px): төменгі навигация панелі.\n" +
                    "• Планшет (iPad / Android Tablet) және компьютер: сол жақ бүйірлік NavigationRail мәзірі және орталықтандырылған ықшам мазмұн."
        )

        Spacer(modifier = Modifier.height(10.dp))

        GuideInfoCard(
            title = "API кілттері мен деректер қауіпсіздігі",
            icon = Icons.Default.Security,
            color = Color(0xFF7C3AED),
            body = "PWA нұсқасында GEMINI_API_KEY тек серверлік проксиде (pwa/api/analyze.js) сақталады және клиенттік кодта ешқашан ашылмайды. Android нұсқасында бұлттық AI үшін Firebase App Check қолданылады."
        )

        Spacer(modifier = Modifier.height(14.dp))
        MedicalDisclaimerCard()
    }
}

@Composable
private fun GuideInfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyDark)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(body, fontSize = 12.sp, color = Color(0xFF334155), lineHeight = 17.sp)
        }
    }
}
