package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.StudentTestAttempt
import com.example.data.model.TestPhase
import com.example.data.samples.ResearchProtocolData
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.FreshGreenDark
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun StudentPrePostSheet(
    attempts: List<StudentTestAttempt>,
    onTakeTest: () -> Unit,
    onDeleteAttempt: (StudentTestAttempt) -> Unit
) {
    Text(
        text = "3-ПАРАҚ: ОҚУШЫ ТЕСТІ — ДЕЙІН ЖӘНЕ КЕЙІНГІ ЖАУАПТАР",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = FreshGreenDark
    )
    Spacer(modifier = Modifier.height(6.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Оқушылар нәтижесі (${attempts.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NavyDark
        )
        Button(
            onClick = onTakeTest,
            modifier = Modifier
                .height(48.dp)
                .testTag("start_student_test_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Тест тапсыру", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    if (attempts.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("empty_student_tests_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Әзірге тапсырылған Pre/Post тест нәтижелері жоқ.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Алдымен «4. Тест сұрақтары» парағында «Пайдалануға дейін (Pre-test)», ал қосымшаны қолданғаннан кейін «Пайдаланудан кейін (Post-test)» жауаптарын сақтаңыз.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 17.sp
                )
            }
        }
        return
    }

    // Group attempts by normalized student name to highlight Pre vs Post improvement
    val grouped = attempts.groupBy { it.studentName.trim().lowercase() }
    grouped.values.forEach { studentAttempts ->
        val pre = studentAttempts.filter { it.phase == TestPhase.PRE.name }.maxByOrNull { it.timestamp }
        val post = studentAttempts.filter { it.phase == TestPhase.POST.name }.maxByOrNull { it.timestamp }
        val displayName = studentAttempts.first().studentName
        val grade = studentAttempts.first().studentGrade
        val age = studentAttempts.first().studentAge

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = displayName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                        Text(
                            text = "$grade • $age жас",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    if (pre != null && post != null) {
                        val delta = post.score - pre.score
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (delta >= 0) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = if (delta >= 0) "+$delta балл өсім" else "$delta балл",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (delta >= 0) FreshGreenDark else AccentAmber,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                studentAttempts.forEach { attempt ->
                    val phaseObj = TestPhase.fromString(attempt.phase)
                    val pct = (attempt.score.toFloat() / attempt.totalQuestions.coerceAtLeast(1)) * 100f
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (phaseObj == TestPhase.PRE) AccentAmber.copy(alpha = 0.18f) else FreshGreen.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = phaseObj.badgeKz,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (phaseObj == TestPhase.PRE) AccentAmber else FreshGreenDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${attempt.score} / ${attempt.totalQuestions} (${pct.toInt()}%)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyDark
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Жауаптар (1–8): ${attempt.answersCsv}",
                                fontSize = 11.sp,
                                color = Color(0xFF475569)
                            )
                        }
                        IconButton(
                            onClick = { onDeleteAttempt(attempt) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Жою",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EightQuestionTestSheet(
    onSubmit: (studentName: String, age: Int, grade: String, phase: TestPhase, selectedIndices: List<Int>) -> Unit
) {
    val context = LocalContext.current
    val questions = ResearchProtocolData.eightQuestions

    var studentName by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("14") }
    var gradeText by remember { mutableStateOf("8 «А»") }
    var selectedPhase by remember { mutableStateOf(TestPhase.PRE) }
    val selectedAnswers = remember { mutableStateListOf(*Array(questions.size) { -1 }) }

    Text(
        text = "4-ПАРАҚ: ТЕСТ СҰРАҚТАРЫ — 8 ТАПСЫРМАДАН ТҰРАТЫН БІЛІМ ТЕСТІ",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = FreshGreenDark
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "Тағам жапсырмасын оқу білімін тексеру",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = NavyDark
    )
    Spacer(modifier = Modifier.height(10.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            OutlinedTextField(
                value = studentName,
                onValueChange = { studentName = it },
                label = { Text("Оқушы аты-жөні немесе коды (мысалы: Айым)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_student_name_input")
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = gradeText,
                    onValueChange = { gradeText = it },
                    label = { Text("Сыныбы") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it },
                    label = { Text("Жасы") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.7f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("Тест кезеңін таңдаңыз:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyDark)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TestPhase.entries.forEach { phase ->
                    val active = selectedPhase == phase
                    if (active) {
                        Button(
                            onClick = { selectedPhase = phase },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("phase_${phase.name}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (phase == TestPhase.PRE) AccentAmber else FreshGreen
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(phase.badgeKz, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { selectedPhase = phase },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("phase_${phase.name}"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(phase.badgeKz, fontSize = 12.sp, color = NavyDark)
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    questions.forEachIndexed { qIdx, q ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AccentCyan.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "${q.number}-тапсырма • ${q.topic}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = q.question,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                q.options.forEachIndexed { optIdx, optText ->
                    val chosen = selectedAnswers[qIdx] == optIdx
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAnswers[qIdx] = optIdx }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = chosen,
                            onClick = { selectedAnswers[qIdx] = optIdx },
                            colors = RadioButtonDefaults.colors(selectedColor = FreshGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = optText, fontSize = 13.sp, color = Color(0xFF334155))
                    }
                }
            }
        }
    }

    Button(
        onClick = {
            if (selectedAnswers.any { it < 0 }) {
                Toast.makeText(context, "Барлық 8 тапсырмаға жауап таңдаңыз.", Toast.LENGTH_SHORT).show()
                return@Button
            }
            val name = studentName.trim().ifBlank { "Оқушы #1" }
            val age = ageText.toIntOrNull()?.coerceIn(10, 18) ?: 14
            onSubmit(name, age, gradeText, selectedPhase, selectedAnswers.toList())
            Toast.makeText(context, "8-тапсырма тесті сәтті сақталды!", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("submit_8_question_test_button"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
    ) {
        Icon(Icons.Default.Check, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Тестті аяқтау және сақтау", fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AnswerKeySheet() {
    Text(
        text = "5-ПАРАҚ: ЖАУАП КІЛТІ — ДҰРЫС ЖАУАПТАР МЕН ТҮСІНДІРМЕЛЕР",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = FreshGreenDark
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "8 тапсырманың дұрыс жауап кілті",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = NavyDark
    )
    Spacer(modifier = Modifier.height(10.dp))

    ResearchProtocolData.eightQuestions.forEach { q ->
        val correctText = q.options.getOrElse(q.correctOptionIndex) { "" }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = FreshGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${q.number}-тапсырма • ${q.topic}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = q.question,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD1FAE5), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Дұрыс жауап: $correctText",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = FreshGreenDark
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Түсіндірме: ${q.explanation}",
                    fontSize = 12.sp,
                    color = Color(0xFF334155),
                    lineHeight = 17.sp
                )
            }
        }
    }
}
