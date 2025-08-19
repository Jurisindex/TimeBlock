import SwiftUI

@main
struct TimeBlockApp: App {
    @State private var tasks: [Task] = []

    var body: some Scene {
        WindowGroup {
            NavigationView {
                List {
                    ForEach(tasks) { task in
                        HStack {
                            Text(task.title)
                            Spacer()
                            Text(task.formattedDuration)
                        }
                    }
                    .onDelete(perform: deleteTask)
                }
                .navigationTitle("TimeBlock")
                .toolbar {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button(action: addTask) {
                            Image(systemName: "plus")
                        }
                    }
                }
            }
        }
    }

    private func addTask() {
        let newTask = Task(title: "New Task", duration: 30 * 60)
        tasks.append(newTask)
    }

    private func deleteTask(at offsets: IndexSet) {
        tasks.remove(atOffsets: offsets)
    }
}

struct Task: Identifiable {
    let id = UUID()
    var title: String
    var duration: TimeInterval

    var formattedDuration: String {
        let formatter = DateComponentsFormatter()
        formatter.allowedUnits = [.hour, .minute]
        formatter.unitsStyle = .short
        return formatter.string(from: duration) ?? "0m"
    }
}
