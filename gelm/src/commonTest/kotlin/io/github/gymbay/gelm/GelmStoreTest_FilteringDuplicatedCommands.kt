package io.github.gymbay.gelm

import io.github.gymbay.gelm.reducers.GelmExternalReducer
import io.github.gymbay.gelm.reducers.GelmInternalReducer
import io.github.gymbay.gelm.reducers.Modifier
import io.github.gymbay.gelm.utils.withMainDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class GelmStoreTest_FilteringDuplicatedCommands {

    @Test
    fun testCancelledJob() = runTest {
        withMainDispatcher(StandardTestDispatcher(testScheduler)) {
            val store = GelmStore(
                initialState = State(),
                externalReducer = TestExternalReducer(),
                internalReducer = TestInternalReducer(),
                actor = TestActor(),
                commandsDispatcher = StandardTestDispatcher(testScheduler)
            )

            store.sendEvent(Event.StartLoading)
            advanceTimeBy(200.milliseconds)
            store.sendEvent(Event.StartLoading)
            advanceTimeBy(200.milliseconds)
            store.sendEvent(Event.StartLoading)
            advanceTimeBy(5.seconds)

            assertEquals(State(loadedCount = 1), store.state.first())
        }
    }

    private data class State(
        val loadedCount: Int = 0,
    )

    private enum class Event {
        StartLoading
    }

    private enum class Command {
        Load
    }

    private enum class InternalEvent {
        Loaded
    }

    private class TestExternalReducer : GelmExternalReducer<Event, State, Nothing, Command>() {
        override fun Modifier<State, Nothing, Command>.processEvent(
            currentState: State,
            event: Event
        ) {
            when (event) {
                Event.StartLoading -> command(Command.Load)
            }
        }
    }

    private class TestActor : GelmActor<Command, InternalEvent>() {
        override suspend fun execute(command: Command) = flow<InternalEvent> {
            when (command) {
                Command.Load -> {
                    delay(1.seconds)
                    emit(InternalEvent.Loaded)
                }
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
                InternalEvent.Loaded -> {
                    val newLoadedCount = currentState.loadedCount + 1
                    state { copy(loadedCount = newLoadedCount) }
                }
            }
        }
    }
}
