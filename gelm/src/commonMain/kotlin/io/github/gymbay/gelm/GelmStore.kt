package io.github.gymbay.gelm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.gymbay.gelm.observer.GelmObserver
import io.github.gymbay.gelm.observer.GelmSubject
import io.github.gymbay.gelm.reducers.GelmExternalReducer
import io.github.gymbay.gelm.reducers.GelmInternalReducer
import io.github.gymbay.gelm.reducers.ReducerResult
import io.github.gymbay.gelm.utils.EventType
import io.github.gymbay.gelm.utils.GelmLogger
import io.github.gymbay.gelm.utils.GelmSavedStateHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Central entity for GELM architecture.
 * Holder for architecture components.
 * Responsible for coordinate flow of external and internal events.
 *
 * `GelmStore` is an `open` class, so an application can declare a concrete named subclass for
 * a screen or a feature. A subclass adds only a distinct runtime type: event handling, state and
 * effect publication, command execution, and reducer orchestration remain final and cannot be
 * overridden.
 *
 * Both the five-type-parameter class and the four-type-parameter `GelmStore` alias from
 * `io.github.gymbay.gelm.utils` can be used as a supertype. The alias expands to
 * `GelmStore<State, Effect, Event, Nothing, Command>`, so it is suitable for stores without
 * internal events; use the five-type-parameter class when an actor and internal reducer are
 * needed.
 *
 * The alias itself does not create a distinct runtime type; the concrete subclass does. A generic
 * application base class also maps to a single runtime class, so declare one non-generic concrete
 * class per store when distinct ViewModel or dependency-injection keys are required.
 *
 * Example with the four-type-parameter alias:
 * ```
 * import io.github.gymbay.gelm.utils.GelmStore
 *
 * class LoginStore : GelmStore<LoginState, LoginEffect, LoginEvent, LoginCommand>(
 *     initialState = LoginState(),
 *     externalReducer = LoginExternalReducer(),
 * )
 * ```
 *
 * Example with the five-type-parameter class:
 * ```
 * import io.github.gymbay.gelm.GelmStore
 *
 * class ProfileStore : GelmStore<
 *     ProfileState, ProfileEffect, ProfileEvent, ProfileInternalEvent, ProfileCommand
 * >(
 *     initialState = ProfileState(),
 *     externalReducer = ProfileExternalReducer(),
 *     actor = ProfileActor(),
 *     internalReducer = ProfileInternalReducer(),
 * )
 * ```
 *
 * @param initialState Started point of screen state. Generally it looks like State().
 * @param externalReducer Reducer for processing external [Event] sent by [GelmStore.sendEvent].
 * @param actor Entity for processing asynchronous [Command] from [externalReducer] or [internalReducer].
 * @param internalReducer Reducer for processing [InternalEvent] from [actor].
 * And sending specific events to another store.
 * @param commandsDispatcher Dispatcher for handling [Command]s in specific thread on [viewModelScope].
 * Useful for tests. Default is [Dispatchers.Default].
 * @param effectsReplayCache How many effects will be repeated to first subscriber.
 * Useful when effects happens in background, before first subscribe or for tests. Default is 1.
 *
 * @property state Reactive stateful flow of state. Subscribe on it in UI.
 * @property effect Reactive one-shot events, stateless.
 * If no subscribers when event happens, it will be stored and replay to new subscriber.
 */
