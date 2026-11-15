package io.github.gymbay.gelm.app.example.store

import android.util.Log
import io.github.gymbay.gelm.GelmStore
import io.github.gymbay.gelm.app.example.store.components.ExampleActor
import io.github.gymbay.gelm.app.example.store.components.ExampleExternalReducer
import io.github.gymbay.gelm.app.example.store.components.ExampleInternalReducer
import io.github.gymbay.gelm.app.example.store.models.ExampleCommand
import io.github.gymbay.gelm.app.example.store.models.ExampleEffect
import io.github.gymbay.gelm.app.example.store.models.ExampleEvent
import io.github.gymbay.gelm.app.example.store.models.ExampleInternalEvent
import io.github.gymbay.gelm.app.example.store.models.ExampleState
import io.github.gymbay.gelm.utils.GelmSavedStateHandler

/**
 * Demo of a store inherited through the full five-type-parameter [GelmStore] class.
 *
 * It uses an actor and an internal reducer, so `InternalEvent` is [ExampleInternalEvent].
 */
class ExampleStore(
    savedStateHandler: GelmSavedStateHandler<ExampleState>? = null,
) : GelmStore<ExampleState, ExampleEffect, ExampleEvent, ExampleInternalEvent, ExampleCommand>(
    initialState = ExampleState(),
    externalReducer = ExampleExternalReducer(),
    actor = ExampleActor(),
    internalReducer = ExampleInternalReducer(),
    logger = { eventType, message ->
        Log.d("COMPOSE_STORE", "$eventType|$message")
    },
    savedStateHandler = savedStateHandler,
)

fun createExampleStore(savedStateHandler: GelmSavedStateHandler<ExampleState>?): ExampleStore =
    ExampleStore(savedStateHandler)
