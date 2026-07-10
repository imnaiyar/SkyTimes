package com.imnaiyar.skytimes.onboarding

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Persistence boundary for an app to implement with DataStore, Settings, a server,
 * or any other storage. The boolean is the first-launch gate; step keys retain
 * progress while a multiscreen tutorial is still in progress.
 */
interface TutorialProgressRepository {
    suspend fun readCompletedStepKeys(): Set<String>
    suspend fun saveCompletedStepKeys(keys: Set<String>)

    /** True means onboarding has been completed or skipped and must not auto-run again. */
    suspend fun readTutorialCompleted(): Boolean = false

    suspend fun saveTutorialCompleted(completed: Boolean) = Unit
}


object NoOpTutorialProgressPersistence : TutorialProgressRepository {
    override suspend fun readCompletedStepKeys(): Set<String> = emptySet()
    override suspend fun saveCompletedStepKeys(keys: Set<String>) = Unit
}

data class TutorialState<S : TutorialStep>(
    /** The flow currently being shown, or null when no tutorial is active. */
    val currentFlowId: String? = null,
    val currentStep: S? = null,
    /** Zero-based position of [currentStep] in [currentFlowId]. */
    val currentStepIndex: Int? = null,
    val completedStepKeys: Set<String> = emptySet(),
    val isTutorialCompleted: Boolean = false,
    val isRunning: Boolean = false,
    val isLoaded: Boolean = false
) {
    val canGoPrevious: Boolean
        get() = isRunning && (currentStepIndex ?: 0) > 0
}

