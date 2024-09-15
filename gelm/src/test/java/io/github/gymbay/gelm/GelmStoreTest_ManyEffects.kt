package io.github.gymbay.gelm

import io.github.gymbay.gelm.reducers.GelmExternalReducer
import io.github.gymbay.gelm.reducers.Modifier
import io.github.gymbay.gelm.utils.MainDispatcherRule
import junit.framework.TestCase
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GelmStoreTest_ManyEffects : TestCase() {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun testManyEffects() = runTest {
        val store = GelmStore<Unit, Effect, Nothing, Nothing, Nothing>(
            initialState = Unit,
            externalReducer = TestExternalReducer(),
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