import Foundation
import SwiftUI

enum NaturalIndicatorType: String, CaseIterable, Identifiable, Codable {
    case anthocyanin = "anthocyanin"
    case curcumin = "curcumin"
    case betalain = "betalain"

    var id: String { rawValue }

    var title: String {
        switch self {
        case .anthocyanin: return "Қызыл қырыққабат (Антоциан)"
        case .curcumin: return "Куркумин (Зімбір/Куркума)"
        case .betalain: return "Қызылша сығындысы (Беталаин)"
        }
    }

    var descriptionText: String {
        switch self {
        case .anthocyanin:
            return "Қышқылда қызыл/алқызыл (pH 2–4), бейтарапта күлгін (pH 6–7), сілтіліде көк-жасыл (pH 8–10)."
        case .curcumin:
            return "Қышқыл және бейтарап ортада сары (pH ≤ 7), сілтілі ортада қызыл-қоңыр (pH > 8)."
        case .betalain:
            return "Бейтарап/әлсіз қышқыл ортада қанық күлгін-қызыл (pH 4–7), жоғары сілтіліде сарғыш-қоңыр (pH > 9)."
        }
    }

    func mapHueToPh(_ hue: Double) -> Double {
        switch self {
        case .anthocyanin:
            if hue >= 330 || hue <= 25 { return 3.2 }
            if hue >= 260 { return 6.4 }
            if hue >= 195 { return 7.9 }
            if hue >= 90 { return 9.6 }
            return 11.0
        case .curcumin:
            if hue >= 40 && hue <= 72 { return 6.2 }
            if hue >= 20 && hue < 40 { return 8.2 }
            if hue < 20 || hue >= 335 { return 9.4 }
            return 7.0
        case .betalain:
            if hue >= 300 || hue <= 15 { return 6.2 }
            if hue >= 20 && hue <= 65 { return 9.5 }
            return 7.4
        }
    }

    var calibratedScale: [(label: String, r: Int, g: Int, b: Int)] {
        switch self {
        case .anthocyanin:
            return [
                ("pH 2–3 (Қышқыл)", 225, 29, 72),
                ("pH 4–5 (Әлсіз қышқыл)", 236, 72, 153),
                ("pH 6–7 (Бейтарап)", 124, 58, 237),
                ("pH 8 (Сілтілі)", 2, 132, 199),
                ("pH 9–10 (Күшті сілтілі)", 16, 185, 129)
            ]
        case .curcumin:
            return [
                ("pH 5–7 (Сары — Қалыпты)", 234, 179, 8),
                ("pH 8 (Қызғылт сары)", 249, 115, 22),
                ("pH 9+ (Қызыл-қоңыр — Сілті)", 185, 28, 28)
            ]
        case .betalain:
            return [
                ("pH 4–7 (Қызыл-күлгін)", 190, 24, 93),
                ("pH 8 (Қоңырқай)", 154, 52, 18),
                ("pH 9.5+ (Сары-қоңыр)", 202, 138, 4)
            ]
        }
    }
}

enum FoodProductProfile: String, CaseIterable, Identifiable, Codable {
    case milk, meat, fish, juice

    var id: String { rawValue }

    var title: String {
        switch self {
        case .milk: return "Сүт және сүт өнімдері"
        case .meat: return "Ет және құс өнімдері"
        case .fish: return "Балық және теңіз өнімдері"
        case .juice: return "Жеміс-жидек / Шырын"
        }
    }

    var idealTempMax: Double {
        switch self {
        case .milk: return 6.0
        case .meat: return 4.0
        case .fish: return 2.0
        case .juice: return 8.0
        }
    }

    var idealPhRange: ClosedRange<Double> {
        switch self {
        case .milk: return 6.4...6.8
        case .meat: return 5.5...6.4
        case .fish: return 6.0...6.8
        case .juice: return 3.3...4.5
        }
    }
}

struct RGBColor: Codable, Equatable {
    var r: Int
    var g: Int
    var b: Int

    var hexString: String {
        String(format: "#%02X%02X%02X", max(0, min(255, r)), max(0, min(255, g)), max(0, min(255, b)))
    }

    var swiftUIColor: Color {
        Color(red: Double(r) / 255.0, green: Double(g) / 255.0, blue: Double(b) / 255.0)
    }

