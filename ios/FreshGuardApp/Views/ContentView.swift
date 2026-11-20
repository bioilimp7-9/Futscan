import SwiftUI

struct ContentView: View {
    @EnvironmentObject private var store: FreshnessStore
    @StateObject private var bleAnalyzer = ColorSensorAnalyzer()

    var body: some View {
        TabView {
            MonitorView(bleAnalyzer: bleAnalyzer)
                .tabItem {
                    Label("Монитор", systemImage: "waveform.path.ecg")
                }

            CameraIndicatorView()
                .tabItem {
                    Label("Индикатор", systemImage: "camera.metering.center.weighted")
                }

            SciencePillarsView()
                .tabItem {
                    Label("Ғылым", systemImage: "atom")
                }

            JournalHistoryView()
                .tabItem {
                    Label("Журнал", systemImage: "list.bullet.clipboard")
                }
        }
        .onAppear {
            bleAnalyzer.onSensorPacketReceived = { packet in
                store.isSimulationMode = false
                if let t = packet.temp { store.tempC = t }
                if let l = packet.lux { store.lightLux = l }
                if let r = packet.r, let g = packet.g, let b = packet.b {
                    store.rgb = RGBColor(r: r, g: g, b: b)
                }
                store.measuredSource = "iPhone CoreBluetooth (BLE)"
            }
        }
    }
}

private struct MonitorView: View {
    @EnvironmentObject private var store: FreshnessStore
    @ObservedObject var bleAnalyzer: ColorSensorAnalyzer
    @State private var tempInput: String = ""
    @State private var luxInput: String = ""
    @State private var jsonInput: String = ""
    @State private var colorPickerValue: Color = .purple

