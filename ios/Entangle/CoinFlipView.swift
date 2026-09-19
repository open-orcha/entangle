import SwiftUI

// Toy model. See content/lessons.json -> coinFlip.disclaimer.
enum FlipMode: String, CaseIterable, Identifiable {
    case entangled, classical
    var id: String { rawValue }
}

enum Face: String {
    case heads = "Heads"
    case tails = "Tails"

    static func random(using generator: inout some RandomNumberGenerator) -> Face {
        Bool.random(using: &generator) ? .heads : .tails
    }
}

struct FlipResult: Equatable {
    let a: Face
    let b: Face
    var agreed: Bool { a == b }
}

struct Tally {
    var flips = 0
    var agreements = 0
    var rate: Double { flips == 0 ? 0 : Double(agreements) / Double(flips) }

    mutating func record(_ result: FlipResult) {
        flips += 1
        if result.agreed { agreements += 1 }
    }
}

enum CoinFlipModel {
    /// One measurement of the pair. The generator is injectable for tests.
    static func flipPair(_ mode: FlipMode, using generator: inout some RandomNumberGenerator) -> FlipResult {
        switch mode {
        case .entangled:
            // One shared random outcome: the two coins always agree.
            let shared = Face.random(using: &generator)
            return FlipResult(a: shared, b: shared)
        case .classical:
            // Two independent flips: they agree about half the time.
            return FlipResult(a: Face.random(using: &generator), b: Face.random(using: &generator))
        }
    }

    static func flipPair(_ mode: FlipMode) -> FlipResult {
        var generator = SystemRandomNumberGenerator()
        return flipPair(mode, using: &generator)
    }
}

struct CoinFlipView: View {
    let copy: CoinFlipCopy

    @State private var mode: FlipMode = .entangled
    @State private var last: FlipResult? = nil
    @State private var tally = Tally()

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                Text(copy.intro).foregroundStyle(.secondary)

                Picker("Mode", selection: $mode) {
                    Text(copy.entangledLabel).tag(FlipMode.entangled)
                    Text(copy.classicalLabel).tag(FlipMode.classical)
                }
                .pickerStyle(.segmented)
                .onChange(of: mode) { reset() }

                HStack(spacing: 24) {
                    coin(last?.a)
                    coin(last?.b)
                }

                HStack {
                    Button(copy.measureLabel) {
                        let result = CoinFlipModel.flipPair(mode)
                        last = result
                        tally.record(result)
                    }
                    .buttonStyle(.borderedProminent)
                    Button(copy.resetLabel) { reset() }
                        .buttonStyle(.bordered)
                }

                Text("\(tally.flips) measurements · \(tally.agreements) agreed · \(Int((tally.rate * 100).rounded()))% agreement")
                    .foregroundStyle(.secondary)

                Text(copy.disclaimer)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
            .padding()
        }
        .navigationTitle(copy.title)
        .navigationBarTitleDisplayMode(.inline)
    }

    private func reset() {
        last = nil
        tally = Tally()
    }

    private func coin(_ face: Face?) -> some View {
        Text(face?.rawValue ?? "?")
            .font(.title3.weight(.semibold))
            .frame(width: 120, height: 120)
            .background(.thinMaterial, in: Circle())
            .overlay(Circle().stroke(face == .heads ? Color.accentColor : face == .tails ? Color.teal : Color.gray, lineWidth: 3))
    }
}
