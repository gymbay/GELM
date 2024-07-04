package ru.gymbay.gelm

import androidx.annotation.AnyThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.gymbay.gelm.observer.GelmObserver
import ru.gymbay.gelm.observer.GelmSubjectActions
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
 * @param internalReducer Reducer for processing [InternalEvent] from [actor].
 * @param actor Entity for processing asynchronous [Command] from [externalReducer] or [internalReducer].
 * And sending specific events to another store.
 * @param commandsDispatcher Dispatcher for handling [Command]s. Useful for tests. Default is [Dispatchers.Default].
 * @param effectsReplayCache How many effects will be repeated to first subscriber.
 * Useful when effects happens in background or before first subscribe. Default is 1.
 *
 * @property state Reactive stateful flow of state. Subscribe on it in UI.
 * @property effect Reactive one-shot events, stateless.
 * If no subscribers when event happens, it will be stored and replay to new subscriber.
 */
class GelmStore<State, Effect, Event, InternalEvent, Command>(
    initialState: State,
    private val externalReducer: GelmExternalReducer<Event, State, Effect, Command>,
    private val internalReducer: GelmInternalReducer<InternalEvent, State, Effect, Command>? = null,
    private val actor: GelmActor<Command, InternalEvent>? = null,
    private val commandsDispatcher: CoroutineDispatcher = Dispatchers.Default,
    effectsReplayCache: Int = 1
) : ViewModel(), GelmObserver<Event>, GelmSubjectActions {

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
    @AnyThread
    override fun sendEvent(event: Event) {
        val result = externalReducer.startProcessing(_state.value, event)
        handleReducerResult(result)
    }

    /**
     * Subscribe another [GelmStore] to events from that [GelmStore].
     *
     * @param observer Generally another [GelmStore].
     */
    override fun subscribe(observer: GelmObserver<*>) {
        actor?.subscribe(observer)
    }

    /**
     * Unsubscribe another [GelmStore].
     *
     * @param observer Generally another [GelmStore].
     */
    override fun unsubscribe(observer: GelmObserver<*>) {
        actor?.unsubscribe(observer)
    }

    private fun handleReducerResult(result: ReducerResult<State, Effect, Command>) {
        viewModelScope.launch {
            _state.update { result.state }
        }

        viewModelScope.launch {
            result.effects.forEach {
                _effect.emit(it)
            }
        }

        viewModelScope.launch(commandsDispatcher) {
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
    }

}