    var hsv: (h: Double, s: Double, v: Double) {
        let rn = Double(r) / 255.0
        let gn = Double(g) / 255.0
        let bn = Double(b) / 255.0
        let maxV = max(rn, gn, bn)
        let minV = min(rn, gn, bn)
        let delta = maxV - minV

        var hue: Double = 0
        if delta != 0 {
            if maxV == rn {
                hue = ((gn - bn) / delta).truncatingRemainder(dividingBy: 6)
            } else if maxV == gn {
                hue = (bn - rn) / delta + 2
            } else {
                hue = (rn - gn) / delta + 4
            }
            hue *= 60
            if hue < 0 { hue += 360 }
        }
        let sat = maxV == 0 ? 0 : (delta / maxV) * 100
        let val = maxV * 100
        return (hue.rounded(), sat.rounded(), val.rounded())
    }
}

struct FreshnessAnalysis {
    let isReady: Bool
    let score: Int?
    let statusLabel: String
    let ph: Double?
    let hue: Double?
    let biologicalConclusion: String
    let statusColor: Color
}

struct FreshnessLogEntry: Identifiable, Codable {
    let id: UUID
    let timestamp: Date
    let productName: String
    let indicatorName: String
    let tempC: Double?
    let lightLux: Double?
    let hexColor: String?
    let ph: Double?
    let score: Int
    let status: String
    let isSimulation: Bool
}

struct EducationalScenario: Identifiable {
    let id: String
    let label: String
    let product: FoodProductProfile
    let indicator: NaturalIndicatorType
    let tempC: Double
    let lightLux: Double
    let rgb: RGBColor

    static let presets: [EducationalScenario] = [
        EducationalScenario(
            id: "fresh_milk",
            label: "Балғын сүт (+4 °C, қараңғы)",
            product: .milk,
            indicator: .anthocyanin,
            tempC: 4.0,
            lightLux: 18,
            rgb: RGBColor(r: 124, g: 58, b: 237)
        ),
        EducationalScenario(
            id: "spoiled_milk",
            label: "Ашыған сүт (+23 °C, жарық)",
            product: .milk,
            indicator: .anthocyanin,
            tempC: 23.0,
            lightLux: 420,
            rgb: RGBColor(r: 225, g: 29, b: 72)
        ),
        EducationalScenario(
            id: "spoiled_fish",
            label: "Бұзыла бастаған балық (+16 °C)",
            product: .fish,
            indicator: .anthocyanin,
            tempC: 16.5,
            lightLux: 190,
            rgb: RGBColor(r: 16, g: 185, b: 129)
        )
    ]
}

final class FreshnessStore: ObservableObject {
    @Published var product: FoodProductProfile = .milk
    @Published var indicator: NaturalIndicatorType = .anthocyanin
    @Published var tempC: Double? = nil
    @Published var lightLux: Double? = nil
    @Published var rgb: RGBColor? = nil
    @Published var measuredSource: String = "Өлшенбеген (Құрылғы/Камера күтілуде)"
    @Published var isSimulationMode: Bool = false
    @Published var logs: [FreshnessLogEntry] = []

    private let logsKey = "freshguard_ios_swift_logs"

    init() {
        loadLogs()
    }

