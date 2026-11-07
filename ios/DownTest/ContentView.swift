
import SwiftUI
import AVFoundation

struct ContentView: View {
    @State private var auraMode = false
    @State private var downResult = 0
    @State private var auraResult = 0
    @State private var downHistory: [Int] = []
    @State private var auraHistory: [Int] = []

    @StateObject private var camera = CameraModel()

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            ScrollView {
                VStack(spacing: 22) {
                    Text("DOWN TEST")
                        .font(.largeTitle.bold())
                        .foregroundColor(.white)

                    Picker("Režim", selection: $auraMode) {
                        Text("DOWN TEST").tag(false)
                        Text("AURA TEST").tag(true)
                    }
                    .pickerStyle(.segmented)

                    CameraPreview(session: camera.session)
                        .frame(height: 280)
                        .clipShape(RoundedRectangle(cornerRadius: 20))

                    Text(auraMode ? "AURA SCORE" : "DOWN TEST SCORE")
                        .font(.headline)
                        .foregroundColor(.white)

                    Text("\(auraMode ? auraResult : downResult)")
                        .font(.system(size: 65, weight: .bold))
                        .foregroundColor(.white)

                    Button {
                        if auraMode {
                            auraResult = Int.random(in: 0...1000)
                            auraHistory.append(auraResult)
                        } else {
                            downResult = Int.random(in: 0...100)
                            downHistory.append(downResult)
                        }
                    } label: {
                        Text("SKENOVAT")
                            .font(.title2.bold())
                            .foregroundColor(.black)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.white)
                            .clipShape(RoundedRectangle(cornerRadius: 15))
                    }

                    Text("HISTORIE")
                        .font(.title2.bold())
                        .foregroundColor(.white)

                    let history = auraMode ? auraHistory : downHistory

                    if history.isEmpty {
                        Text("Zatím žádné výsledky")
                            .foregroundColor(.gray)
                    } else {
                        ForEach(Array(history.reversed().enumerated()), id: \.offset) { _, value in
                            Text("\(value)")
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(10)
                                .background(Color.gray.opacity(0.25))
                                .clipShape(RoundedRectangle(cornerRadius: 10))
                        }
                    }
                }
                .padding()
            }
        }
        .onAppear {
            camera.start()
        }
        .onDisappear {
            camera.stop()
        }
    }
}

final class CameraModel: ObservableObject {
    let session = AVCaptureSession()

    private let queue = DispatchQueue(label: "camera.queue")
    private var configured = false

    func start() {
        queue.async {
            guard !self.configured else {
                if !self.session.isRunning {
                    self.session.startRunning()
                }
                return
            }

            guard let device = AVCaptureDevice.default(
                .builtInWideAngleCamera,
                for: .video,
                position: .front
            ) else {
                return
            }

            do {
                let input = try AVCaptureDeviceInput(device: device)

                self.session.beginConfiguration()
                self.session.sessionPreset = .high

                if self.session.canAddInput(input) {
                    self.session.addInput(input)
                }

                self.session.commitConfiguration()
                self.configured = true
                self.session.startRunning()
            } catch {
                print("Camera error: \(error)")
            }
        }
    }

    func stop() {
        queue.async {
            if self.session.isRunning {
                self.session.stopRunning()
            }
        }
    }
}

struct CameraPreview: UIViewRepresentable {
    let session: AVCaptureSession

    func makeUIView(context: Context) -> CameraView {
        let view = CameraView()
        view.previewLayer.session = session
        view.previewLayer.videoGravity = .resizeAspectFill
        return view
    }

    func updateUIView(_ uiView: CameraView, context: Context) {
        uiView.previewLayer.session = session
    }
}

final class CameraView: UIView {
    override class var layerClass: AnyClass {
        AVCaptureVideoPreviewLayer.self
    }

    var previewLayer: AVCaptureVideoPreviewLayer {
        layer as! AVCaptureVideoPreviewLayer
    }
}
