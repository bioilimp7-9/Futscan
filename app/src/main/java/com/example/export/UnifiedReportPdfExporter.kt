package com.example.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.example.data.model.ChemicalPhRecord
import com.example.data.model.DataSourceTag
import com.example.data.model.FreshnessVisionRecord
import com.example.data.model.ProductItem
import com.example.data.model.StorageMonitorRecord
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UnifiedReportPdfData(
    val productName: String,
    val studentAuthor: String,
    val schoolProjectTitle: String,
    val ocrProduct: ProductItem?,
    val visionRecords: List<FreshnessVisionRecord>,
    val phRecords: List<ChemicalPhRecord>,
    val storageRecords: List<StorageMonitorRecord>
)

data class PdfReportChronoRow(
    val timestamp: String,
    val moduleLabel: String,
    val resultSummary: String,
    val dataSource: String
)

data class PdfReportPreparedSummary(
    val projectHeading: String,
    val productLabel: String,
    val authorLabel: String,
    val ocrSourceBadge: String,
    val visionSourceBadge: String,
    val phSourceBadge: String,
    val storageSourceBadge: String,
    val storageTempLine: String?,
    val phPoints: List<Pair<String, Float>>,
    val tempPoints: List<Pair<String, Float>>,
    val chronoRows: List<PdfReportChronoRow>
)

object UnifiedReportPdfExporter {

    private const val PAGE_WIDTH = 595   // A4 width in points (72 dpi)
    private const val PAGE_HEIGHT = 842  // A4 height in points (72 dpi)
    private const val MARGIN_X = 36f
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_X * 2
    private const val BOTTOM_LIMIT = PAGE_HEIGHT - 54f

    fun buildSuggestedFilename(productName: String): String {
        val cleanName = productName
            .trim()
            .replace(Regex("[^\\p{L}\\p{N}_-]+"), "_")
            .trim('_')
            .ifBlank { "Onim" }
            .take(28)
        val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        return "FOODSCAN_2.0_${cleanName}_${dateStamp}.pdf"
    }

    fun prepareSummary(data: UnifiedReportPdfData): PdfReportPreparedSummary {
        val latestVision = data.visionRecords.firstOrNull()
        val latestPh = data.phRecords.firstOrNull()
        val latestStorage = data.storageRecords.firstOrNull()

        val storageTempLine = latestStorage?.let { s ->
            val roomText = s.roomTempC?.let { "$it °C" } ?: "Өлшенбеген"
            val fridgeText = s.fridgeTempC?.let { "$it °C" } ?: "Өлшенбеген"
            val humText = s.humidityPercent?.let { "$it%" } ?: "Өлшенбеген"
            "Бөлме t°: $roomText   |   Тоңазытқыш t°: $fridgeText   |   Ылғалдылық: $humText"
        }

        val phPoints = data.phRecords.reversed().mapNotNull { rec ->
            extractPhFloat(rec.estimatedPhRange)?.let { rec.measuredAtText to it }
        }
        val tempPoints = data.storageRecords.reversed().mapNotNull { rec ->
            val temp = rec.fridgeTempC ?: rec.roomTempC
            temp?.let { rec.measuredAtText to it }
        }

        val rows = buildList {
            data.visionRecords.forEach { v ->
                add(PdfReportChronoRow(v.measuredAtText, "Визуалды", v.statusLabel, v.dataSource))
            }
            data.phRecords.forEach { p ->
                add(PdfReportChronoRow(p.measuredAtText, "pH", p.estimatedPhRange, p.dataSource))
            }
            data.storageRecords.forEach { s ->
                val summary = "Бөлме: ${s.roomTempC ?: "—"}°C / Тоң: ${s.fridgeTempC ?: "—"}°C"
                add(PdfReportChronoRow(s.measuredAtText, "Сақтау", summary, s.dataSource))
            }
        }

        return PdfReportPreparedSummary(
            projectHeading = data.schoolProjectTitle.trim().ifBlank { "Мектеп ғылыми жобасы: Біріктірілген өнім есебі (A–D)" },
            productLabel = data.productName.trim().ifBlank { "Таңдалмаған" },
            authorLabel = data.studentAuthor.trim().ifBlank { "Көрсетілмеген" },
            ocrSourceBadge = if (data.ocrProduct != null) DataSourceTag.OCR.labelKz else DataSourceTag.NONE.labelKz,
            visionSourceBadge = latestVision?.dataSource ?: DataSourceTag.NONE.labelKz,
            phSourceBadge = latestPh?.dataSource ?: DataSourceTag.NONE.labelKz,
            storageSourceBadge = latestStorage?.dataSource ?: DataSourceTag.NONE.labelKz,
            storageTempLine = storageTempLine,
            phPoints = phPoints,
            tempPoints = tempPoints,
            chronoRows = rows
        )
    }

