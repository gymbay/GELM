package io.github.gymbay.gelm

import io.github.gymbay.gelm.reducers.GelmExternalReducer
import io.github.gymbay.gelm.reducers.GelmInternalReducer
import io.github.gymbay.gelm.reducers.Modifier
import io.github.gymbay.gelm.utils.withMainDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GelmStoreTest_SendEvent {

    @Test
    fun testSendEvent() = runTest {
        withMainDispatcher {
            val store = GelmStore(
                initialState = State(),
                externalReducer = TestExternalReducer(),
                internalReducer = TestInternalReducer(),
                actor = TestActor(),
                commandsDispatcher = StandardTestDispatcher(testScheduler)
            )

            store.sendEvent(Event.FirstEvent)
            assertEquals(State(title = "external"), store.state.first())
            assertEquals(Effect.Toast, store.effect.first())

            store.sendEvent(Event.SecondEvent)
            advanceUntilIdle()
            assertEquals(State(title = "internal"), store.state.first())
            assertEquals(Effect.Alert, store.effect.first())
        }
    }

    private data class State(
        val title: String? = null,
    )

    private enum class Event {
        FirstEvent, SecondEvent
    }

    private enum class Effect {
        Toast, Alert
    }

    private enum class Command {
        Load
    }

    private enum class InternalEvent {
        Loaded
    }

    private class TestExternalReducer : GelmExternalReducer<Event, State, Effect, Command>() {
        override fun Modifier<State, Effect, Command>.processEvent(
            currentState: State,
            event: Event
        ) {
            when (event) {
                Event.FirstEvent -> {
                    state { copy(title = "external") }
                    effect(Effect.Toast)
                }

                Event.SecondEvent -> {
                    command(Command.Load)
                }
            }
        }
    }

    private class TestActor : GelmActor<Command, InternalEvent>() {
        override suspend fun execute(command: Command) = flow<InternalEvent> {
            when (command) {
                Command.Load -> emit(InternalEvent.Loaded)
            }
        }
    }

    private class TestInternalReducer :
        GelmInternalReducer<InternalEvent, State, Effect, Command>() {
        override fun Modifier<State, Effect, Command>.processInternalEvent(
            currentState: State,
            internalEvent: InternalEvent
        ) {
            when (internalEvent) {
                InternalEvent.Loaded -> {
                    state { copy(title = "internal") }
                    effect(Effect.Alert)
                }
            }
        }
    }
}