open class GelmStore<State, Effect, Event, InternalEvent, Command>(
    initialState: State,
    private val externalReducer: GelmExternalReducer<Event, State, Effect, Command>,
    private val actor: GelmActor<Command, InternalEvent>? = null,
    private val internalReducer: GelmInternalReducer<InternalEvent, State, Effect, Command>? = null,
    private val commandsDispatcher: CoroutineDispatcher = Dispatchers.Default,
    effectsReplayCache: Int = 1,
    private val logger: GelmLogger? = null,
    private val savedStateHandler: GelmSavedStateHandler<State>? = null
) : ViewModel(), GelmObserver<Event>, GelmSubject {

    private val stateMutex = Mutex()

    private val startedState: State = run {
        val restoredState = savedStateHandler?.restoreState(initialState)
        when {
            restoredState != null -> {
                logger?.log(EventType.InitialInvoked, "State restored: $restoredState")
                restoredState
            }

            else -> initialState
        }
    }

    val state: StateFlow<State>
        get() = _state
    private val _state: MutableStateFlow<State> = MutableStateFlow(startedState)

    @OptIn(ExperimentalCoroutinesApi::class)
    val effect: Flow<Effect>
        get() = _effect
            .onEach {
                _effect.resetReplayCache()
            }

    private val _effect: MutableSharedFlow<Effect> = MutableSharedFlow(replay = effectsReplayCache)

    private val activeCommandsMutex = Mutex()
    private val activeCommandsPull: MutableMap<Command, Job> = mutableMapOf()
    override val observers: MutableList<GelmObserver<*>> by lazy { mutableListOf() }

    init {
        logger?.log(EventType.InitialInvoked, "Initial state: ${startedState.toString()}")
        viewModelScope.launch {
            stateMutex.withLock {
                val result = externalReducer.startProcessing(startedState)
                handleReducerResult(result)
            }
        }
    }

    /**
     * Sent events to handle in [GelmExternalReducer].
     *
     * @param event External event for handling.
     */
    override fun sendEvent(event: Event) {
        logger?.log(EventType.SendEventInvoked, "Event sent: ${event.toString()}")
        viewModelScope.launch {
            stateMutex.withLock {
                val result = externalReducer.startProcessing(_state.value, event)
                handleReducerResult(result)
            }
        }
    }

    private suspend fun handleReducerResult(result: ReducerResult<State, Effect, Command>) {
        logger?.log(EventType.HandleResultInvoked, "Handle result started: $result")

        _state.update { prevValue ->
            if (prevValue != result.state) {
                logger?.log(EventType.StateEmitted, "State emitted: ${result.state.toString()}")
            }
            savedStateHandler?.saveState(result.state)
            result.state
        }

        result.effects.forEach {
            _effect.emit(it)
            logger?.log(EventType.EffectEmitted, "Effect emitted: ${it.toString()}")
        }

        if (actor != null) {
            val toCancel = activeCommandsMutex.withLock {
                result.cancelledCommands.mapNotNull { command ->
                    activeCommandsPull.remove(command)
                }
            }
            toCancel.forEach { it.cancel() }
            for (command in result.cancelledCommands) {
                logger?.log(EventType.CommandCancelled, "Command cancelled: ${command.toString()}")
            }
            for (command in result.commands) {
                val job = viewModelScope.launch(commandsDispatcher) {
                    val flow = actor.execute(command)
                        .onCompletion {
                            activeCommandsMutex.withLock {
                                activeCommandsPull.remove(command)
                            }
                            logger?.log(
                                EventType.CommandCompleted,
                                "Command completed: ${command.toString()}"
                            )
                        }

                    flow.collect { actorEvent ->
                        if (internalReducer != null) {
                            stateMutex.withLock {
                                val newResult =
                                    internalReducer.startProcessing(_state.value, actorEvent)
                                handleReducerResult(newResult)
                            }
                        }
                    }
                }
                val added = activeCommandsMutex.withLock {
                    if (activeCommandsPull.containsKey(command)) false
                    else {
                        activeCommandsPull[command] = job
                        true
                    }
                }
                if (!added) {
                    job.cancel()
                    logger?.log(EventType.CommandSkipped, "Command skipped: ${command.toString()}")
                } else {
                    logger?.log(EventType.CommandStarted, "Command started: ${command.toString()}")
                }
            }
        }

        for (event in result.observersEvents) {
            notify(event)
            logger?.log(EventType.ObserverEventSent, "Observer event sent: $event")
        }
    }

}