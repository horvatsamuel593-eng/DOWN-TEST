
import SwiftUI
import AVFoundation
import UIKit

enum AppScreen {
    case menu
    case downChoice
    case questions
    case camera
    case result
    case history
}

struct GameMode: Identifiable {
    let id: Int
    let name: String
    let premium: Bool
}

struct Question {
    let title: String
    let answers: [(String, Int)]
}

struct ContentView: View {
    @State private var screen: AppScreen = .menu
    @State private var selectedMode = "DOWN TEST"

    @State private var result = 0
    @State private var auraResult = 0
    @State private var history: [String: [Int]] = [:]

    @State private var questionOrder: [Int] = []
    @State private var questionIndex = 0
    @State private var selectedAnswers: [Int: Int] = [:]

    @State private var scanning = false
    @State private var scanProgress = 0
    @State private var showPremiumAlert = false

    @StateObject private var camera = CameraModel()

    private let modes = [
        GameMode(id: 1, name: "DOWN TEST", premium: false),
        GameMode(id: 2, name: "AURA TEST", premium: false),
        GameMode(id: 3, name: "GOONER TEST", premium: false),
        GameMode(id: 4, name: "TROLL TEST", premium: true),
        GameMode(id: 5, name: "GIGACHAD TEST", premium: true),
        GameMode(id: 6, name: "BRAINROT TEST", premium: true),
        GameMode(id: 7, name: "NPC TEST", premium: true),
        GameMode(id: 8, name: "SIGMA TEST", premium: true)
    ]

    private let questions: [Question] = [
        Question(
            title: "Jak moc posloucháš songu Jan Koller?",
            answers: [
                ("Vždycky", 35),
                ("Občas", 35),
                ("Každý týden", 35),
                ("Vůbec neposlouchám", 35)
            ]
        ),
        Question(
            title: "Na jaké YouTubery se koukáš nebo koukal?",
            answers: [
                ("Fizi", 50),
                ("Tary", 45),
                ("Porty", 40),
                ("Dodo", 35)
            ]
        ),
        Question(
            title: "Víš, co znamená, když ti někdo řekne, že jsi down?",
            answers: [
                ("Vím na 100 %", 40),
                ("Asi jo", 36),
                ("Asi ne", 33),
                ("Vůbec nevím", 30)
            ]
        ),
        Question(
            title: "Jak často říkáš 67?",
            answers: [
                ("Hodně často", 35),
                ("Občas", 35),
                ("Řekl jsem to jenom párkrát", 35),
                ("Nikdy jsem to neřekl", 30)
            ]
        ),
        Question(
            title: "Kolik si myslíš, že ti vyjde výsledek DOWN TESTU?",
            answers: [
                ("Míň než 50", 50),
                ("Víc než 50", 40)
            ]
        )
    ]

    private let background = Color.black
    private let panel = Color.white.opacity(0.10)

    var body: some View {
        ZStack {
            background.ignoresSafeArea()

            Group {
                switch screen {
                case .menu:
                    menuView
                case .downChoice:
                    downChoiceView
                case .questions:
                    questionsView
                case .camera:
                    cameraView
                case .result:
                    resultView
                case .history:
                    historyView
                }
            }
        }
        .preferredColorScheme(.dark)
        .alert("PREMIUM", isPresented: $showPremiumAlert) {
            Button("OK", role: .cancel) { }
        } message: {
            Text("Tento režim je zatím zamčený.")
        }
    }

