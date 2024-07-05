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
class GelmStoreTest_InitEvent: TestCase() {

    @Test
    fun testInitEvent() = runTest {
        val store = GelmStore(
            initialState = State(),
            externalReducer = TestExternalReducer(),
            internalReducer = TestInternalReducer(),
            actor = TestActor(),
            scope = CoroutineScope(UnconfinedTestDispatcher()),
            commandsDispatcher = StandardTestDispatcher(testScheduler)
        )

        assertEquals(State(title = "init"), store.state.first())
        assertEquals(Effect.FromExternalReducerInit, store.effect.first())
        advanceUntilIdle()
        assertEquals(State(title = "loaded"), store.state.first())
        assertEquals(Effect.FromInternalReducer, store.effect.first())
    }

    private data class State(
        val title: String? = null,
    )

    private enum class Effect {
        FromExternalReducerInit, FromInternalReducer
    }

    private enum class Command {
        Load
    }

    private enum class InternalEvent {
        Loaded
    }

    private class TestExternalReducer : GelmExternalReducer<Nothing, State, Effect, Command>() {

        override fun Modifier<State, Effect, Command>.processInit(currentState: State) {
            state { copy(title = "init") }
            effect(Effect.FromExternalReducerInit)
            command(Command.Load)
        }

    }

    private class TestInternalReducer : GelmInternalReducer<InternalEvent, State, Effect, Command>() {
        override fun Modifier<State, Effect, Command>.processInternalEvent(
            currentState: State,
            internalEvent: InternalEvent
        ) {
            when (internalEvent) {
                InternalEvent.Loaded -> {
                    state { copy(title = "loaded") }
                    effect(Effect.FromInternalReducer)
                }
            }
        }
    }

    private class TestActor : GelmActor<Command, InternalEvent>() {
        override suspend fun execute(command: Command) = flow {
            emit(InternalEvent.Loaded)
        }
    }

}



