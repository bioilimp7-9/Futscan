package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SurveyResponse
import com.example.ui.MainViewModel
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun SurveyScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val surveys by viewModel.surveyList.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Сауалнама өту, 1: Нәтижелер мен графиктер

    // Form inputs
    var ageText by remember { mutableStateOf("14") }
    var q1Selected by remember { mutableStateOf("Кейде") }
    var q2Selected by remember { mutableStateOf("Қиындықпен") }
    var q3Selected by remember { mutableStateOf("Шамамен білемін") }
    var q4Selected by remember { mutableStateOf("Кейде шатасамын") }
    var q5Selected by remember { mutableStateOf("Өте пайдалы болды") }
    var commentText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("survey_screen")
    ) {
        Text(
            text = "ЖАСӨСПІРІМДЕРДІҢ АҚПАРАТТЫҚ ТИІМДІЛІГІН ЗЕРТТЕУ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 1.sp
        )
        Text(
            text = "Жасөспірімдер сауалнамасы",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NavyDark
        )
        Text(
            text = "12–17 жастағы мектеп оқушылары арасындағы тағам сауаттылығы бойынша сауалнама.",
            fontSize = 13.sp,
            color = Color(0xFF475569)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // TAB SELECTOR
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = NavyPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .testTag("survey_tabs")
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Сауалнама толтыру", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Нәтижелер мен диаграмма (${surveys.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // SURVEY FORM
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("survey_form_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Age
                    Text(
                        text = "Оқушының жасы (12-17):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Q1
                    SurveyQuestionBlock(
                        number = 1,
                        question = "Тағам өнімін сатып алғанда құрамын оқисың ба?",
                        options = listOf("Иә, үнемі", "Кейде", "Жоқ, ешқашан"),
                        selected = q1Selected,
                        onSelect = { q1Selected = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                    // Q2
                    SurveyQuestionBlock(
                        number = 2,
                        question = "Қант мөлшерін қаптамадан таба аласың ба?",
                        options = listOf("Оңай табамын", "Қиындықпен", "Таба алмаймын"),
                        selected = q2Selected,
                        onSelect = { q2Selected = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                    // Q3
                    SurveyQuestionBlock(
                        number = 3,
                        question = "100 грамм және бір порция көрсеткіштерінің айырмашылығын білесің бе?",
                        options = listOf("Иә, жақсы білемін", "Шамамен білемін", "Білмеймін"),
                        selected = q3Selected,
                        onSelect = { q3Selected = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                    // Q4
                    SurveyQuestionBlock(
                        number = 4,
                        question = "Екі өнімнің құрамын салыстыра аласың ба?",
                        options = listOf("Иә, оңай", "Кейде шатасамын", "Жоқ, қиналамын"),
                        selected = q4Selected,
                        onSelect = { q4Selected = it }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))

                    // Q5
                    SurveyQuestionBlock(
                        number = 5,
                        question = "FOODSCAN AI жүйесі ақпаратты түсінуге көмектесті ме?",
                        options = listOf("Өте пайдалы болды", "Біраз көмектесті", "Әлі толық түсінбедім"),
                        selected = q5Selected,
                        onSelect = { q5Selected = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("Пікіріңіз немесе ұсынысыңыз (міндетті емес):") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val age = ageText.toIntOrNull()?.coerceIn(10, 18) ?: 14
                            viewModel.submitSurvey(
                                age = age,
                                q1 = q1Selected,
                                q2 = q2Selected,
                                q3 = q3Selected,
                                q4 = q4Selected,
                                q5 = q5Selected,
                                comment = commentText
                            ) {
                                Toast.makeText(context, "Жауабыңыз зерттеу дерекқорына сәтті қосылды!", Toast.LENGTH_SHORT).show()
                                selectedTab = 1
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_survey_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Жауапты сақтау және диаграмманы көру", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // RESULTS & CHARTS (SHOWING ACTUAL RESPONSES ONLY)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("survey_results_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "НАҚТЫ САУАЛНАМА СТАТИСТИКАСЫ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FreshGreen
                        ) {
                            Text(
                                text = "Барлығы: ${surveys.size} жасөспірім",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Chart 1
                    SurveyResultChartItem(
                        title = "1. Тағам құрамын оқитындар",
                        responses = surveys.map { it.q1ReadsLabel },
                        options = listOf("Иә, үнемі", "Кейде", "Жоқ, ешқашан")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Chart 2
                    SurveyResultChartItem(
                        title = "2. Қант мөлшерін табу қабілеті",
                        responses = surveys.map { it.q2FindsSugar },
                        options = listOf("Оңай табамын", "Қиындықпен", "Таба алмаймын")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Chart 3
                    SurveyResultChartItem(
                        title = "3. 100г және порция айырмашылығын білу",
                        responses = surveys.map { it.q3Knows100gVsServing },
                        options = listOf("Иә, жақсы білемін", "Шамамен білемін", "Білмеймін")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Chart 4
                    SurveyResultChartItem(
                        title = "4. Екі өнімді салыстыру біліктілігі",
                        responses = surveys.map { it.q4CanCompare },
                        options = listOf("Иә, оңай", "Кейде шатасамын", "Жоқ, қиналамын")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Chart 5
                    SurveyResultChartItem(
                        title = "5. FOODSCAN AI тиімділігі",
                        responses = surveys.map { it.q5HelpedUnderstand },
                        options = listOf("Өте пайдалы болды", "Біраз көмектесті", "Әлі толық түсінбедім")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        MedicalDisclaimerCard()
    }
}

@Composable
private fun SurveyQuestionBlock(
    number: Int,
    question: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column {
        Text(
            text = "$number. $question",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NavyDark,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        options.forEach { opt ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(opt) }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == opt,
                    onClick = { onSelect(opt) },
                    colors = RadioButtonDefaults.colors(selectedColor = FreshGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = opt, fontSize = 13.sp, color = Color(0xFF334155))
            }
        }
    }
}

@Composable
private fun SurveyResultChartItem(
    title: String,
    responses: List<String>,
    options: List<String>
) {
    val total = responses.size.coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = NavyDark
        )
        Spacer(modifier = Modifier.height(8.dp))

        val colors = listOf(FreshGreen, AccentCyan, AccentAmber)

        options.forEachIndexed { index, option ->
            val count = responses.count { it.contains(option.take(4), ignoreCase = true) }
            val pct = (count.toFloat() / total.toFloat()) * 100f
            val barColor = colors.getOrElse(index) { Color(0xFF64748B) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = option,
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    modifier = Modifier.width(130.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (pct / 100f).coerceIn(0.01f, 1f))
                            .height(10.dp)
                            .background(barColor)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$count (${String.format(java.util.Locale.US, "%.0f%%", pct)})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark,
                    modifier = Modifier.width(55.dp)
                )
            }
        }
    }
}
