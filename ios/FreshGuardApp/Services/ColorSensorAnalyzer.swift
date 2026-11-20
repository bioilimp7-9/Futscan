import Foundation
import UIKit
import CoreBluetooth

struct HardwareSensorPacket: Decodable {
    let temp: Double?
    let lux: Double?
    let r: Int?
    let g: Int?
    let b: Int?
}

final class ColorSensorAnalyzer: NSObject, ObservableObject, CBCentralManagerDelegate, CBPeripheralDelegate {
    @Published var bleStatus: String = "Қосылмаған"
    @Published var discoveredDeviceName: String?

    private var centralManager: CBCentralManager?
    private var connectedPeripheral: CBPeripheral?
    var onSensorPacketReceived: ((HardwareSensorPacket) -> Void)?

    /// Extracts average RGB color from the center 12% region of an iPhone camera photo
    static func extractCenterAverageRGB(from image: UIImage) -> RGBColor? {
        guard let cgImage = image.cgImage else { return nil }
        let width = cgImage.width
        let height = cgImage.height
        guard width > 0, height > 0 else { return nil }

        let boxSize = max(12, Int(Double(min(width, height)) * 0.12))
        let startX = max(0, width / 2 - boxSize / 2)
        let startY = max(0, height / 2 - boxSize / 2)
        let cropRect = CGRect(x: startX, y: startY, width: min(boxSize, width - startX), height: min(boxSize, height - startY))

        guard let cropped = cgImage.cropping(to: cropRect) else { return nil }
        let cw = cropped.width
        let ch = cropped.height
        var pixelData = [UInt8](repeating: 0, count: cw * ch * 4)
        let colorSpace = CGColorSpaceCreateDeviceRGB()

        guard let context = CGContext(
            data: &pixelData,
            width: cw,
            height: ch,
            bitsPerComponent: 8,
            bytesPerRow: cw * 4,
            space: colorSpace,
            bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
        ) else { return nil }

        context.draw(cropped, in: CGRect(x: 0, y: 0, width: cw, height: ch))

        var rSum = 0
        var gSum = 0
        var bSum = 0
        let totalPixels = cw * ch
        guard totalPixels > 0 else { return nil }

        for i in stride(from: 0, to: pixelData.count, by: 4) {
            rSum += Int(pixelData[i])
            gSum += Int(pixelData[i + 1])
            bSum += Int(pixelData[i + 2])
        }

        return RGBColor(
            r: rSum / totalPixels,
            g: gSum / totalPixels,
            b: bSum / totalPixels
        )
    }

    func startBleScan() {
        bleStatus = "Bluetooth ізделуде..."
        centralManager = CBCentralManager(delegate: self, queue: .main)
    }

    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .poweredOn:
            bleStatus = "Құрылғы ізделуде (ESP32 / BLE)..."
            central.scanForPeripherals(withServices: nil, options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
        case .unauthorized:
            bleStatus = "Bluetooth рұқсаты берілмеген"
        case .poweredOff:
            bleStatus = "iPhone Bluetooth өшірулі"
        default:
            bleStatus = "Bluetooth дайын емес"
        }
    }

    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String: Any], rssi RSSI: NSNumber) {
        if let name = peripheral.name, name.localizedCaseInsensitiveContains("Fresh") || name.localizedCaseInsensitiveContains("ESP32") {
            discoveredDeviceName = name
            bleStatus = "Қосылуда: \(name)"
            connectedPeripheral = peripheral
            peripheral.delegate = self
            central.stopScan()
            central.connect(peripheral, options: nil)
        }
    }

    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        bleStatus = "Қосылды: \(peripheral.name ?? "Датчик")"
        peripheral.discoverServices(nil)
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        peripheral.services?.forEach { service in
            peripheral.discoverCharacteristics(nil, for: service)
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        service.characteristics?.forEach { characteristic in
            if characteristic.properties.contains(.notify) {
                peripheral.setNotifyValue(true, for: characteristic)
            }
            if characteristic.properties.contains(.read) {
                peripheral.readValue(for: characteristic)
            }
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        guard let data = characteristic.value,
              let packet = try? JSONDecoder().decode(HardwareSensorPacket.self, from: data) else {
            return
        }
        DispatchQueue.main.async {
            self.onSensorPacketReceived?(packet)
        }
    }
}