    var body: some View {
        let analysis = store.analyze()
        NavigationStack {
            ScrollView {
                VStack(spacing: 14) {
                    if store.isSimulationMode {
                        HStack(alignment: .top, spacing: 8) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .foregroundStyle(.orange)
                            Text("ҚАУІПСІЗ ОҚУ СИМУЛЯЦИЯСЫ РЕЖИМІ: Қазір оқу-зерттеу үлгі деректері көрсетілуде. Нақты өлшеу үшін камераны немесе датчик енгізуін қолданыңыз.")
                                .font(.caption)
                                .foregroundStyle(.primary)
                        }
                        .padding(12)
                        .background(Color.orange.opacity(0.15))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }

                    VStack(alignment: .leading, spacing: 8) {
                        HStack(alignment: .top) {
                            VStack(alignment: .leading) {
                                Text("БАЛҒЫНДЫҚ ИНДЕКСІ")
                                    .font(.caption.weight(.bold))
                                    .foregroundStyle(.mint)
                                Text(analysis.score.map { "\($0)%" } ?? "—")
                                    .font(.system(size: 40, weight: .heavy))
                                    .foregroundStyle(.white)
                            }
                            Spacer()
                            Text(analysis.statusLabel)
                                .font(.caption2.weight(.bold))
                                .padding(.horizontal, 10)
                                .padding(.vertical, 5)
                                .background(analysis.statusColor.opacity(0.25))
                                .foregroundStyle(.white)
                                .clipShape(Capsule())
                        }
                        Text("Өнім: \(store.product.title)\nИндикатор: \(store.indicator.title)\nДереккөз: \(store.measuredSource)")
                            .font(.caption)
                            .foregroundStyle(.white.opacity(0.85))
                    }
                    .padding()
                    .background(
                        LinearGradient(colors: [Color(red: 0.04, green: 0.15, blue: 0.25), Color(red: 0.08, green: 0.27, blue: 0.42)], startPoint: .topLeading, endPoint: .bottomTrailing)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 18))

                    HStack(spacing: 8) {
                        MetricTile(title: "ТЕМПЕРАТУРА", value: store.tempC.map { String(format: "%.1f °C", $0) } ?? "—", sub: "Шек: ≤ \(Int(store.product.idealTempMax)) °C")
                        MetricTile(title: "ЖАРЫҚ", value: store.lightLux.map { "\(Int($0)) лк" } ?? "—", sub: "Оңтайлы: < 50 лк")
                        MetricTile(title: "ИНДИКАТОР pH", value: analysis.ph.map { String(format: "%.1f", $0) } ?? "—", sub: "Норма: \(store.product.idealPhRange.lowerBound)–\(store.product.idealPhRange.upperBound)")
                    }

                    VStack(alignment: .leading, spacing: 8) {
                        Text("БИОЛОГИЯ ЖӘНЕ ХИМИЯ ҚОРЫТЫНДЫСЫ")
                            .font(.caption.weight(.bold))
                            .foregroundStyle(.purple)
                        Text(analysis.biologicalConclusion)
                            .font(.subheadline)
                        if analysis.isReady {
                            HStack {
                                Button("Журналға сақтау") {
                                    store.saveCurrentEntry()
                                }
                                .buttonStyle(.borderedProminent)
                                .tint(.green)
                                Button("Тазалау") {
                                    store.clearMeasurements()
                                }
                                .buttonStyle(.bordered)
                            }
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding()
                    .background(Color(.secondarySystemGroupedBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                    VStack(alignment: .leading, spacing: 10) {
                        Text("Құрылғы параметрлері мен датчиктер")
                            .font(.headline)

                        Picker("Өнім", selection: $store.product) {
                            ForEach(FoodProductProfile.allCases) { p in
                                Text(p.title).tag(p)
                            }
                        }

                        Picker("Табиғи индикатор", selection: $store.indicator) {
                            ForEach(NaturalIndicatorType.allCases) { ind in
                                Text(ind.title).tag(ind)
                            }
                        }

                        HStack(spacing: 10) {
                            TextField("Темп (°C)", text: $tempInput)
                                .keyboardType(.decimalPad)
                                .textFieldStyle(.roundedBorder)
                            TextField("Жарық (лк)", text: $luxInput)
                                .keyboardType(.numberPad)
                                .textFieldStyle(.roundedBorder)
                        }

                        ColorPicker("Түс датчигі (RGB)", selection: $colorPickerValue)

                        Button("Датчик мәндерін талдау") {
                            store.isSimulationMode = false
                            if let t = Double(tempInput.replacingOccurrences(of: ",", with: ".")) {
                                store.tempC = t
                            }
                            if let l = Double(luxInput) {
                                store.lightLux = l
                            }
                            if let comps = UIColor(colorPickerValue).cgColor.components, comps.count >= 3 {
                                store.rgb = RGBColor(r: Int(comps[0] * 255), g: Int(comps[1] * 255), b: Int(comps[2] * 255))
                            }
                            store.measuredSource = "Қолмен / Құрылғыдан енгізілген дерек"
                        }
                        .buttonStyle(.borderedProminent)
                        .frame(maxWidth: .infinity)
                    }
                    .padding()
                    .background(Color(.secondarySystemGroupedBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            Text("CoreBluetooth: \(bleAnalyzer.bleStatus)")
                                .font(.caption.weight(.semibold))
                            Spacer()
                            Button("BLE қосу") {
                                bleAnalyzer.startBleScan()
                            }
                            .buttonStyle(.bordered)
                        }
                        TextField("JSON: {\"temp\":4.2,\"lux\":18,\"r\":124,\"g\":58,\"b\":237}", text: $jsonInput)
                            .textFieldStyle(.roundedBorder)
                        Button("JSON дерегін оқу") {
                            if let data = jsonInput.data(using: .utf8),
                               let packet = try? JSONDecoder().decode(HardwareSensorPacket.self, from: data) {
                                store.isSimulationMode = false
                                if let t = packet.temp { store.tempC = t }
                                if let l = packet.lux { store.lightLux = l }
                                if let r = packet.r, let g = packet.g, let b = packet.b {
                                    store.rgb = RGBColor(r: r, g: g, b: b)
                                }
                                store.measuredSource = "Цифрлық датчик JSON пакеті"
                            }
                        }
                        .buttonStyle(.bordered)
                    }
                    .padding()
                    .background(Color(.secondarySystemGroupedBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Оқу-зерттеу үлгілері (Қауіпсіз симуляция)")
                            .font(.subheadline.weight(.semibold))
                        ForEach(EducationalScenario.presets) { sc in
                            Button {
                                store.applySimulation(sc)
                            } label: {
                                HStack {
                                    Text("🧪 \(sc.label)")
                                    Spacer()
                                }
                            }
                            .buttonStyle(.bordered)
                        }
                    }
                    .padding()
                    .background(Color(.secondarySystemGroupedBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                }
                .padding()
            }
            .background(Color(.systemGroupedBackground))
            .navigationTitle("Монитор")
        }
    }
}

private struct MetricTile: View {
    let title: String
    let value: String
    let sub: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.caption2.weight(.bold))
                .foregroundStyle(.secondary)
            Text(value)
                .font(.headline.weight(.bold))
            Text(sub)
                .font(.caption2)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(10)
        .background(Color(.secondarySystemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

private struct SciencePillarsView: View {
    private let pillars: [(tag: String, color: Color, title: String, body: String)] = [
        ("ФИЗИКА", .blue, "Температура, жарық және RGB түс датчиктері",
         "• DS18B20/NTC температура датчигі +2…+6 °C сақтау режимін бақылайды.\n• BH1750 жарық датчигі фотототығу қаупін (люкс) өлшейді.\n• TCS34725 RGB датчигі шағылған қызыл, жасыл, көк сәулені тіркейді."),
        ("ХИМИЯ", .purple, "Табиғи индикаторлар және қышқылдық (pH) өзгерісі",
         "• Қызыл қырыққабат антоцианы: қышқылда қызыл, бейтарапта күлгін, сілтіліде көк-жасыл.\n• Сүт ашығанда сүт қышқылы жиналып pH төмендейді; ет/балық бұзылғанда ұшқыш аминдер (NH3) жиналып pH өседі."),
        ("БИОЛОГИЯ", .green, "Микроағзалардың тағам бұзылуындағы рөлі",
         "• 4 °C – 60 °C қауіпті аймақта Lactobacillus, Pseudomonas бактериялары жылдам көбейеді.\n• Табиғи индикатор осы биохимиялық процесті лезде көрсетеді."),
        ("ЦИФРЛЫҚ ТЕХНОЛОГИЯ", .orange, "Табиғи индикатор түсін цифрлық жүйемен байланыстыру",
         "iPhone камерасы / RGB датчик → RGB→HSV Hue (0–360°) → калибрлеу қисығы (pH) → Температура + Жарық + pH біріктірілген Балғындық индексі (0–100%).")
    ]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 12) {
                    ForEach(pillars, id: \.tag) { item in
                        VStack(alignment: .leading, spacing: 6) {
                            Text(item.tag)
                                .font(.caption2.weight(.bold))
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(item.color.opacity(0.15))
                                .foregroundStyle(item.color)
                                .clipShape(Capsule())
                            Text(item.title)
                                .font(.headline)
                            Text(item.body)
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding()
                        .background(Color(.secondarySystemGroupedBackground))
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                    }
                }
                .padding()
            }
            .background(Color(.systemGroupedBackground))
            .navigationTitle("Ғылым негізі")
        }
    }
}

private struct JournalHistoryView: View {
    @EnvironmentObject private var store: FreshnessStore

    var body: some View {
        NavigationStack {
            List {
                if store.logs.isEmpty {
                    Text("Әзірге өлшем жазбалары жоқ. «Монитор» бөлімінде өлшем жасап сақтаңыз.")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                } else {
                    ForEach(store.logs) { item in
                        VStack(alignment: .leading, spacing: 6) {
                            HStack {
                                Text(item.productName)
                                    .font(.headline)
                                Spacer()
                                Text("\(item.score)%")
                                    .font(.caption.weight(.bold))
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 4)
                                    .background(item.score >= 75 ? Color.green.opacity(0.2) : (item.score >= 45 ? Color.orange.opacity(0.2) : Color.red.opacity(0.2)))
                                    .clipShape(Capsule())
                            }
                            Text("\(item.indicatorName) • pH: \(item.ph.map { String(format: "%.1f", $0) } ?? "—")")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                            if item.isSimulation {
                                Text("Симуляциялық үлгі")
                                    .font(.caption2.weight(.bold))
                                    .foregroundStyle(.orange)
                            }
                        }
                    }
                    .onDelete(perform: store.deleteLogs)
                }
            }
            .navigationTitle("Журнал")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    ShareLink(item: store.exportCsvString()) {
                        Label("CSV экспорт", systemImage: "square.and.arrow.up")
                    }
                }
            }
        }
    }
}
