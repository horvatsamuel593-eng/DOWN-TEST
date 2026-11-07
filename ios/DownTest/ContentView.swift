import SwiftUI

struct ContentView: View {
    @State private var auraMode = false
    @State private var result = 0
    @State private var history: [Int] = []

    var body: some View {
        VStack(spacing: 20) {
            Text("DOWN TEST")
                .font(.largeTitle.bold())

            Picker("Režim", selection: $auraMode) {
                Text("DOWN TEST").tag(false)
                Text("AURA TEST").tag(true)
            }
            .pickerStyle(.segmented)

            Text("\(result)")
                .font(.system(size: 65, weight: .bold))

            Button("SKENOVAT") {
                result = Int.random(in: 0...(auraMode ? 1000 : 100))
                history.append(result)
            }
            .font(.title2.bold())

            Text("HISTORIE")
                .font(.headline)

            List(history.reversed(), id: \.self) { item in
                Text("\(item)")
            }
        }
        .padding()
    }
}