package io.github.gymbay.gelm

import io.github.gymbay.gelm.reducers.GelmExternalReducer
import io.github.gymbay.gelm.reducers.GelmInternalReducer
import io.github.gymbay.gelm.reducers.Modifier
import io.github.gymbay.gelm.utils.EventType
import io.github.gymbay.gelm.utils.GelmLogger
import io.github.gymbay.gelm.utils.MainDispatcherRule
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class GelmStoreTest_ManyCommands : TestCase() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun testManyCommands() = runTest {
        val store = GelmStore(
            initialState = State(),
            externalReducer = TestExternalReducer(),
            internalReducer = TestInternalReducer(),
            actor = TestActor(),
            commandsDispatcher = StandardTestDispatcher(testScheduler),
            logger = object : GelmLogger {
                override fun log(eventType: EventType, message: String) {
                    println("$eventType = $message")
                }
            }
        )

        advanceUntilIdle()

        assertEquals(
            State(
                command1Result = true,
                command2Result = true,
                command3Result = true
            ),
            store.state.first()
        )
    }

    private data class State(
        val command1Result: Boolean = false,
        val command2Result: Boolean = false,
        val command3Result: Boolean = false,
    )

    private enum class Command {
        Command1,
        Command2,
        Command3
    }

    private enum class InternalEvent {
        Result1,
        Result2,
        Result3
    }

    private class TestExternalReducer : GelmExternalReducer<Nothing, State, Nothing, Command>() {
        override fun Modifier<State, Nothing, Command>.processInit(currentState: State) {
            command(Command.Command1)
            command(Command.Command2)
            command(Command.Command3)
        }
    }

    private class TestActor : GelmActor<Command, InternalEvent>() {
        override suspend fun execute(command: Command) = flow<InternalEvent> {
            when (command) {
                Command.Command1 -> emit(InternalEvent.Result1)
                Command.Command2 -> emit(InternalEvent.Result2)
                Command.Command3 -> emit(InternalEvent.Result3)
            }
        }
    }

    private class TestInternalReducer : GelmInternalReducer<InternalEvent, State, Nothing, Command>() {
        override fun Modifier<State, Nothing, Command>.processInternalEvent(
            currentState: State,
            internalEvent: InternalEvent
        ) {
            when (internalEvent) {
                InternalEvent.Result1 -> state { copy(command1Result = true) }
                InternalEvent.Result2 -> state { copy(command2Result = true) }
                InternalEvent.Result3 -> state { copy(command3Result = true) }
            }
        }
    }

}