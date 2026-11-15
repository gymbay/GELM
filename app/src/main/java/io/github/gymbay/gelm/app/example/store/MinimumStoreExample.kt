package io.github.gymbay.gelm.app.example.store

import io.github.gymbay.gelm.app.example.store.components.ExampleExternalReducer
import io.github.gymbay.gelm.app.example.store.models.ExampleCommand
import io.github.gymbay.gelm.app.example.store.models.ExampleEffect
import io.github.gymbay.gelm.app.example.store.models.ExampleEvent
import io.github.gymbay.gelm.app.example.store.models.ExampleState
import io.github.gymbay.gelm.utils.GelmStore

/**
 * Demo of a store inherited through the four-type-parameter [GelmStore] alias.
 *
 * The alias fixes `InternalEvent` to `Nothing`, so this store has no actor or internal reducer.
 */
class MinimumStore : GelmStore<ExampleState, ExampleEffect, ExampleEvent, ExampleCommand>(
    initialState = ExampleState(),
    externalReducer = ExampleExternalReducer(),
)