    private var menuView: some View {
        ScrollView {
            VStack(spacing: 18) {
                Text("DOWN TEST")
                    .font(.system(size: 36, weight: .black))
                    .foregroundColor(.white)
                    .padding(.top, 25)

                Text("VYBER SI REŽIM")
                    .font(.headline)
                    .foregroundColor(.gray)

                ForEach(modes) { mode in
                    Button {
                        if mode.premium {
                            showPremiumAlert = true
                        } else {
                            selectedMode = mode.name
                            if mode.name == "DOWN TEST" {
                                screen = .downChoice
                            } else {
                                screen = .camera
                            }
                        }
                    } label: {
                        HStack {
                            Text(mode.name)
                                .font(.title3.bold())

                            Spacer()

                            if mode.premium {
                                Image(systemName: "lock.fill")
                            } else {
                                Image(systemName: "chevron.right")
                            }
                        }
                        .foregroundColor(.white)
                        .padding()
                        .background(panel)
                        .clipShape(RoundedRectangle(cornerRadius: 15))
                    }
                }

                Button {
                    screen = .history
                } label: {
                    Label("HISTORIE", systemImage: "clock.arrow.circlepath")
                        .font(.headline)
                        .foregroundColor(.black)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 15))
                }

                Text("Je to jen joke, neberte to vážně.")
                    .font(.footnote)
                    .foregroundColor(.gray)
                    .padding(.top, 10)
            }
            .padding()
        }
    }

    private var downChoiceView: some View {
        VStack(spacing: 22) {
            Text("DOWN TEST")
                .font(.largeTitle.bold())
                .foregroundColor(.white)

            Text("VYBER SI ZPŮSOB TESTOVÁNÍ")
                .foregroundColor(.gray)

            Button {
                screen = .camera
            } label: {
                menuButton("SCAN", icon: "camera.fill")
            }

            Button {
                questionOrder = Array(questions.indices).shuffled()
                questionIndex = 0
                selectedAnswers = [:]
                screen = .questions
            } label: {
                menuButton("OTÁZKY", icon: "questionmark.circle.fill")
            }

            Button("ZPĚT") {
                screen = .menu
            }
            .foregroundColor(.gray)
        }
        .padding()
    }

    private var questionsView: some View {
        VStack(spacing: 18) {
            HStack {
                Button("ZPĚT") {
                    if questionIndex > 0 {
                        questionIndex -= 1
                    } else {
                        screen = .downChoice
                    }
                }
                .foregroundColor(.white)

                Spacer()

                Text("\(questionIndex + 1) / 5")
                    .foregroundColor(.gray)
            }

            Spacer()

            let questionNumber = questionOrder[questionIndex]
            let question = questions[questionNumber]

            Text("DOWN TEST")
                .font(.largeTitle.bold())
                .foregroundColor(.white)

            Text(question.title)
                .font(.title2.bold())
                .foregroundColor(.white)
                .multilineTextAlignment(.center)

            ForEach(question.answers.indices, id: \.self) { index in
                Button {
                    selectedAnswers[questionNumber] = index
                } label: {
                    HStack {
                        Text(question.answers[index].0)
                        Spacer()
                        if selectedAnswers[questionNumber] == index {
                            Image(systemName: "checkmark.circle.fill")
                        }
                    }
                    .font(.headline)
                    .foregroundColor(.white)
                    .padding()
                    .background(
                        selectedAnswers[questionNumber] == index
                        ? Color.blue.opacity(0.6)
                        : panel
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 13))
                }
            }

            Button {
                if selectedAnswers[questionNumber] != nil {
                    if questionIndex < 4 {
                        questionIndex += 1
                    } else {
                        let score = questionOrder.reduce(0) { total, number in
                            let answerIndex = selectedAnswers[number]!
                            return total + questions[number].answers[answerIndex].1
                        }
                        result = score
                        saveResult(score)
                        screen = .result
                    }
                }
            } label: {
                Text(questionIndex == 4 ? "VYHODNOTIT" : "DALŠÍ")
                    .font(.headline.bold())
                    .foregroundColor(.black)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 13))
            }
            .opacity(selectedAnswers[questionNumber] == nil ? 0.4 : 1)

            Spacer()
        }
        .padding()
    }

    private func menuButton(_ title: String, icon: String) -> some View {
        Label(title, systemImage: icon)
            .font(.title2.bold())
            .foregroundColor(.black)
            .frame(maxWidth: .infinity)
            .padding()
            .background(.white)
            .clipShape(RoundedRectangle(cornerRadius: 15))
    }

    private func saveResult(_ value: Int) {
        var items = history[selectedMode] ?? []
        items.append(value)
        history[selectedMode] = Array(items.suffix(8))
    }

    private var cameraView: some View {
        VStack(spacing: 20) {
            HStack {
                Button("ZPĚT") {
                    scanning = false
                    camera.stop()
                    screen = selectedMode == "DOWN TEST" ? .downChoice : .menu
                }
                .foregroundColor(.white)

                Spacer()

                Text(selectedMode)
                    .font(.headline.bold())
                    .foregroundColor(.white)
            }

            Spacer()

            if camera.permissionGranted {
                CameraPreview(session: camera.session)
                    .frame(height: 330)
                    .clipShape(RoundedRectangle(cornerRadius: 20))
            } else {
                VStack(spacing: 12) {
                    Image(systemName: "camera.fill")
                        .font(.system(size: 55))
                    Text("KAMERA")
                        .font(.headline)
                    Text("Pro skenování povol přístup ke kameře.")
                        .font(.footnote)
                        .multilineTextAlignment(.center)
                }
                .foregroundColor(.white)
                .frame(height: 330)
                .frame(maxWidth: .infinity)
                .background(panel)
                .clipShape(RoundedRectangle(cornerRadius: 20))
            }

            if scanning {
                Text("SCANNING…")
                    .font(.largeTitle.bold())
                    .foregroundColor(.white)

                ProgressView(value: Double(scanProgress), total: 100)
                    .tint(.white)

                Text("\(scanProgress) %")
                    .foregroundColor(.gray)
            } else {
                Button {
                    startScanning()
                } label: {
                    menuButton("SKENOVAT", icon: "viewfinder")
                }
                .disabled(!camera.permissionGranted)
                .opacity(camera.permissionGranted ? 1 : 0.4)
            }

            Spacer()
        }
        .padding()
        .onAppear {
            camera.requestPermissionAndStart()
        }
        .onDisappear {
            camera.stop()
        }
    }

    private func startScanning() {
        guard !scanning else { return }

        scanning = true
        scanProgress = 0

        Task {
            while scanProgress < 100 {
                try? await Task.sleep(for: .milliseconds(180))
                scanProgress = min(scanProgress + 10, 100)
            }

            try? await Task.sleep(for: .milliseconds(300))

            if selectedMode == "AURA TEST" {
                auraResult = Int.random(in: -1000...1000)
                result = auraResult
            } else {
                // Náhodný easter egg, bez analýzy vzhledu.
                result = Int.random(in: 1...100) == 1
                    ? 100
                    : Int.random(in: 0...100)
            }

            saveResult(result)
            scanning = false
            camera.stop()
            screen = .result
        }
    }

    private var resultView: some View {
        VStack(spacing: 22) {
            Spacer()

            if selectedMode == "DOWN TEST" && result > 100 {
                Text("⚠️ WARNING ⚠️")
                    .font(.largeTitle.bold())
                    .foregroundColor(.red)

                Text("SYSTEM ERROR")
                    .font(.title.bold())
                    .foregroundColor(.red)

                Text("ROZBIL JSI APLIKACI!")
                    .font(.title2.bold())
                    .foregroundColor(.white)

                Text("Jsi tak velkej DOWN, že jsi rozbil celou aplikaci. 💀")
                    .font(.headline)
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
            } else {
                Text(selectedMode)
                    .font(.largeTitle.bold())
                    .foregroundColor(.white)

                Text("TVŮJ VÝSLEDEK")
                    .font(.headline)
                    .foregroundColor(.gray)

                if selectedMode == "AURA TEST" {
                    Text("\(auraResult) / 1000")
                        .font(.system(size: 55, weight: .black))
                        .foregroundColor(.white)
                } else {
                    Text("\(result) %")
                        .font(.system(size: 75, weight: .black))
                        .foregroundColor(.white)
                }

                Text("Je to jen joke, neberte to vážně.")
                    .font(.footnote)
                    .foregroundColor(.gray)
            }

            Spacer()

            ShareLink(
                item: "\(selectedMode): \(selectedMode == "AURA TEST" ? "\(auraResult) / 1000" : "\(result) %")"
            ) {
                menuButton("SDÍLET VÝSLEDEK", icon: "square.and.arrow.up")
            }

            Button {
                screen = .menu
            } label: {
                menuButton("HLAVNÍ MENU", icon: "house.fill")
            }

            Button {
                screen = .history
            } label: {
                menuButton("HISTORIE", icon: "clock")
            }
        }
        .padding()
    }

    private var historyView: some View {
        VStack(spacing: 18) {
            HStack {
                Button("ZPĚT") {
                    screen = .menu
                }
                .foregroundColor(.white)

                Spacer()

                Text("HISTORIE")
                    .font(.title.bold())
                    .foregroundColor(.white)

                Spacer()
            }

            ScrollView {
                VStack(spacing: 20) {
                    ForEach(modes.filter { history[$0.name]?.isEmpty == false }) { mode in
                        VStack(alignment: .leading, spacing: 10) {
                            Text(mode.name)
                                .font(.headline)
                                .foregroundColor(.white)

                            ForEach(Array((history[mode.name] ?? []).reversed().enumerated()), id: \.offset) { _, value in
                                Text(mode.name == "AURA TEST"
                                     ? "\(value) / 1000"
                                     : "\(value) %")
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .padding()
                                    .background(panel)
                                    .clipShape(RoundedRectangle(cornerRadius: 10))
                            }
                        }
                    }

                    if history.values.allSatisfy({ $0.isEmpty }) {
                        Text("Zatím žádné výsledky")
                            .foregroundColor(.gray)
                    }
                }
            }
        }
        .padding()
    }
}

final class CameraModel: ObservableObject {
    let session = AVCaptureSession()

    @Published var permissionGranted = false

    private let queue = DispatchQueue(label: "down.test.camera")
    private var configured = false

    func requestPermissionAndStart() {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            DispatchQueue.main.async {
                self.permissionGranted = true
                self.start()
            }

        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { granted in
                DispatchQueue.main.async {
                    self.permissionGranted = granted
                    if granted {
                        self.start()
                    }
                }
            }

        default:
            DispatchQueue.main.async {
                self.permissionGranted = false
            }
        }
    }

    func start() {
        queue.async {
            if !self.configured {
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
                } catch {
                    print("Camera error: \(error)")
                    return
                }
            }

            if !self.session.isRunning {
                self.session.startRunning()
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
