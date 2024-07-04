package ru.gymbay.gelm

import junit.framework.TestCase
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import ru.gymbay.gelm.reducers.GelmExternalReducer
import ru.gymbay.gelm.reducers.Modifier
import ru.gymbay.gelm.utils.MainDispatcherRule

@RunWith(JUnit4::class)
class GelmStoreTest_ManyEffects : TestCase() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun testManyEffects() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val store = GelmStore<Unit, Effect, Nothing, Nothing, Nothing>(
            initialState = Unit,
            externalReducer = TestExternalReducer(),
            commandsDispatcher = testDispatcher,
            effectsReplayCache = 3
        )

        advanceUntilIdle()

        assertEquals(
            listOf(
                Effect.Alert,
                Effect.Toast,
                Effect.Navigation,
            ),
            store.effect.take(3).toList()
        )
    }

    private enum class Effect {
        Alert, Toast, Navigation
    }

    private class TestExternalReducer : GelmExternalReducer<Nothing, Unit, Effect, Nothing>() {
        override fun Modifier<Unit, Effect, Nothing>.processInit(currentState: Unit) {
            effect(Effect.Alert)
            effect(Effect.Toast)
            effect(Effect.Navigation)
        }
    }
}