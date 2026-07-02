package com.imnaiyar.skytimes.startup

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class AppInitializer(
    private val tasks: List<StartupTask>
) {
    suspend fun initialize(): AppInitializationResult = coroutineScope {
        val results = tasks
            .map { task ->
                async {
                    runTask(task)
                }
            }
            .awaitAll()

        val criticalFailure = results
            .filterIsInstance<StartupTaskResult.Failure>()
            .firstOrNull { it.critical }

        if (criticalFailure != null) {
            throw AppInitializationException(
                taskName = criticalFailure.taskName,
                cause = criticalFailure.cause
            )
        }

        AppInitializationResult(
            warnings = results
                .filterIsInstance<StartupTaskResult.Failure>()
                .map { failure ->
                    AppStartupWarning(
                        taskName = failure.taskName,
                        cause = failure.cause
                    )
                }
        )
    }

    private suspend fun runTask(task: StartupTask): StartupTaskResult {
        return try {
            task.run()
            StartupTaskResult.Success(task.name)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (cause: Throwable) {
            StartupTaskResult.Failure(
                taskName = task.name,
                critical = task.critical,
                cause = cause
            )
        }
    }
}

data class AppInitializationResult(
    val warnings: List<AppStartupWarning> = emptyList()
)

class AppInitializationException(
    val taskName: String,
    cause: Throwable
) : RuntimeException("Startup task failed: $taskName", cause)

private sealed interface StartupTaskResult {
    data class Success(
        val taskName: String
    ) : StartupTaskResult

    data class Failure(
        val taskName: String,
        val critical: Boolean,
        val cause: Throwable
    ) : StartupTaskResult
}
