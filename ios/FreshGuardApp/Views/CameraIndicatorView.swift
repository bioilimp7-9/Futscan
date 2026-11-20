import SwiftUI
import UIKit

struct CameraIndicatorView: View {
    @EnvironmentObject private var store: FreshnessStore
    @State private var capturedImage: UIImage?
    @State private var showCameraSheet = false
    @State private var showLibrarySheet = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Табиғи индикаторды таңдаңыз")
                            .font(.subheadline.weight(.semibold))
                        Picker("Индикатор", selection: $store.indicator) {
                            ForEach(NaturalIndicatorType.allCases) { item in
                                Text(item.title).tag(item)
                            }
                        }
                        .pickerStyle(.menu)

                        Text(store.indicator.descriptionText)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    .padding()
                    .background(Color(.secondarySystemGroupedBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                    ZStack {
                        RoundedRectangle(cornerRadius: 18)
                            .fill(Color.black.opacity(0.9))
                            .frame(height: 240)

                        if let image = capturedImage {
                            Image(uiImage: image)
                                .resizable()
                                .scaledToFill()
                                .frame(height: 240)
                                .clipShape(RoundedRectangle(cornerRadius: 18))
                        } else {
                            VStack(spacing: 8) {
                                Image(systemName: "camera.viewfinder")
                                    .font(.system(size: 40))
                                    .foregroundStyle(.white.opacity(0.7))
                                Text("Табиғи индикатор жолағын iPhone камерасына бағыттаңыз")
                                    .font(.caption)
                                    .foregroundStyle(.white.opacity(0.75))
                                    .multilineTextAlignment(.center)
                                    .padding(.horizontal)
                            }
                        }

                        Circle()
                            .strokeBorder(Color.white, lineWidth: 2.5)
                            .frame(width: 48, height: 48)
                    }

                    HStack(spacing: 12) {
                        Button {
                            showCameraSheet = true
                        } label: {
                            Label("iPhone Камера", systemImage: "camera.fill")
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                        }
                        .buttonStyle(.borderedProminent)
                        .disabled(!UIImagePickerController.isSourceTypeAvailable(.camera))

                        Button {
                            showLibrarySheet = true
                        } label: {
                            Label("Фото таңдау", systemImage: "photo.on.rectangle")
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                        }
                        .buttonStyle(.bordered)
                    }

                    let analysis = store.analyze()
                    HStack(spacing: 14) {
                        RoundedRectangle(cornerRadius: 12)
                            .fill(store.rgb?.swiftUIColor ?? Color(.systemGray4))
                            .frame(width: 56, height: 56)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.primary.opacity(0.15), lineWidth: 1)
                            )

                        VStack(alignment: .leading, spacing: 4) {
                            Text("АНЫҚТАЛҒАН ТҮС ПЕН pH")
                                .font(.caption2.weight(.bold))
                                .foregroundStyle(.secondary)
                            Text(analysis.ph != nil ? String(format: "pH ≈ %.1f", analysis.ph!) : "Түс алынбаған")
                                .font(.title3.weight(.bold))
                            if let rgb = store.rgb, let hue = analysis.hue {
                                Text("RGB(\(rgb.r), \(rgb.g), \(rgb.b)) • \(rgb.hexString) • Hue \(Int(hue))°")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                        }
                        Spacer()
                    }
                    .padding()
                    .background(Color(.secondarySystemGroupedBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Индикатордың калибрленген түс шкаласы")
                            .font(.subheadline.weight(.semibold))
                        ForEach(store.indicator.calibratedScale, id: \.label) { swatch in
                            Button {
                                store.isSimulationMode = false
                                store.rgb = RGBColor(r: swatch.r, g: swatch.g, b: swatch.b)
                                store.measuredSource = "Калибрленген үлгі (\(swatch.label))"
                            } label: {
                                HStack(spacing: 12) {
                                    RoundedRectangle(cornerRadius: 6)
                                        .fill(RGBColor(r: swatch.r, g: swatch.g, b: swatch.b).swiftUIColor)
                                        .frame(width: 22, height: 22)
                                    Text(swatch.label)
                                        .font(.subheadline)
                                        .foregroundStyle(.primary)
                                    Spacer()
                                }
                                .padding(10)
                                .background(Color(.tertiarySystemGroupedBackground))
                                .clipShape(RoundedRectangle(cornerRadius: 10))
                            }
                        }
                    }
                    .padding()
                    .background(Color(.secondarySystemGroupedBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                }
                .padding()
            }
            .background(Color(.systemGroupedBackground))
            .navigationTitle("Индикатор")
            .sheet(isPresented: $showCameraSheet) {
                ImagePickerSheet(sourceType: .camera) { img in
                    processImage(img, label: "iPhone Камера фотосы")
                }
            }
            .sheet(isPresented: $showLibrarySheet) {
                ImagePickerSheet(sourceType: .photoLibrary) { img in
                    processImage(img, label: "iPhone Фотосурет талдауы")
                }
            }
        }
    }

    private func processImage(_ image: UIImage, label: String) {
        capturedImage = image
        if let rgb = ColorSensorAnalyzer.extractCenterAverageRGB(from: image) {
            store.isSimulationMode = false
            store.rgb = rgb
            store.measuredSource = label
        }
    }
}

private struct ImagePickerSheet: UIViewControllerRepresentable {
    let sourceType: UIImagePickerController.SourceType
    let onPicked: (UIImage) -> Void
    @Environment(\.dismiss) private var dismiss

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.sourceType = sourceType
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}

    final class Coordinator: NSObject, UINavigationControllerDelegate, UIImagePickerControllerDelegate {
        private let parent: ImagePickerSheet
        init(_ parent: ImagePickerSheet) { self.parent = parent }

        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
            if let img = info[.originalImage] as? UIImage {
                parent.onPicked(img)
            }
            parent.dismiss()
        }

        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            parent.dismiss()
        }
    }
}
