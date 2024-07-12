package ru.gymbay.gelm

import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import ru.gymbay.gelm.reducers.GelmExternalReducer
import ru.gymbay.gelm.reducers.GelmInternalReducer
import ru.gymbay.gelm.reducers.Modifier
import ru.gymbay.gelm.utils.MainDispatcherRule
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class GelmStoreTest_FilteringDuplicatedCommands : TestCase() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun testCancelledJob() = runTest {
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

    private class TestInternalReducer : GelmInternalReducer<InternalEvent, State, Nothing, Command>() {
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