    fun createShareablePdfFile(context: Context, data: UnifiedReportPdfData): File {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(reportsDir, buildSuggestedFilename(data.productName))
        FileOutputStream(file).use { out ->
            writePdfToStream(context, out, data)
        }
        return file
    }

    fun writePdfToStream(
        context: Context,
        outputStream: OutputStream,
        data: UnifiedReportPdfData
    ) {
        val summary = prepareSummary(data)
        val document = PdfDocument()
        val writer = PdfPageWriter(document)
        try {
            writer.startNewPage()
            drawCoverBanner(writer, summary)

            // Section A: Food Composition (OCR)
            drawSectionHeader(
                writer = writer,
                title = "A. Қоректік құрам (FOOD COMPOSITION)",
                sourceBadge = summary.ocrSourceBadge
            )
            val ocr = data.ocrProduct
            if (ocr != null) {
                drawWrappedBody(
                    writer,
                    "Өлшем негізі: ${ocr.servingSizeText}  |  Санаты: ${ocr.category}",
                    bold = false
                )
                drawNutrientSummaryStrip(writer, ocr)
                if (ocr.notes.isNotBlank()) {
                    drawWrappedBody(writer, "Ескертпе: ${ocr.notes}")
                }
            } else {
                drawWrappedBody(writer, "Дерек жоқ — бұл өнім бойынша қаптама сканерленбеген.", color = 0xFF64748B.toInt())
            }
            writer.advance(10f)

            // Section B: Visual Analysis (Freshness Vision)
            val latestVision = data.visionRecords.firstOrNull()
            drawSectionHeader(
                writer = writer,
                title = "B. Визуалды балғындық талдау (FRESHNESS VISION)",
                sourceBadge = summary.visionSourceBadge
            )
            if (latestVision != null) {
                drawWrappedBody(writer, "Мәртебе: ${latestVision.statusLabel}", bold = true, color = 0xFF047857.toInt())
                drawWrappedBody(writer, "Танылған тағам: ${latestVision.recognizedFoodName}")
                drawWrappedBody(writer, "Сыртқы көрініс: ${latestVision.surfaceDescription}")
                drawWrappedBody(writer, "Байқалған өзгерістер: ${latestVision.observedChanges}")
                drawWrappedBody(writer, "Салыстырмалы қорытынды: ${latestVision.comparativeConclusion}")
                drawWrappedBody(writer, "Уақыты: ${latestVision.measuredAtText}", color = 0xFF64748B.toInt())

                latestVision.imageUri?.let { uriStr ->
                    loadScaledBitmapSafely(context, uriStr)?.let { bmp ->
                        writer.ensureSpace(120f)
                        val targetW = 140f
                        val aspect = if (bmp.width > 0) bmp.height.toFloat() / bmp.width.toFloat() else 0.75f
                        val targetH = (targetW * aspect).coerceIn(70f, 115f)
                        val dest = RectF(MARGIN_X, writer.y, MARGIN_X + targetW, writer.y + targetH)
                        writer.canvas.drawBitmap(bmp, null, dest, null)
                        bmp.recycle()
                        writer.advance(targetH + 6f)
                    }
                }
            } else {
                drawWrappedBody(writer, "Дерек жоқ — визуалды балғындық талдауы жүргізілмеген.", color = 0xFF64748B.toInt())
            }
            writer.advance(10f)

            // Section C: Chemical Analysis (pH)
            val latestPh = data.phRecords.firstOrNull()
            drawSectionHeader(
                writer = writer,
                title = "C. Химиялық pH талдау (CHEMICAL ANALYSIS)",
                sourceBadge = summary.phSourceBadge
            )
            if (latestPh != null) {
                drawWrappedBody(writer, "Үлгі түрі: ${latestPh.sampleType}")
                drawWrappedBody(writer, "Индикатор қағазының түсі: ${latestPh.paperColor}")
                drawWrappedBody(writer, "Болжамды pH мәні: ${latestPh.estimatedPhRange}", bold = true, color = 0xFF6D28D9.toInt())
                drawWrappedBody(writer, "Орта сипаттамасы: ${latestPh.acidityExplanation}")
                drawWrappedBody(writer, "Сенімділік: ${latestPh.confidenceLevel} • Шектеу: ${latestPh.errorMarginNote}", color = 0xFF475569.toInt())
                drawWrappedBody(writer, "Уақыты: ${latestPh.measuredAtText}", color = 0xFF64748B.toInt())
            } else {
                drawWrappedBody(writer, "Дерек жоқ — pH индикатор талдауы жүргізілмеген.", color = 0xFF64748B.toInt())
            }
            writer.advance(10f)

            // Section D: Storage Conditions (Storage Monitor)
            val latestStorage = data.storageRecords.firstOrNull()
            drawSectionHeader(
                writer = writer,
                title = "D. Сақтау жағдайы (STORAGE MONITOR)",
                sourceBadge = summary.storageSourceBadge
            )
            if (latestStorage != null && summary.storageTempLine != null) {
                drawWrappedBody(writer, summary.storageTempLine, bold = true)
                drawWrappedBody(writer, "Жарық: ${latestStorage.lightCondition}   |   Орны: ${latestStorage.storageLocation}   |   Ұзақтығы: ${latestStorage.storageDuration}")
                drawWrappedBody(writer, "Уақыты: ${latestStorage.measuredAtText}", color = 0xFF64748B.toInt())
            } else {
                drawWrappedBody(writer, "Дерек жоқ — сақтау параметрлері енгізілмеген.", color = 0xFF64748B.toInt())
            }
            writer.advance(12f)

            // Multi-day Trend Charts (pH & Temperature)
            if (summary.phPoints.isNotEmpty() || summary.tempPoints.isNotEmpty()) {
                drawSectionHeader(
                    writer = writer,
                    title = "Көпкүндік динамика және тренд графиктері",
                    sourceBadge = "Хронологиялық талдау"
                )
                if (summary.phPoints.isNotEmpty()) {
                    drawTrendChartBox(
                        writer = writer,
                        title = "pH өзгерісінің графигі (шамамен мәндер)",
                        points = summary.phPoints,
                        lineColor = 0xFF7C3AED.toInt(),
                        minY = 2f,
                        maxY = 12f,
                        unit = ""
                    )
                }
                if (summary.tempPoints.isNotEmpty()) {
                    drawTrendChartBox(
                        writer = writer,
                        title = "Сақтау температурасының графигі (°C)",
                        points = summary.tempPoints,
                        lineColor = 0xFF0284C7.toInt(),
                        minY = -5f,
                        maxY = 35f,
                        unit = "°C"
                    )
                }
            }

            // Chronological Table
            if (summary.chronoRows.isNotEmpty()) {
                drawSectionHeader(
                    writer = writer,
                    title = "Өлшемдердің хронологиялық жиынтық кестесі (${summary.chronoRows.size})",
                    sourceBadge = "Жоба журналы"
                )
                drawTableHeader(writer)
                summary.chronoRows.forEach { row ->
                    drawTableRow(writer, row.timestamp, row.moduleLabel, row.resultSummary, row.dataSource)
                }
                writer.advance(10f)
            }

            // Warnings & School Project Integrity Footer Box
            drawIntegrityNoticeBox(writer)

            writer.finishCurrentPage()
            document.writeTo(outputStream)
        } finally {
            document.close()
        }
    }

