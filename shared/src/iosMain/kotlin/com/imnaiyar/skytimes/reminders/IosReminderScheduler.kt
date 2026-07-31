package com.imnaiyar.skytimes.reminders

import androidx.compose.runtime.Composable
import com.imnaiyar.skytimes.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNNotificationTrigger
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.time.Clock
import kotlin.time.Instant

// maximum number of notification requests allowed on iOS
private const val MAX_PENDING_REQUESTS = 64;

class IosReminderScheduler(
    private val settingsRepository: SettingsRepository,
    private val reminderRepository: ReminderRepository,
    private val scope: CoroutineScope,
    private val notificationCenter: UNUserNotificationCenter = UNUserNotificationCenter.currentNotificationCenter(),
    private val config: ReminderConfig = ReminderConfig(),
) : ReminderScheduler {

    private val settings = settingsRepository.settings

    override suspend fun refresh() {
        ensureStateLoaded()

        if (!settings.value.notificationsEnabled) return
        refreshWindow()
    }

    override suspend fun scheduleReminder(reminder: Reminder) {
        ensureStateLoaded()

        if (!reminder.enabled || !settings.value.notificationsEnabled) {
            cancelReminder(reminder.eventId.name)
            return
        }

        val desired = reminderTimes(
            reminder,
            Clock.System.now(),
            config.reminderWindowSize
        ).map { reminder to it }

        schedule(desired, true)
    }

    override suspend fun cancelReminder(eventId: String) {
        ensureStateLoaded()
        val identifiers = reminderRepository.reminders.value
            .filter { it.eventId.name == eventId }
            .flatMap { reminder ->
                pendingRequests()
                    .filter { it.identifier.startsWith(reminder.identifierPrefix()) }
                    .map { it.identifier }
            }

        if (identifiers.isNotEmpty()) {
            notificationCenter.removePendingNotificationRequestsWithIdentifiers(identifiers)
        }
    }

    override suspend fun cancelAll() {
        ensureStateLoaded()
        val identifiers = pendingRequests().map { it.identifier }
        if (identifiers.isNotEmpty()) {
            notificationCenter.removePendingNotificationRequestsWithIdentifiers(identifiers)
        }
    }

    override suspend fun hasPermission(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
                val granted = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
                        || settings?.authorizationStatus == UNAuthorizationStatusProvisional
                        || settings?.authorizationStatus == UNAuthorizationStatusEphemeral
                continuation.resume(granted)
            }
        }
    }

    // only useful for android
    override fun hasExactAlarm(): Boolean = true
    override fun requestExactAlarm() = Unit

    // TODO: check if permission is denied, in that case no dialogue will be presented
    // Redirect to app settings for enabling via confirm dialogue
    override suspend fun requestPermission(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val options = UNAuthorizationOptionAlert or
                    UNAuthorizationOptionSound or
                    UNAuthorizationOptionBadge
            notificationCenter.requestAuthorizationWithOptions(options) { granted, _ ->
                continuation.resume(granted)
            }
        }
    }

    fun refreshNow() {
        scope.launch {
            refresh()
        }
    }

    fun requestPermissionNow() {
        scope.launch {
            requestPermission()
        }
    }

    private suspend fun ensureStateLoaded() {
        settingsRepository.initialize()
        reminderRepository.initialize()
    }

    private suspend fun refreshWindow() {
        val now = Clock.System.now()

        val desired = reminderRepository.reminders.value
            .filter(Reminder::enabled)
            .flatMap { reminder ->
                reminderTimes(reminder, now, config.reminderWindowSize)
                    .map { reminder to it }
            }
            .take(MAX_PENDING_REQUESTS)

        schedule(desired)
    }

    private suspend fun schedule(
        desiredRequests: List<Pair<Reminder, Instant>>,
        // if true, pending requests will not be removed
        ignoreRest: Boolean = false
    ) {
        val currentRequests = pendingRequests()
        val currentIdentifiers = currentRequests.map { it.identifier }.toSet()

        val desiredIdentifiers = desiredRequests
            .map { (reminder, time) -> reminder.notificationIdentifier(time) }
            .toSet()

        // this is when called during scheduleReminder
        // remove scheduled reminders of current given event (which is first in list, the only one)
        currentRequests
            .filterNot { it.identifier.startsWith(desiredRequests.first().first.identifierPrefix()) }
            .takeIf { it.isNotEmpty() && ignoreRest }
            ?.let {
                notificationCenter.removePendingNotificationRequestsWithIdentifiers(
                    it.map { request -> request.identifier }
                )
            }


        // this is for when called during refresh
        // remove rest pending request that are not included in refresh
        currentRequests
            .filter { it.identifier !in desiredIdentifiers }
            .takeIf { it.isNotEmpty() && !ignoreRest }
            ?.let {
                notificationCenter.removePendingNotificationRequestsWithIdentifiers(
                    it.map { request -> request.identifier }
                )
            }

        val remainingSlots =
            (MAX_PENDING_REQUESTS - pendingRequests().size).coerceAtLeast(0)

        desiredRequests
            .filterNot { (reminder, time) ->
                reminder.notificationIdentifier(time) in currentIdentifiers
            }
            .take(remainingSlots)
            .forEach { (reminder, time) ->
                scheduleNotification(reminder, time)
            }
    }

    private suspend fun scheduleNotification(reminder: Reminder, time: kotlin.time.Instant) {
        val now = Clock.System.now()
        val intervalSeconds = ((time - now).inWholeMilliseconds / 1000.0).coerceAtLeast(1.0)
        val content = UNMutableNotificationContent().apply {
            setTitle(Reminder.title(reminder.eventId))
            setBody(Reminder.body(reminder.eventId, reminder.offsetMinutes))
            setSound(UNNotificationSound.defaultSound())
        }
        val trigger: UNNotificationTrigger =
            UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                intervalSeconds,
                false
            )
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = reminder.notificationIdentifier(time),
            content = content,
            trigger = trigger
        )
        suspendCancellableCoroutine { continuation ->
            notificationCenter.addNotificationRequest(request) { error: NSError? ->
                if (error != null) {
                    continuation.resume(Unit)
                } else {
                    continuation.resume(Unit)
                }
            }
        }
    }

    private suspend fun pendingRequests(): List<UNNotificationRequest> {
        return suspendCancellableCoroutine { continuation ->
            notificationCenter.getPendingNotificationRequestsWithCompletionHandler { requests ->
                continuation.resume((requests as List<UNNotificationRequest>?) ?: emptyList())
            }
        }
    }


    private fun Reminder.identifierPrefix(): String = "$id:"

    private fun Reminder.notificationIdentifier(time: Instant): String =
        "$id:${time.toEpochMilliseconds()}"
}


actual fun getReminderSchedular(
    settingsRepository: SettingsRepository,
    reminderRepository: ReminderRepository,
    scope: CoroutineScope
): ReminderScheduler {
    val scheduler = IosReminderScheduler(settingsRepository, reminderRepository, scope)
    IosReminderBridge.install(scheduler)
    return scheduler
}

@Composable
actual fun rememberNotificationPermissionRequester(): ((Boolean) -> Unit) -> Unit = {}

/**
 * This is delegated to the iOS BGRefresh task when that happens
 */
object IosReminderBridge {
    private val bridgeScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** This is initialized in [getReminderSchedular] which is initialized once in [AppContainer] */
    private var scheduler: IosReminderScheduler? = null

    fun install(scheduler: IosReminderScheduler) {
        this.scheduler = scheduler
    }

    fun refresh() {
        scheduler?.refreshNow()
    }

    fun requestPermission() {
        scheduler?.requestPermissionNow()
    }
}
