import BackgroundTasks
import Shared
import SwiftUI
import UIKit

final class AppDelegate: NSObject, UIApplicationDelegate {
    private let refreshTaskIdentifier = "com.imnaiyar.skytimes.reminders.refresh"

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: refreshTaskIdentifier, using: nil) { task in
            self.handleAppRefresh(task: task as! BGAppRefreshTask)
        }
        scheduleAppRefresh()
        return true
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        scheduleAppRefresh()
    }

    private func handleAppRefresh(task: BGAppRefreshTask) {
        scheduleAppRefresh()

        task.expirationHandler = { }
        IosReminderBridge.shared.refresh()
        task.setTaskCompleted(success: true)
    }

    private func scheduleAppRefresh() {
        let request = BGAppRefreshTaskRequest(identifier: refreshTaskIdentifier)
        request.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60)
        try? BGTaskScheduler.shared.submit(request)
    }
}
