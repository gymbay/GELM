package io.github.gymbay.gelm

import io.github.gymbay.gelm.reducers.GelmExternalReducer
import io.github.gymbay.gelm.reducers.Modifier
import io.github.gymbay.gelm.utils.withMainDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GelmStoreTest_ManyEffects {

    @Test
    fun testManyEffects() = runTest {
        withMainDispatcher {
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
