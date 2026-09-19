import SwiftUI

@main
struct EntangleApp: App {
    var body: some Scene {
        WindowGroup {
            LessonsView(content: ContentStore.load())
        }
    }
}