class TutorialManager<S : TutorialStep>(
    flows: List<TutorialFlow<S>>,
    private val scope: CoroutineScope,
    private val repository: TutorialProgressRepository = NoOpTutorialProgressPersistence
) {

    private val flows = flows.toList()
    private val flowsById = this.flows.associateBy(TutorialFlow<S>::id)
    private val definitionsByKey = this.flows
        .flatMap(TutorialFlow<S>::steps)
        .associateBy { it.step.persistenceKey }
    private val _state = MutableStateFlow(TutorialState<S>())
    val state: StateFlow<TutorialState<S>> = _state.asStateFlow()

    init {
        require(this.flows.map(TutorialFlow<S>::id).distinct().size == this.flows.size) {
            "Every tutorial flow must have a unique id."
        }
        val stepKeys = this.flows.flatMap(TutorialFlow<S>::steps).map { it.step.persistenceKey }
        require(stepKeys.distinct().size == stepKeys.size) {
            "Every tutorial step must have a unique persistenceKey across all flows."
        }
        scope.launch {
            val completed = repository.readCompletedStepKeys()
            val tutorialCompleted = repository.readTutorialCompleted()
            _state.update { previous ->
                val next = if (previous.isRunning && !tutorialCompleted) {
                    nextStep(flowId = previous.currentFlowId, completed = completed)
                } else {
                    null
                }
                previous.copy(
                    currentFlowId = next?.flowId,
                    currentStep = next?.definition?.step,
                    currentStepIndex = next?.index,
                    completedStepKeys = completed,
                    isTutorialCompleted = tutorialCompleted,
                    isRunning = next != null,
                    isLoaded = true
                )
            }
        }
    }

    /** Starts the first flow that still has an incomplete step. */
    fun start() = startInternal(flowId = null)

    /** Starts (or resumes) a specific flow at its first incomplete step. */
    fun start(flowId: String) = startInternal(flowId)

    private fun startInternal(flowId: String?) {
        if (flowId != null) {
            require(flowId in flowsById) { "Unknown tutorial flow: $flowId" }
        }
        _state.update { previous ->
            if (!previous.isLoaded) {
                previous.copy(currentFlowId = flowId, isRunning = true)
            } else if (previous.isTutorialCompleted) {
                previous
            } else {
                val next = nextStep(flowId, previous.completedStepKeys)
                previous.copy(
                    currentFlowId = next?.flowId,
                    currentStep = next?.definition?.step,
                    currentStepIndex = next?.index,
                    isRunning = next != null
                )
            }
        }
    }

    /** Marks the displayed step complete and advances within the current flow. */
    fun next() {
        val current = _state.value.currentStep ?: return
        val flowId = _state.value.currentFlowId ?: return
        val currentIndex = _state.value.currentStepIndex ?: return
        val completed = _state.value.completedStepKeys + current.persistenceKey
        val next = nextStep(flowId, completed, startIndex = currentIndex + 1)
        val tutorialCompleted = completed.containsAll(definitionsByKey.keys)
        _state.update {
            it.copy(
                currentFlowId = next?.flowId,
                currentStep = next?.definition?.step,
                currentStepIndex = next?.index,
                completedStepKeys = completed,
                isTutorialCompleted = tutorialCompleted,
                isRunning = next != null
            )
        }
        persist(completed, tutorialCompleted)
    }


    /** Moves to the preceding step in the current flow without changing completion state. */
    fun previous() {
        val current = _state.value.currentStep ?: return
        val flowId = _state.value.currentFlowId ?: return
        val currentIndex = _state.value.currentStepIndex ?: return
        val flow = flowsById[flowId] ?: return
        val previousDefinition = flow.steps.getOrNull(currentIndex - 1) ?: return

        val completed = _state.value.completedStepKeys - current.persistenceKey
        val tutorialCompleted = _state.value.isTutorialCompleted
        _state.update {
            it.copy(
                currentStep = previousDefinition.step,
                currentStepIndex = currentIndex - 1,
                completedStepKeys = completed
            )
        }
        persist(completed, tutorialCompleted)
    }

    /**
     * Ends all tutorial flows and persists the first-launch completion marker.
     */
    fun skip() {
        finish()
    }

    /** Marks every configured step complete and prevents future automatic starts. */
    fun finish() {
        val completed = _state.value.completedStepKeys + definitionsByKey.keys
        _state.update {
            it.copy(
                currentFlowId = null,
                currentStep = null,
                currentStepIndex = null,
                completedStepKeys = completed,
                isTutorialCompleted = true,
                isRunning = false
            )
        }
        persist(completed, tutorialCompleted = true)
    }

    /** Clears all persisted tutorial progress. Intended for debug/settings reset actions. */
    fun reset() {
        _state.update {
            it.copy(
                currentFlowId = null,
                currentStep = null,
                currentStepIndex = null,
                completedStepKeys = emptySet(),
                isTutorialCompleted = false,
                isRunning = false
            )
        }
        persist(emptySet(), tutorialCompleted = false)
    }

    fun definitionFor(step: S): TutorialDefinition<S>? =
        definitionsByKey[step.persistenceKey]

    private fun nextStep(
        flowId: String?,
        completed: Set<String>,
        startIndex: Int = 0
    ): FlowStep<S>? {
        val candidates = if (flowId == null) flows else listOfNotNull(flowsById[flowId])
        for (flow in candidates) {
            val index =
                flow.steps.indexOfFirstFrom(startIndex) { it.step.persistenceKey !in completed }
            if (index >= 0) return FlowStep(flow.id, index, flow.steps[index])
        }
        return null
    }

    private data class FlowStep<S : TutorialStep>(
        val flowId: String,
        val index: Int,
        val definition: TutorialDefinition<S>
    )

    private fun persist(completed: Set<String>, tutorialCompleted: Boolean) {
        scope.launch {
            repository.saveCompletedStepKeys(completed)
            repository.saveTutorialCompleted(tutorialCompleted)
        }
    }
}


private inline fun <T> List<T>.indexOfFirstFrom(startIndex: Int, predicate: (T) -> Boolean): Int {
    for (index in startIndex.coerceAtLeast(0) until size) {
        if (predicate(this[index])) return index
    }
    return -1
}
