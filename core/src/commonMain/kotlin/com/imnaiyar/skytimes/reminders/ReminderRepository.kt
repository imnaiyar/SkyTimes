package com.imnaiyar.skytimes.reminders

import com.imnaiyar.skytimes.constants.EventKey
import com.imnaiyar.skytimes.startup.StartupTask
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

// TODO: allow muting a particular event through notification actions, data should be saved, just disable it
class ReminderRepository(
    private val storage: Settings = Settings(),
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
) : StartupTask {
    override val name: String = "Reminders"
    override val critical: Boolean = false

    private val updateMutex = Mutex()
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())

    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    override suspend fun initialize() {
        updateMutex.withLock {
            _reminders.value = loadReminders()
        }
    }

    suspend fun upsert(reminder: Reminder) {
        update { current -> current.filterNot { it.id == reminder.id } + reminder }
    }

    suspend fun remove(reminderId: String) {
        update { current -> current.filterNot { it.id == reminderId } }
    }

    suspend fun removeByEvent(eventId: EventKey) {
        update { current -> current.filterNot { it.eventId == eventId } }
    }

    suspend fun clear() {
        update { emptyList() }
    }

    fun remindersFor(eventId: EventKey): List<Reminder> =
        reminders.value.filter { it.eventId == eventId }

    private suspend inline fun update(transform: (List<Reminder>) -> List<Reminder>) {
        updateMutex.withLock {
            val current = _reminders.value
            val next = transform(current).distinctBy(Reminder::id)
            if (next == current) return
            save(next)
            _reminders.value = next
        }
    }

    private fun loadReminders(): List<Reminder> {
        val encoded = storage.getStringOrNull(ReminderKeys.Reminders)
            ?: return emptyList()
        return runCatching { json.decodeFromString<List<Reminder>>(encoded) }
            .getOrDefault(emptyList())
    }

    private fun save(reminders: List<Reminder>) {
        if (reminders.isEmpty()) {
            storage.remove(ReminderKeys.Reminders)
        } else {
            storage.putString(ReminderKeys.Reminders, json.encodeToString(reminders))
        }
    }
}

private object ReminderKeys {
    const val Reminders = "reminders"
}
