package io.github.gymbay.gelm

import io.github.gymbay.gelm.reducers.GelmExternalReducer
import io.github.gymbay.gelm.reducers.GelmInternalReducer
import io.github.gymbay.gelm.reducers.Modifier
import io.github.gymbay.gelm.utils.withMainDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GelmStoreTest_ParallelSendEvent {

    @Test
    fun testParallelSendEvents() {
        val testDispatcher = StandardTestDispatcher()
        runTest(testDispatcher) {
            withMainDispatcher(testDispatcher) {
                val store = GelmStore(
                    initialState = State(),
                    externalReducer = TestExternalReducer(),
                    internalReducer = TestInternalReducer(),
                    actor = TestActor(),
                    commandsDispatcher = testDispatcher
                )

                coroutineScope {
                    store.sendEvent(Event.SecondEvent)
                }
                coroutineScope {
                    store.sendEvent(Event.FirstEvent)
                }
                coroutineScope {
                    store.sendEvent(Event.ThirdEvent)
                }
                advanceUntilIdle()

                assertEquals(
                    State(field1 = "FirstEvent", field2 = "SecondEvent", field3 = "ThirdEvent"),
                    store.state.first()
                )
            }
        }
    }

    private data class State(
        val field1: String = "NotFilled",
        val field2: String = "NotFilled",
        val field3: String = "NotFilled",
    )

    private enum class Event {
        FirstEvent, SecondEvent, ThirdEvent
    }

    private enum class Command {
        Load
    }

    private enum class InternalEvent {
        ThirdEvent
    }

    private class TestExternalReducer : GelmExternalReducer<Event, State, Nothing, Command>() {
        override fun Modifier<State, Nothing, Command>.processEvent(
            currentState: State,
            event: Event
        ) {
            when (event) {
                Event.FirstEvent -> {
                    state { copy(field1 = "FirstEvent") }
                }

                Event.SecondEvent -> {
                    state { copy(field2 = "SecondEvent") }
                }

                Event.ThirdEvent -> {
                    command(Command.Load)
                }
            }
        }
    }

    private class TestActor : GelmActor<Command, InternalEvent>() {
        override suspend fun execute(command: Command) = flow<InternalEvent> {
            when (command) {
                Command.Load -> emit(InternalEvent.ThirdEvent)
            }
        }
    }

    private class TestInternalReducer :
        GelmInternalReducer<InternalEvent, State, Nothing, Command>() {
        override fun Modifier<State, Nothing, Command>.processInternalEvent(
            currentState: State,
            internalEvent: InternalEvent
        ) {
            when (internalEvent) {
                InternalEvent.ThirdEvent -> {
                    state { copy(field3 = "ThirdEvent") }
                }
            }
        }
    }
}
