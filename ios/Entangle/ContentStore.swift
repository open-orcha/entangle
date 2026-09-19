import Foundation

// Mirrors content/lessons.json, the contract shared with the Android and web apps.

struct AppInfo: Decodable {
    let name: String
    let tagline: String
    let contentVersion: Int
}

struct Lesson: Decodable, Identifiable, Hashable {
    let id: String
    let title: String
    let summary: String
    let body: String

    var paragraphs: [String] { body.components(separatedBy: "\n\n") }
}

struct QuizQuestion: Decodable, Identifiable {
    let id: String
    let lessonId: String
    let question: String
    let choices: [String]
    let answerIndex: Int
    let explanation: String
}

struct CoinFlipCopy: Decodable {
    let title: String
    let intro: String
    let entangledLabel: String
    let classicalLabel: String
    let measureLabel: String
    let resetLabel: String
    let disclaimer: String
}

struct Content: Decodable {
    let app: AppInfo
    let lessons: [Lesson]
    let quiz: [QuizQuestion]
    let coinFlip: CoinFlipCopy
}

enum ContentStore {
    /// lessons.json is bundled as a resource by ios/project.yml.
    static func load(bundle: Bundle = .main) -> Content {
        guard let url = bundle.url(forResource: "lessons", withExtension: "json"),
              let data = try? Data(contentsOf: url),
              let content = try? JSONDecoder().decode(Content.self, from: data)
        else {
            fatalError("content/lessons.json is missing or malformed")
        }
        return content
    }
}
