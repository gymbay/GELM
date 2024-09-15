package io.github.gymbay.gelm

import io.github.gymbay.gelm.reducers.GelmExternalReducer
import io.github.gymbay.gelm.reducers.Modifier
import io.github.gymbay.gelm.utils.MainDispatcherRule
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class GelmStoreTest_StoreDelegation : TestCase() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun testStoreDelegation() = runTest {
        val initialStore = GelmStore<Unit, Nothing, InitialEvent, Nothing, Nothing>(
            initialState = Unit,
            externalReducer = InitialExternalReducer(),
            commandsDispatcher = StandardTestDispatcher(testScheduler)
        )

        val delegationStore = GelmStore<DelegationState, Nothing, DelegationEvent, Nothing, Nothing>(
            initialState = DelegationState(),
            externalReducer = DelegationExternalReducer(),
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

    private class InitialExternalReducer :
        GelmExternalReducer<InitialEvent, Unit, Nothing, Nothing>() {
        override fun Modifier<Unit, Nothing, Nothing>.processEvent(
            currentState: Unit,
            event: InitialEvent
        ) {
            when (event) {
                is InitialEvent.StartDelegation -> event(DelegationEvent.Data(title = event.title))
            }
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