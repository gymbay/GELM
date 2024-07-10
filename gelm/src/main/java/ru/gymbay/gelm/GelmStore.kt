package ru.gymbay.gelm

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gymbay.gelm.observer.GelmObserver
import ru.gymbay.gelm.observer.GelmSubject
import ru.gymbay.gelm.reducers.GelmExternalReducer
import ru.gymbay.gelm.reducers.GelmInternalReducer
import ru.gymbay.gelm.reducers.ReducerResult
import java.util.concurrent.ConcurrentHashMap

/**
 * Central entity for GELM architecture.
 * Holder for architecture components.
 * Responsible for coordinate flow of external and internal events.
 *
 * @param initialState Started point of screen state. Generally it looks like State().
 * @param externalReducer Reducer for processing external [Event] sent by [GelmStore.sendEvent].
 * @param actor Entity for processing asynchronous [Command] from [externalReducer] or [internalReducer].
 * @param internalReducer Reducer for processing [InternalEvent] from [actor].
 * And sending specific events to another store.
 * @param scope Coroutine scope for launching external [Event]. In view model it's viewModelScope
 * @param commandsDispatcher Dispatcher for handling [Command]s in specific thread on [scope].
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
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
    private val commandsDispatcher: CoroutineDispatcher = Dispatchers.Default,
    effectsReplayCache: Int = 1
) : GelmObserver<Event>, GelmSubject() {

    val state: Flow<State>
        get() = _state
    private val _state: MutableStateFlow<State> = MutableStateFlow(initialState)

    val effect: Flow<Effect>
        get() = _effect
            .onEach {
                _effect.resetReplayCache()
            }

    private val _effect: MutableSharedFlow<Effect> = MutableSharedFlow(replay = effectsReplayCache)

    private val activeCommandsPull: MutableMap<Command, Job> = ConcurrentHashMap()

    init {
        val result = externalReducer.startProcessing(initialState)
        handleReducerResult(result)
    }

    /**
     * Sent events to handle in [GelmExternalReducer].
     *
     * @param event External event for handling.
     */
    override fun sendEvent(event: Event) {
        val result = externalReducer.startProcessing(_state.value, event)
        handleReducerResult(result)
    }

    private fun handleReducerResult(result: ReducerResult<State, Effect, Command>) {
        scope.launch {
            _state.update { result.state }
        }

        scope.launch {
            result.effects.forEach {
                _effect.emit(it)
            }
        }

        scope.launch(commandsDispatcher) {
            val actor = actor ?: return@launch
            for (command in result.cancelledCommands) {
                activeCommandsPull.remove(command)?.cancel()
            }
            for (command in result.commands) {
                // filtering duplicating active job
                if (activeCommandsPull.containsKey(command)) {
                    continue
                }
                val job = launch {
                    val flow = actor.execute(command)
                        .onCompletion {
                            activeCommandsPull.remove(command)
                        }

                    if (internalReducer != null) {
                        flow.collect { actorEvent ->
                            val newResult =
                                internalReducer.startProcessing(_state.first(), actorEvent)
                            handleReducerResult(newResult)
                        }
                    }
                }
                activeCommandsPull[command] = job
            }
        }

        scope.launch {
            for (event in result.observersEvents) {
                notify(event)
            }
        }
    }

}