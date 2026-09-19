import SwiftUI

struct QuizView: View {
    let questions: [QuizQuestion]

    @State private var index = 0
    @State private var selected: Int? = nil
    @State private var score = 0

    var body: some View {
        Group {
            if index >= questions.count {
                finished
            } else {
                question(questions[index])
            }
        }
        .navigationTitle(index < questions.count ? "Question \(index + 1) of \(questions.count)" : "Quiz complete")
        .navigationBarTitleDisplayMode(.inline)
    }

    private var finished: some View {
        VStack(spacing: 20) {
            Text("You scored \(score) out of \(questions.count).")
                .font(.title2)
            Button("Try again") {
                index = 0
                selected = nil
                score = 0
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
    }

    private func question(_ q: QuizQuestion) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text(q.question).font(.title3)
                ForEach(Array(q.choices.enumerated()), id: \.offset) { i, choice in
                    Button {
                        guard selected == nil else { return }
                        selected = i
                        if i == q.answerIndex { score += 1 }
                    } label: {
                        Text(choice)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding()
                            .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 10))
                            .overlay(
                                RoundedRectangle(cornerRadius: 10)
                                    .stroke(borderColor(for: i, in: q), lineWidth: 2)
                            )
                    }
                    .buttonStyle(.plain)
                    .disabled(selected != nil)
                }
                if let selected {
                    Text((selected == q.answerIndex ? "Correct. " : "Not quite. ") + q.explanation)
                        .lineSpacing(4)
                    Button(index + 1 < questions.count ? "Next question" : "See result") {
                        index += 1
                        self.selected = nil
                    }
                    .buttonStyle(.borderedProminent)
                }
            }
            .padding()
        }
    }

    private func borderColor(for i: Int, in q: QuizQuestion) -> Color {
        guard let selected else { return .clear }
        if i == q.answerIndex { return .green }
        if i == selected { return .red }
        return .clear
    }
}