    func analyze() -> FreshnessAnalysis {
        let phValue = rgb.map { indicator.mapHueToPh($0.hsv.h) }
        let hueValue = rgb?.hsv.h

        if tempC == nil && lightLux == nil && rgb == nil {
            return FreshnessAnalysis(
                isReady: false,
                score: nil,
                statusLabel: "Деректер енгізілмеген",
                ph: nil,
                hue: nil,
                biologicalConclusion: "Талдау үшін датчик мәндерін енгізіңіз немесе iPhone камерасымен индикатор түсін оқыңыз.",
                statusColor: .secondary
            )
        }

        var score = 100.0
        var reasons: [String] = []

        if let t = tempC {
            if t > product.idealTempMax {
                let excess = t - product.idealTempMax
                score -= min(45.0, excess * 3.2)
                reasons.append("Температура (+\(String(format: "%.1f", t)) °C) шекті нормадан (+\(String(format: "%.0f", product.idealTempMax)) °C) жоғары — бактериялардың көбею жылдамдығы артады.")
            } else if t < 0 {
                score -= 8.0
                reasons.append("Мұздату температурасы (< 0 °C) микроағзаларды тежейді.")
            }
        }

        if let lux = lightLux, lux > 100 {
            score -= min(20.0, (lux - 100.0) / 25.0)
            reasons.append("Жарық деңгейі (\(Int(lux)) лк) жоғары — фотототығу мен дәрумендердің ыдырау қаупі бар.")
        }

        if let ph = phValue {
            if ph < product.idealPhRange.lowerBound {
                let diff = product.idealPhRange.lowerBound - ph
                score -= min(50.0, diff * 18.0)
                reasons.append("Табиғи индикатор қышқылдануды көрсетті (pH ≈ \(String(format: "%.1f", ph))): Lactobacillus бактериялары қышқыл бөлуде.")
            } else if ph > product.idealPhRange.upperBound {
                let diff = ph - product.idealPhRange.upperBound
                score -= min(50.0, diff * 18.0)
                reasons.append("Табиғи индикатор сілтіленуді көрсетті (pH ≈ \(String(format: "%.1f", ph))): Pseudomonas ақуызды ыдыратып, ұшқыш аминдер (NH3) бөлуде.")
            }
        }

        let finalScore = max(0, min(100, Int(score.rounded())))
        let label: String
        let color: Color
        if finalScore < 45 {
            label = "Бұзылу қаупі жоғары!"
            color = .red
        } else if finalScore < 75 {
            label = "Бақылау қажет • Ауытқу байқалды"
            color = .orange
        } else {
            label = "Балғын • Сақтау жағдайы қалыпты"
            color = .green
        }

        let conclusion = reasons.isEmpty
            ? "Температура, жарық және қышқылдық-негіздік тепе-теңдік микроағзалар өсуін тежейтін оңтайлы деңгейде."
            : reasons.joined(separator: " ")

        return FreshnessAnalysis(
            isReady: true,
            score: finalScore,
            statusLabel: label,
            ph: phValue,
            hue: hueValue,
            biologicalConclusion: conclusion,
            statusColor: color
        )
    }

    func applySimulation(_ scenario: EducationalScenario) {
        isSimulationMode = true
        product = scenario.product
        indicator = scenario.indicator
        tempC = scenario.tempC
        lightLux = scenario.lightLux
        rgb = scenario.rgb
        measuredSource = "ҚАУІПСІЗ ОҚУ СИМУЛЯЦИЯСЫ (\(scenario.label))"
    }

    func clearMeasurements() {
        isSimulationMode = false
        tempC = nil
        lightLux = nil
        rgb = nil
        measuredSource = "Өлшенбеген (Құрылғы/Камера күтілуде)"
    }

    func saveCurrentEntry() {
        let analysis = analyze()
        guard analysis.isReady, let score = analysis.score else { return }
        let entry = FreshnessLogEntry(
            id: UUID(),
            timestamp: Date(),
            productName: product.title,
            indicatorName: indicator.title,
            tempC: tempC,
            lightLux: lightLux,
            hexColor: rgb?.hexString,
            ph: analysis.ph,
            score: score,
            status: analysis.statusLabel,
            isSimulation: isSimulationMode
        )
        logs.insert(entry, at: 0)
        persistLogs()
    }

    func deleteLogs(at offsets: IndexSet) {
        logs.remove(atOffsets: offsets)
        persistLogs()
    }

    func exportCsvString() -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "kk_KZ")
        formatter.dateStyle = .short
        formatter.timeStyle = .short

        var lines = ["\u{FEFF}Уақыт;Өнім;Индикатор;Температура (°C);Жарық (лк);Түс HEX;pH;Балғындық (%);Күйі;Режим"]
        for l in logs {
            let t = l.tempC.map { String(format: "%.1f", $0) } ?? ""
            let lux = l.lightLux.map { String(Int($0)) } ?? ""
            let ph = l.ph.map { String(format: "%.1f", $0) } ?? ""
            let mode = l.isSimulation ? "Симуляция" : "Нақты өлшем"
            lines.append("\"\(formatter.string(from: l.timestamp))\";\"\(l.productName)\";\"\(l.indicatorName)\";\(t);\(lux);\"\(l.hexColor ?? "")\";\(ph);\(l.score);\"\(l.status)\";\"\(mode)\"")
        }
        return lines.joined(separator: "\n")
    }

    private func persistLogs() {
        if let data = try? JSONEncoder().encode(logs) {
            UserDefaults.standard.set(data, forKey: logsKey)
        }
    }

    private func loadLogs() {
        if let data = UserDefaults.standard.data(forKey: logsKey),
           let decoded = try? JSONDecoder().decode([FreshnessLogEntry].self, from: data) {
            logs = decoded
        }
    }
}
