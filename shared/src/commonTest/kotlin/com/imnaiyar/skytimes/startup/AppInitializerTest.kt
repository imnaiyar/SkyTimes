package com.imnaiyar.skytimes.startup

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppInitializerTest {
    @Test
    fun allSuccessfulTasksCompleteWithoutWarnings() = runTest {
        val initializer = AppInitializer(
            tasks = listOf(
                FakeStartupTask(name = "Settings"),
                FakeStartupTask(name = "Database")
            )
        )

        val result = initializer.initialize()

        assertTrue(result.warnings.isEmpty())
    }

    @Test
    fun optionalTaskFailureReturnsWarningAndDoesNotBlockStartup() = runTest {
        val failure = IllegalStateException("Analytics unavailable")
        val initializer = AppInitializer(
            tasks = listOf(
                FakeStartupTask(name = "Settings"),
                FakeStartupTask(
                    name = "Analytics",
                    critical = false,
                    failure = failure
                )
            )
        )

        val result = initializer.initialize()

        assertEquals(1, result.warnings.size)
        assertEquals("Analytics", result.warnings.single().taskName)
        assertEquals(failure, result.warnings.single().cause)
    }

    @Test
    fun criticalTaskFailureBlocksStartup() = runTest {
        val failure = IllegalStateException("Database unavailable")
        val initializer = AppInitializer(
            tasks = listOf(
                FakeStartupTask(
                    name = "Database",
                    critical = true,
                    failure = failure
                )
            )
        )

        val exception = assertFailsWith<AppInitializationException> {
            initializer.initialize()
        }

        assertEquals("Database", exception.taskName)
        assertEquals(failure, exception.cause)
    }

    @Test
    fun tasksRunConcurrently() = runTest {
        val initializer = AppInitializer(
            tasks = listOf(
                FakeStartupTask(name = "Settings", delayMillis = 100),
                FakeStartupTask(name = "Database", delayMillis = 100),
                FakeStartupTask(name = "Fonts", delayMillis = 100)
            )
        )

        initializer.initialize()

        assertEquals(100L, currentTime)
    }
}

private class FakeStartupTask(
    override val name: String,
    override val critical: Boolean = true,
    private val delayMillis: Long = 0,
    private val failure: Throwable? = null
) : StartupTask {
    override suspend fun run() {
        delay(delayMillis)
        failure?.let { throw it }
    }
}
