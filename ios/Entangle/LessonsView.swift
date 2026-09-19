import SwiftUI

struct LessonsView: View {
    let content: Content

    var body: some View {
        NavigationStack {
            List {
                Section {
                    Text(content.app.tagline)
                        .foregroundStyle(.secondary)
                }
                Section("Lessons") {
                    ForEach(Array(content.lessons.enumerated()), id: \.element.id) { index, lesson in
                        NavigationLink(value: lesson) {
                            HStack(spacing: 14) {
                                Text("\(index + 1)")
                                    .font(.headline)
                                    .frame(width: 32, height: 32)
                                    .background(Color.accentColor, in: Circle())
                                    .foregroundStyle(.white)
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(lesson.title).font(.headline)
                                    Text(lesson.summary).font(.subheadline).foregroundStyle(.secondary)
                                }
                            }
                        }
                    }
                }
                Section("Practice") {
                    NavigationLink("Take the quiz") { QuizView(questions: content.quiz) }
                    NavigationLink(content.coinFlip.title) { CoinFlipView(copy: content.coinFlip) }
                }
            }
            .navigationTitle(content.app.name)
            .navigationDestination(for: Lesson.self) { lesson in
                LessonDetailView(lesson: lesson, content: content)
            }
        }
    }
}

struct LessonDetailView: View {
    let lesson: Lesson
    let content: Content

    private var next: Lesson? {
        guard let i = content.lessons.firstIndex(of: lesson) else { return nil }
        return content.lessons.indices.contains(i + 1) ? content.lessons[i + 1] : nil
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text(lesson.summary).foregroundStyle(.secondary)
                ForEach(lesson.paragraphs, id: \.self) { paragraph in
                    Text(paragraph).lineSpacing(4)
                }
                if let next {
                    NavigationLink(value: next) {
                        Label("Next: \(next.title)", systemImage: "arrow.right")
                    }
                    .buttonStyle(.borderedProminent)
                } else {
                    NavigationLink {
                        QuizView(questions: content.quiz)
                    } label: {
                        Label("Take the quiz", systemImage: "checkmark.circle")
                    }
                    .buttonStyle(.borderedProminent)
                }
            }
            .padding()
        }
        .navigationTitle(lesson.title)
        .navigationBarTitleDisplayMode(.inline)
    }
}
