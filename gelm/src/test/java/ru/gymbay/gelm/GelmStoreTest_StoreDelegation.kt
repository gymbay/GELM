package ru.gymbay.gelm

import junit.framework.TestCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import ru.gymbay.gelm.reducers.GelmExternalReducer
import ru.gymbay.gelm.reducers.Modifier

@RunWith(JUnit4::class)
class GelmStoreTest_StoreDelegation : TestCase() {

    @Test
    fun testStoreDelegation() = runTest {
        val initialStore = GelmStore(
            initialState = Unit,
            externalReducer = InitialExternalReducer(),
            actor = InitialActor(),
            scope = CoroutineScope(UnconfinedTestDispatcher()),
            commandsDispatcher = StandardTestDispatcher(testScheduler)
        )

        val delegationStore = GelmStore<DelegationState, Nothing, DelegationEvent, Nothing, Nothing>(
            initialState = DelegationState(),
            externalReducer = DelegationExternalReducer(),
            scope = this
        )

        initialStore.subscribe(delegationStore)
        assertNull(delegationStore.state.first().title)

        val newTitle = "initial"
        initialStore.sendEvent(InitialEvent.StartDelegation(title = newTitle))
        advanceUntilIdle()
        assertEquals(DelegationState(title = newTitle), delegationStore.state.first())
    }

    private sealed interface InitialEvent {
        data class StartDelegation(val title: String) : InitialEvent
    }

    private sealed interface InitialCommand {
        data class StartDelegation(val title: String) : InitialCommand
    }

    private class InitialExternalReducer : GelmExternalReducer<InitialEvent, Unit, Nothing, InitialCommand>() {
        override fun Modifier<Unit, Nothing, InitialCommand>.processEvent(
            currentState: Unit,
            event: InitialEvent
        ) {
            when (event) {
                is InitialEvent.StartDelegation -> command(InitialCommand.StartDelegation(event.title))
            }
        }
    }

    private class InitialActor: GelmActor<InitialCommand, Nothing>() {
        override suspend fun execute(command: InitialCommand): Flow<Nothing> {
            when (command) {
                is InitialCommand.StartDelegation -> {
                    notify(DelegationEvent.Data(title = command.title))
                }
            }
            return emptyFlow()
        }
    }

    private data class DelegationState(
        val title: String? = null
    )

    private sealed interface DelegationEvent {
        data class Data(val title: String) : DelegationEvent
    }

    private class DelegationExternalReducer : GelmExternalReducer<DelegationEvent, DelegationState, Nothing, Nothing>() {
        override fun Modifier<DelegationState, Nothing, Nothing>.processEvent(
            currentState: DelegationState,
            event: DelegationEvent
        ) {
            when (event) {
                is DelegationEvent.Data -> state { copy(title = event.title) }
            }
        }
    }

}