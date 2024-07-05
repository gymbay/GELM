package ru.gymbay.gelm

import junit.framework.TestCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import ru.gymbay.gelm.reducers.GelmExternalReducer
import ru.gymbay.gelm.reducers.GelmInternalReducer
import ru.gymbay.gelm.reducers.Modifier

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class GelmStoreTest_SendEvent : TestCase() {

    @Test
    fun testSendEvent() = runTest {
        val store = GelmStore(
            initialState = State(),
            externalReducer = TestExternalReducer(),
            internalReducer = TestInternalReducer(),
            actor = TestActor(),
            scope = CoroutineScope(UnconfinedTestDispatcher()),
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
            when(command) {
                Command.Load -> emit(InternalEvent.Loaded)
            }
        }
    }

    private class TestInternalReducer : GelmInternalReducer<InternalEvent, State, Effect, Command>() {
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

