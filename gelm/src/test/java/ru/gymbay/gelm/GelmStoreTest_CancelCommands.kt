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
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class GelmStoreTest_CancelCommands : TestCase() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun testCancelCommands() = runTest {
        val store = GelmStore(
            initialState = State(),
            externalReducer = TestExternalReducer(),
            internalReducer = TestInternalReducer(),
            actor = TestActor(),
            commandsDispatcher = StandardTestDispatcher(testScheduler)
        )

        store.sendEvent(Event.PlanJobs)
        advanceTimeBy(1.seconds)
        store.sendEvent(Event.CancelSomePlannedJob)
        advanceTimeBy(3.seconds)

        assertEquals(State(title = "second"), store.state.first())
    }

    private data class State(
        val title: String? = null
    )

    private enum class Event {
        PlanJobs, CancelSomePlannedJob
    }

    private sealed interface InternalEvent {
        data class Loaded(val value: String) : InternalEvent
    }

    private enum class Command {
        First, Second, Third
    }

    private class TestExternalReducer : GelmExternalReducer<Event, State, Nothing, Command>() {
        override fun Modifier<State, Nothing, Command>.processEvent(
            currentState: State,
            event: Event
        ) {
            when (event) {
                Event.PlanJobs -> {
                    command(Command.First)
                    command(Command.Second)
                    command(Command.Third)
                }
                Event.CancelSomePlannedJob -> {
                    cancelCommand(Command.First)
                    cancelCommand(Command.Third)
                }
            }
        }
    }

    private class TestActor : GelmActor<Command, InternalEvent>() {
        override suspend fun execute(command: Command) = flow<InternalEvent> {
            delay(3.seconds)
            when (command) {
                Command.First -> emit(InternalEvent.Loaded("first"))
                Command.Second -> emit(InternalEvent.Loaded("second"))
                Command.Third -> emit(InternalEvent.Loaded("third"))
            }
        }
    }

    private class TestInternalReducer : GelmInternalReducer<InternalEvent, State, Nothing, Command>() {
        override fun Modifier<State, Nothing, Command>.processInternalEvent(
            currentState: State,
            internalEvent: InternalEvent
        ) {
            when (internalEvent) {
                is InternalEvent.Loaded -> state { copy(title = internalEvent.value) }
            }
        }
    }

}