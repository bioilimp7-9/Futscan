package com.example.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MedicalDisclaimerCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.FreshGreen
import com.example.ui.theme.FreshGreenMuted
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

data class EduTopic(
    val id: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val content: String,
    val keyTakeaway: String,
    val diagramExplanation: String
)

@Composable
fun EducationScreen(
    modifier: Modifier = Modifier
) {
    var expandedIndex by remember { mutableIntStateOf(0) }

    val topics = listOf(
        EduTopic(
            id = 1,
            title = "Ақуыз дегеніміз не?",
            subtitle = "Жасөспірім ағзасының басты құрылыс материалы",
            icon = Icons.Default.FitnessCenter,
            color = FreshGreen,
            content = "Ақуыздар (протеиндер) — ағзадағы барлық жасушалардың, бұлшықеттердің, сүйектердің, шаш пен терінің негізін құрайтын органикалық заттар. Олар аминқышқылдарынан тұрады. Жасөспірім шақта қарқынды өсу мен бойдың ұзаруы жүретіндіктен, ақуыз жеткілікті болуы өте маңызды.\n\nКөздері: ет, балық, жұмыртқа, сүт, ірімшік, бұршақ дақылдары, жаңғақтар.",
            keyTakeaway = "12–17 жастағы жасөспірімге тәулігіне дене салмағының әр килограммына шамамен 1.0–1.2 г ақуыз қажет.",
            diagramExplanation = "Бұлшықет синтезі + Иммундық антиденелер түзілуі + Өсу гормонының белсенділігі."
        ),
        EduTopic(
            id = 2,
            title = "Майдың ағзадағы қызметі",
            subtitle = "Қаныққан, қанықпаған майлар және трансмайлар",
            icon = Icons.Default.Egg,
            color = AccentAmber,
            content = "Майлар тек энергия қоры ғана емес, сонымен қатар ми жасушалары мен гормондардың құрылысына қатысады, А, D, E, K майда еритін дәрумендерін сіңіруге көмектеседі.\n\n• Қанықпаған пайдалы майлар: өсімдік майлары, зәйтүн, авокадо, балық майы.\n• Қаныққан майлар: жануар майлары, сары май (мөлшермен қажет).\n• Трансмайлар: маргарин, чипсы, кондитерлік майлар (денсаулыққа зиян, шектелуі тиіс!).",
            keyTakeaway = "Трансмайлар қаптамада «гидрогенизацияланған май» деп жазылуы мүмкін. Оларды тұтынбауға тырысыңыз.",
            diagramExplanation = "Пайдалы майлар жүрек пен ми жұмысын қолдайды, ал трансмайлар қан тамырларына ауыртпалық түсіреді."
        ),
        EduTopic(
            id = 3,
            title = "Көмірсулар және энергия",
            subtitle = "Күрделі және қарапайым көмірсулардың айырмасы",
            icon = Icons.Default.Bolt,
            color = AccentCyan,
            content = "Көмірсулар — ми мен бұлшықеттердің басты отыны (глюкоза).\n\n• Күрделі көмірсулар (баяу): сұлы, қарақұмық, қара бидай наны, көкөністер. Олар энергияны біртіндеп бөліп, ұзақ уақыт тоқтық сезімін сыйлайды.\n• Қарапайым көмірсулар (жылдам): қант, тәтті тоқаштар, газдалған сусындар. Қандағы глюкозаны күрт көтеріп, тез шаршауға алып келеді.",
            keyTakeaway = "Күнделікті рационның негізін қарапайым қант емес, күрделі көмірсулар құрауы тиіс.",
            diagramExplanation = "Баяу көмірсу = тұрақты зейін мен оқу белсенділігі. Жылдам қант = қысқа серпін және шаршау."
        ),
        EduTopic(
            id = 4,
            title = "Қант мөлшерін қалай түсінеміз?",
            subtitle = "Жасырын қант және тәуліктік 25 грамм нормасы",
            icon = Icons.Default.Grain,
            color = Color(0xFFEC4899),
            content = "Дүниежүзілік Денсаулық сақтау Ұйымы (ДДҰ) жасөспірімдерге еркін қант мөлшерін тәулігіне 25 грамнан (шамамен 5-6 шай қасық) асырмауды ұсынады.\n\nЖасырын қант көбіне газдалған сусындарда (0.5 л бөтелкеде 40–50 г қант!), шырындарда, тәтті йогурттар мен шоколадтарда болады.",
            keyTakeaway = "1 шай қасық қант = шамамен 4-5 грамм. Өнімдегі қантты қасық санымен есептеп көріңіз!",
            diagramExplanation = "500 мл тәтті сусын ішсеңіз, бірден 2 күндік қант нормасын қабылдайсыз."
        ),
        EduTopic(
            id = 5,
            title = "Тұз мөлшерін қалай оқимыз?",
            subtitle = "Натрий және ас тұзы (NaCl)",
            icon = Icons.Default.Opacity,
            color = Color(0xFF3B82F6),
            content = "Тұз ағзадағы су балансын сақтайды, бірақ оның артық мөлшері қан қысымын көтеріп, бүйрекке салмақ түсіреді. ДДҰ нормасы — тәулігіне 5 грамнан (1 шай қасық) аспауы керек.\n\nЕгер қаптамада «Натрий (Sodium)» көрсетілсе, тұз мөлшерін табу үшін оны 2.5-ке көбейту керек: Тұз = Натрий × 2.5.",
            keyTakeaway = "Чипсы, фастфуд, сухариктерде тұз мөлшері өте жоғары болады.",
            diagramExplanation = "Натрий 0.4 г болса = 0.4 × 2.5 = 1.0 г нақты ас тұзы."
        ),
        EduTopic(
            id = 6,
            title = "Тағамдық құндылық дегеніміз не?",
            subtitle = "100 грамм және 1 порция арасындағы айырмашылық",
            icon = Icons.Default.Science,
            color = NavyPrimary,
            content = "Тағамдық құндылық (пищевая ценность) — өнім құрамындағы ақуыз, май, көмірсу және калория мөлшері.\n\nЕҢ МАҢЫЗДЫ ҚАТЕЛІК: Көптеген өндірушілер кестені «1 порция» үшін жазады (мысалы 25 г). Оқушы оны бүкіл қаптаманың көрсеткіші деп ойлап шатасады. FOODSCAN AI екі өнімді салыстырғанда осы айырмашылықты қадағалайды.",
            keyTakeaway = "Әрқашан көрсеткіштің 100 граммға ма, әлде бір порцияға жазылғанын тексеріңіз!",
            diagramExplanation = "100 г үшін жазылған өнімді 30 г порциямен тікелей салыстыруға БОЛМАЙДЫ."
        ),
        EduTopic(
            id = 7,
            title = "Өнім жапсырмасын дұрыс оқу жолдары",
            subtitle = "Ингредиенттер тәртібі мен Е-қоспалар",
            icon = Icons.Default.QrCodeScanner,
            color = Color(0xFF8B5CF6),
            content = "Заң бойынша қаптамадағы құрам ингредиенттердің массалық үлесі азаю ретімен орналасады. Яғни, ең бірінші тұрған ингредиент — өнімде ең көп мөлшерде.\n\nЕгер шоколад немесе шырын құрамында бірінші болып «Қант» тұрса, демек бұл өнімнің негізгі бөлігі қанттан тұрады. Сондай-ақ сақтау мерзімі мен сақтау температурасын қадағалау маңызды.",
            keyTakeaway = "Құрамның алғашқы 3 ингредиенті өнімнің нақты неден жасалғанын білдіреді.",
            diagramExplanation = "1-орында: негізгі шикізат (сүт немесе жеміс) тұрған өнімдерге басымдық беріңіз."
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("education_screen")
    ) {
        Text(
            text = "ЖАСӨСПІРІМДЕРГЕ АРНАЛҒАН БІЛІМ БАЗАСЫ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 1.sp
        )
        Text(
            text = "Тағам туралы білім",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = NavyDark
        )
        Text(
            text = "8-сынып деңгейіндегі нутрициология негіздері, жапсырманы дұрыс оқу ережелері мен ДДҰ ұсыныстары.",
            fontSize = 13.sp,
            color = Color(0xFF475569)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // TEEN DAILY NUTRITION QUICK REFERENCE CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "12–17 ЖАСТАҒЫ ЖАСӨСПІРІМНІҢ ТӘУЛІКТІК БАҒДАРЫ:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = FreshGreenMuted
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TeenRefBadge("Қант", "макс 25 г", "≤ 5-6 қасық")
                    TeenRefBadge("Тұз", "макс 5 г", "≤ 1 қасық")
                    TeenRefBadge("Ақуыз", "65–85 г", "өсу материалы")
                    TeenRefBadge("Калория", "2300 ккал", "белсенділікке")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7 TOPICS ACCORDION
        topics.forEachIndexed { index, topic ->
            val isExpanded = expandedIndex == index

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedIndex = if (isExpanded) -1 else index }
                    .testTag("edu_topic_${topic.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(topic.color.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = topic.icon, contentDescription = null, tint = topic.color, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                            Text(
                                text = topic.subtitle,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFF64748B)
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text(
                                text = topic.content,
                                fontSize = 13.sp,
                                color = Color(0xFF334155),
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Diagram / Formula block
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "ДИАГРАММА / ФОРМУЛА:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = topic.diagramExplanation,
                                        fontSize = 12.sp,
                                        color = Color(0xFF1E293B),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Key Takeaway
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFD1FAE5)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = FreshGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = topic.keyTakeaway,
                                        fontSize = 12.sp,
                                        color = Color(0xFF065F46),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        MedicalDisclaimerCard()
    }
}

@Composable
private fun TeenRefBadge(title: String, val1: String, val2: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontSize = 11.sp, color = Color(0xFF94A3B8))
        Text(text = val1, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = val2, fontSize = 9.sp, color = FreshGreenMuted)
    }
}