    private fun drawCoverBanner(writer: PdfPageWriter, summary: PdfReportPreparedSummary) {
        val bannerHeight = 98f
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF0A2540.toInt() }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF059669.toInt() }
        val rect = RectF(MARGIN_X, writer.y, PAGE_WIDTH - MARGIN_X, writer.y + bannerHeight)
        writer.canvas.drawRoundRect(rect, 10f, 10f, bgPaint)
        writer.canvas.drawRoundRect(
            RectF(MARGIN_X, writer.y, MARGIN_X + 8f, writer.y + bannerHeight),
            4f,
            4f,
            accentPaint
        )

        val kickerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFA7F3D0.toInt()
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 15.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val metaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE2E8F0.toInt()
            textSize = 10.5f
        }

        val leftX = MARGIN_X + 18f
        val maxTextW = CONTENT_WIDTH - 28f
        val topY = writer.y + 20f
        writer.canvas.drawText("FOODSCAN AI 2.0  •  SMART FOOD ANALYSIS SYSTEM", leftX, topY, kickerPaint)

        val clippedTitle = TextUtils.ellipsize(summary.projectHeading, titlePaint, maxTextW, TextUtils.TruncateAt.END).toString()
        writer.canvas.drawText(clippedTitle, leftX, topY + 21f, titlePaint)

        val clippedProduct = TextUtils.ellipsize("Зерттелетін өнім: ${summary.productLabel}", metaPaint, maxTextW, TextUtils.TruncateAt.END).toString()
        writer.canvas.drawText(clippedProduct, leftX, topY + 41f, metaPaint)

        val nowText = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        val clippedMeta = TextUtils.ellipsize("Орындаған: ${summary.authorLabel}   |   Күні: $nowText", metaPaint, maxTextW, TextUtils.TruncateAt.END).toString()
        writer.canvas.drawText(clippedMeta, leftX, topY + 59f, metaPaint)

        writer.advance(bannerHeight + 14f)
    }

    private fun drawSectionHeader(writer: PdfPageWriter, title: String, sourceBadge: String) {
        writer.ensureSpace(58f)
        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFF1F5F9.toInt() }
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFE0F2FE.toInt() }
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0A2540.toInt()
            textSize = 11.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val badgeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0369A1.toInt()
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val barH = 26f
        writer.canvas.drawRoundRect(
            RectF(MARGIN_X, writer.y, PAGE_WIDTH - MARGIN_X, writer.y + barH),
            6f,
            6f,
            barPaint
        )

        val badgeTextWidth = badgeTextPaint.measureText(sourceBadge)
        val badgeW = badgeTextWidth + 14f
        val badgeRight = PAGE_WIDTH - MARGIN_X - 6f
        val badgeLeft = badgeRight - badgeW
        val maxTitleW = (badgeLeft - MARGIN_X - 18f).coerceAtLeast(100f)
        val clippedTitle = TextUtils.ellipsize(title, titlePaint, maxTitleW, TextUtils.TruncateAt.END).toString()
        writer.canvas.drawText(clippedTitle, MARGIN_X + 10f, writer.y + 17f, titlePaint)

        writer.canvas.drawRoundRect(
            RectF(badgeLeft, writer.y + 5f, badgeRight, writer.y + 21f),
            4f,
            4f,
            badgePaint
        )
        writer.canvas.drawText(sourceBadge, badgeLeft + 7f, writer.y + 16f, badgeTextPaint)
        writer.advance(barH + 6f)
    }

    private fun drawNutrientSummaryStrip(writer: PdfPageWriter, ocr: ProductItem) {
        writer.ensureSpace(46f)
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFF8FAFC.toInt() }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFCBD5E1.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF64748B.toInt()
            textSize = 8.5f
        }
        val valuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0F172A.toInt()
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val stripH = 38f
        val rect = RectF(MARGIN_X, writer.y, PAGE_WIDTH - MARGIN_X, writer.y + stripH)
        writer.canvas.drawRoundRect(rect, 6f, 6f, boxPaint)
        writer.canvas.drawRoundRect(rect, 6f, 6f, borderPaint)

        val cells = listOf(
            "Калория" to (ocr.calories?.let { "$it ккал" } ?: "Дерек жоқ"),
            "Қант" to (ocr.sugar?.let { "$it г" } ?: "Дерек жоқ"),
            "Май" to (ocr.fat?.let { "$it г" } ?: "Дерек жоқ"),
            "Ақуыз" to (ocr.protein?.let { "$it г" } ?: "Дерек жоқ"),
            "Тұз" to (ocr.salt?.let { "$it г" } ?: "Дерек жоқ")
        )
        val cellW = CONTENT_WIDTH / cells.size
        cells.forEachIndexed { idx, (label, valText) ->
            val cx = MARGIN_X + idx * cellW + 10f
            writer.canvas.drawText(label, cx, writer.y + 14f, labelPaint)
            writer.canvas.drawText(valText, cx, writer.y + 29f, valuePaint)
        }
        writer.advance(stripH + 6f)
    }

    private fun drawTrendChartBox(
        writer: PdfPageWriter,
        title: String,
        points: List<Pair<String, Float>>,
        lineColor: Int,
        minY: Float,
        maxY: Float,
        unit: String
    ) {
        writer.ensureSpace(115f)
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0F172A.toInt()
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        writer.canvas.drawText(title, MARGIN_X, writer.y + 12f, titlePaint)
        writer.advance(16f)

        val chartH = 84f
        val chartLeft = MARGIN_X
        val chartRight = PAGE_WIDTH - MARGIN_X
        val chartTop = writer.y
        val chartBottom = chartTop + chartH

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFF8FAFC.toInt() }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE2E8F0.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        writer.canvas.drawRoundRect(RectF(chartLeft, chartTop, chartRight, chartBottom), 6f, 6f, bgPaint)
        writer.canvas.drawRoundRect(RectF(chartLeft, chartTop, chartRight, chartBottom), 6f, 6f, borderPaint)

        val padX = 28f
        val padY = 18f
        val plotW = (chartRight - chartLeft) - padX * 2
        val plotH = chartH - padY * 2
        val span = (maxY - minY).coerceAtLeast(1f)

        val coords = points.mapIndexed { idx, (_, value) ->
            val x = if (points.size == 1) (chartLeft + chartRight) / 2f else chartLeft + padX + (idx.toFloat() / (points.size - 1)) * plotW
            val y = chartBottom - padY - (((value.coerceIn(minY, maxY) - minY) / span) * plotH)
            x to y
        }

        if (coords.size > 1) {
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = lineColor
                style = Paint.Style.STROKE
                strokeWidth = 2.2f
            }
            val path = Path().apply {
                moveTo(coords.first().first, coords.first().second)
                coords.drop(1).forEach { (x, y) -> lineTo(x, y) }
            }
            writer.canvas.drawPath(path, linePaint)
        }

        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = lineColor }
        val valPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0F172A.toInt()
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val datePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF64748B.toInt()
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
        }

        coords.forEachIndexed { idx, (x, y) ->
            writer.canvas.drawCircle(x, y, 3.8f, dotPaint)
            val valStr = String.format(Locale.US, "%.1f%s", points[idx].second, unit)
            writer.canvas.drawText(valStr, x, (y - 6f).coerceAtLeast(chartTop + 10f), valPaint)
            val dateStr = points[idx].first.take(10)
            writer.canvas.drawText(dateStr, x, chartBottom - 4f, datePaint)
        }

        writer.advance(chartH + 10f)
    }

    private fun drawTableHeader(writer: PdfPageWriter) {
        writer.ensureSpace(24f)
        val bgPaint = Paint().apply { color = 0xFFE2E8F0.toInt() }
        writer.canvas.drawRect(MARGIN_X, writer.y, PAGE_WIDTH - MARGIN_X, writer.y + 20f, bgPaint)
        val headPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0F172A.toInt()
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        writer.canvas.drawText("Күні / Уақыты", MARGIN_X + 6f, writer.y + 13f, headPaint)
        writer.canvas.drawText("Бөлім", MARGIN_X + 125f, writer.y + 13f, headPaint)
        writer.canvas.drawText("Нәтиже", MARGIN_X + 195f, writer.y + 13f, headPaint)
        writer.canvas.drawText("Дереккөз", MARGIN_X + 385f, writer.y + 13f, headPaint)
        writer.advance(20f)
    }

    private fun drawTableRow(
        writer: PdfPageWriter,
        time: String,
        module: String,
        result: String,
        source: String
    ) {
        writer.ensureSpace(20f)
        val linePaint = Paint().apply {
            color = 0xFFE2E8F0.toInt()
            strokeWidth = 0.7f
        }
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF334155.toInt()
            textSize = 8.5f
        }
        val ellipsizeCol = { text: String, maxW: Float ->
            TextUtils.ellipsize(text, textPaint, maxW, TextUtils.TruncateAt.END).toString()
        }
        writer.canvas.drawText(ellipsizeCol(time, 112f), MARGIN_X + 6f, writer.y + 13f, textPaint)
        writer.canvas.drawText(ellipsizeCol(module, 64f), MARGIN_X + 125f, writer.y + 13f, textPaint)
        writer.canvas.drawText(ellipsizeCol(result, 182f), MARGIN_X + 195f, writer.y + 13f, textPaint)
        writer.canvas.drawText(ellipsizeCol(source, 132f), MARGIN_X + 385f, writer.y + 13f, textPaint)
        writer.canvas.drawLine(MARGIN_X, writer.y + 18f, PAGE_WIDTH - MARGIN_X, writer.y + 18f, linePaint)
        writer.advance(18f)
    }

    private fun drawIntegrityNoticeBox(writer: PdfPageWriter) {
        val text = "Ғылыми дәлдік және қауіпсіздік ескертуі: Сурет жарығы әртүрлі болса, түс салыстыру нәтижесінің дәлдігі төмендеуі мүмкін. Визуалды талдау ішкі бактерия санын немесе медициналық/санитарлық қауіпсіздікті анықтамайды. pH индикатор нәтижесі зертханалық pH-метр өлшемі емес, шамамен бағалау болып табылады."
        val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF92400E.toInt()
            textSize = 8.8f
        }
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, tp, (CONTENT_WIDTH - 20f).toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
        val boxH = layout.height + 16f
        writer.ensureSpace(boxH + 8f)

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFEF3C7.toInt() }
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFCD34D.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val rect = RectF(MARGIN_X, writer.y, PAGE_WIDTH - MARGIN_X, writer.y + boxH)
        writer.canvas.drawRoundRect(rect, 8f, 8f, bg)
        writer.canvas.drawRoundRect(rect, 8f, 8f, border)

        writer.canvas.save()
        writer.canvas.translate(MARGIN_X + 10f, writer.y + 8f)
        layout.draw(writer.canvas)
        writer.canvas.restore()
        writer.advance(boxH + 8f)
    }

    private fun drawWrappedBody(
        writer: PdfPageWriter,
        text: String,
        bold: Boolean = false,
        color: Int = 0xFF1E293B.toInt()
    ) {
        val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
        }
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, tp, CONTENT_WIDTH.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1.0f)
            .build()
        val h = layout.height.toFloat()
        writer.ensureSpace(h + 4f)
        writer.canvas.save()
        writer.canvas.translate(MARGIN_X, writer.y)
        layout.draw(writer.canvas)
        writer.canvas.restore()
        writer.advance(h + 3f)
    }

    private fun loadScaledBitmapSafely(context: Context, uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            }
            val maxDim = 480
            var sampleSize = 1
            while (boundsOpts.outWidth / sampleSize > maxDim || boundsOpts.outHeight / sampleSize > maxDim) {
                sampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            }
        } catch (_: Throwable) {
            null
        }
    }

    internal fun extractPhFloat(text: String): Float? {
        if (text.isBlank() || text == "анықтау мүмкін болмады") return null
        val regex = Regex("[0-9]+(?:\\.[0-9]+)?")
        val nums = regex.findAll(text.replace(',', '.'))
            .mapNotNull { it.value.toFloatOrNull() }
            .filter { it in 0f..14f }
            .toList()
        return if (nums.isNotEmpty()) nums.average().toFloat() else null
    }

    private class PdfPageWriter(private val document: PdfDocument) {
        private var currentPage: PdfDocument.Page? = null
        private var pageNumber = 0
        var y: Float = 32f
            private set

        val canvas: Canvas
            get() = currentPage!!.canvas

        fun startNewPage() {
            finishCurrentPage()
            pageNumber += 1
            val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            currentPage = document.startPage(info)
            y = 32f
        }

        fun ensureSpace(neededHeight: Float) {
            if (y + neededHeight > BOTTOM_LIMIT) {
                startNewPage()
            }
        }

        fun advance(delta: Float) {
            y += delta
        }

        fun finishCurrentPage() {
            currentPage?.let { page ->
                val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF64748B.toInt()
                    textSize = 8.5f
                }
                page.canvas.drawText(
                    "FOODSCAN AI 2.0 • Мектеп жобасының біріктірілген есебі (A–D)",
                    MARGIN_X,
                    PAGE_HEIGHT - 24f,
                    footerPaint
                )
                val pageLabel = "Бет $pageNumber"
                val w = footerPaint.measureText(pageLabel)
                page.canvas.drawText(pageLabel, PAGE_WIDTH - MARGIN_X - w, PAGE_HEIGHT - 24f, footerPaint)
                document.finishPage(page)
            }
            currentPage = null
        }
    }
}
