package io.github.gymbay.gelm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.CreationExtras
import io.github.gymbay.gelm.reducers.GelmExternalReducer
import io.github.gymbay.gelm.reducers.GelmInternalReducer
import io.github.gymbay.gelm.reducers.Modifier
import io.github.gymbay.gelm.utils.withMainDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import io.github.gymbay.gelm.utils.GelmStore as GelmStoreAlias

@OptIn(ExperimentalCoroutinesApi::class)
class GelmStoreTest_Subclassing {

    @Test
    fun testNamedSubclassHandlesInitAndEvents() = runTest {
        withMainDispatcher(StandardTestDispatcher(testScheduler)) {
            val store = FirstStore(commandsDispatcher = StandardTestDispatcher(testScheduler))
            advanceUntilIdle()

            assertEquals(FirstState(counter = 1), store.state.value)
            assertEquals(listOf(FirstEffect.InitHandled), store.effect.take(1).toList())

            store.sendEvent(FirstEvent.Increment)
            advanceUntilIdle()

            assertEquals(FirstState(counter = 2), store.state.value)
            assertEquals(listOf(FirstEffect.EventHandled), store.effect.take(1).toList())
        }
    }

    @Test
    fun testSubclassThroughFourParameterAliasHandlesInitAndEvents() = runTest {
        withMainDispatcher(StandardTestDispatcher(testScheduler)) {
            val store = AliasStore(commandsDispatcher = StandardTestDispatcher(testScheduler))
            advanceUntilIdle()

            assertEquals(FirstState(counter = 1), store.state.value)
            assertEquals(listOf(FirstEffect.InitHandled), store.effect.take(1).toList())

            store.sendEvent(FirstEvent.Increment)
            advanceUntilIdle()

            assertEquals(FirstState(counter = 2), store.state.value)
            assertEquals(listOf(FirstEffect.EventHandled), store.effect.take(1).toList())
        }
    }

    @Test
    fun testNamedSubclassHandlesActorAndInternalReducer() = runTest {
        withMainDispatcher(StandardTestDispatcher(testScheduler)) {
            val store = SecondStore(commandsDispatcher = StandardTestDispatcher(testScheduler))
            advanceUntilIdle()

            assertEquals(SecondState(value = "loaded"), store.state.value)
            assertEquals(
                listOf(SecondEffect.InitHandled, SecondEffect.DataLoaded),
                store.effect.take(2).toList()
            )

            store.sendEvent(SecondEvent.Reload)
            advanceUntilIdle()

            assertEquals(SecondState(value = "reloaded"), store.state.value)
            assertEquals(
                listOf(SecondEffect.ReloadRequested, SecondEffect.DataLoaded),
                store.effect.take(2).toList()
            )
        }
    }

    @Test
    fun testDifferentSubclassesAreDistinctViewModels() = runTest {
        withMainDispatcher(StandardTestDispatcher(testScheduler)) {
            val viewModelStore = ViewModelStore()
            val provider = ViewModelProvider.create(
                store = viewModelStore,
                factory = StoreFactory(StandardTestDispatcher(testScheduler))
            )

            val first = provider[FirstStore::class]
            val firstAgain = provider[FirstStore::class]
            val second = provider[SecondStore::class]
            val alias = provider[AliasStore::class]

            assertSame(first, firstAgain)
            assertTrue((first as Any) !== (second as Any))
            assertTrue((first as Any) !== (alias as Any))
            assertTrue((second as Any) !== (alias as Any))
            assertEquals(FirstStore::class, first::class)
            assertEquals(SecondStore::class, second::class)
            assertEquals(AliasStore::class, alias::class)

            viewModelStore.clear()
        }
    }

    private class FirstStore(
        commandsDispatcher: CoroutineDispatcher,
    ) : GelmStore<FirstState, FirstEffect, FirstEvent, Nothing, Nothing>(
        initialState = FirstState(),
        externalReducer = FirstExternalReducer(),
        commandsDispatcher = commandsDispatcher,
        effectsReplayCache = 10
    )

