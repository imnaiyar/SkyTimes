package com.imnaiyar.skytimes.reminder

import com.imnaiyar.skytimes.startup.StartupTask
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persists [Reminder] instances using the project's existing
 * `multiplatform-settings` key-value store.
 *
 * All reminders are serialized as a JSON array under a single storage key
 * so that atomic reads and writes are cheap.
 *
 * Implements [StartupTask] so that persisted reminders are loaded during
 * the app initialisation phase.
 */
class ReminderRepository(
    private val storage: Settings = Settings(),
    private val json: Json = Json { ignoreUnknownKeys = true },
) : StartupTask {

    override val name = "ReminderRepository"
    override val critical = false

    private val mutex = Mutex()

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    // ── Initialisation ──────────────────────────────────────

    override suspend fun initialize() {
        mutex.withLock {
            _reminders.value = loadAll()
        }
    }

    // ── CRUD ────────────────────────────────────────────────

    suspend fun getAll(): List<Reminder> {
        mutex.withLock {
            return _reminders.value.toList()
        }
    }

    suspend fun getByEventKey(eventKey: String): Reminder? {
        mutex.withLock {
            return _reminders.value.firstOrNull { it.eventKey.name == eventKey }
        }
    }

    suspend fun getById(id: String): Reminder? {
        mutex.withLock {
            return _reminders.value.firstOrNull { it.id == id }
        }
    }

    suspend fun upsert(reminder: Reminder) {
        mutex.withLock {
            val current = _reminders.value.toMutableList()
            val index = current.indexOfFirst { it.id == reminder.id }
            if (index >= 0) {
                current[index] = reminder
            } else {
                current.add(reminder)
            }
            persist(current)
            _reminders.value = current
        }
    }

    suspend fun delete(reminderId: String) {
        mutex.withLock {
            val current = _reminders.value.toMutableList()
            current.removeAll { it.id == reminderId }
            persist(current)
            _reminders.value = current
        }
    }

    suspend fun deleteByEventKey(eventKey: String) {
        mutex.withLock {
            val current = _reminders.value.toMutableList()
            current.removeAll { it.eventKey.name == eventKey }
            persist(current)
            _reminders.value = current
        }
    }

    suspend fun deleteAll() {
        mutex.withLock {
            persist(emptyList())
            _reminders.value = emptyList()
        }
    }

    // ── Internal persistence ───────────────────────────────

    private fun loadAll(): List<Reminder> {
        val raw = storage.getStringOrNull(STORAGE_KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<Reminder>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun persist(list: List<Reminder>) {
        if (list.isEmpty()) {
            storage.remove(STORAGE_KEY)
        } else {
            storage.putString(STORAGE_KEY, json.encodeToString(list))
        }
    }

    private companion object {
        const val STORAGE_KEY = "reminder_data_v1"
    }
}