    private class SecondStore(
        commandsDispatcher: CoroutineDispatcher,
    ) : GelmStore<SecondState, SecondEffect, SecondEvent, SecondInternalEvent, SecondCommand>(
        initialState = SecondState(),
        externalReducer = SecondExternalReducer(),
        actor = SecondActor(),
        internalReducer = SecondInternalReducer(),
        commandsDispatcher = commandsDispatcher,
        effectsReplayCache = 10
    )

    private class AliasStore(
        commandsDispatcher: CoroutineDispatcher,
    ) : GelmStoreAlias<FirstState, FirstEffect, FirstEvent, Nothing>(
        initialState = FirstState(),
        externalReducer = FirstExternalReducer(),
        commandsDispatcher = commandsDispatcher,
        effectsReplayCache = 10
    )

    private class StoreFactory(
        private val commandsDispatcher: CoroutineDispatcher,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
            when (modelClass) {
                FirstStore::class -> FirstStore(commandsDispatcher)
                SecondStore::class -> SecondStore(commandsDispatcher)
                AliasStore::class -> AliasStore(commandsDispatcher)
                else -> error("Unexpected ViewModel class: $modelClass")
            } as T
    }

    private data class FirstState(
        val counter: Int = 0,
    )

    private enum class FirstEvent {
        Increment
    }

    private enum class FirstEffect {
        InitHandled, EventHandled
    }

    private class FirstExternalReducer :
        GelmExternalReducer<FirstEvent, FirstState, FirstEffect, Nothing>() {
        override fun Modifier<FirstState, FirstEffect, Nothing>.processInit(currentState: FirstState) {
            state { copy(counter = 1) }
            effect(FirstEffect.InitHandled)
        }

        override fun Modifier<FirstState, FirstEffect, Nothing>.processEvent(
            currentState: FirstState,
            event: FirstEvent
        ) {
            when (event) {
                FirstEvent.Increment -> {
                    state { copy(counter = counter + 1) }
                    effect(FirstEffect.EventHandled)
                }
            }
        }
    }

    private data class SecondState(
        val value: String? = null,
    )

    private enum class SecondEvent {
        Reload
    }

    private enum class SecondEffect {
        InitHandled, ReloadRequested, DataLoaded
    }

    private data class SecondCommand(
        val value: String,
    )

    private data class SecondInternalEvent(
        val value: String,
    )

    private class SecondExternalReducer :
        GelmExternalReducer<SecondEvent, SecondState, SecondEffect, SecondCommand>() {
        override fun Modifier<SecondState, SecondEffect, SecondCommand>.processInit(
            currentState: SecondState
        ) {
            effect(SecondEffect.InitHandled)
            command(SecondCommand(value = "loaded"))
        }

        override fun Modifier<SecondState, SecondEffect, SecondCommand>.processEvent(
            currentState: SecondState,
            event: SecondEvent
        ) {
            when (event) {
                SecondEvent.Reload -> {
                    effect(SecondEffect.ReloadRequested)
                    command(SecondCommand(value = "reloaded"))
                }
            }
        }
    }

    private class SecondActor : GelmActor<SecondCommand, SecondInternalEvent>() {
        override suspend fun execute(command: SecondCommand) = flow {
            emit(SecondInternalEvent(value = command.value))
        }
    }

    private class SecondInternalReducer :
        GelmInternalReducer<SecondInternalEvent, SecondState, SecondEffect, SecondCommand>() {
        override fun Modifier<SecondState, SecondEffect, SecondCommand>.processInternalEvent(
            currentState: SecondState,
            internalEvent: SecondInternalEvent
        ) {
            state { copy(value = internalEvent.value) }
            effect(SecondEffect.DataLoaded)
        }
    }
}
