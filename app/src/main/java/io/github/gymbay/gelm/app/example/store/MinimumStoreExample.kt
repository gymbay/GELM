package io.github.gymbay.gelm.app.example.store

import io.github.gymbay.gelm.app.example.store.components.ExampleExternalReducer
import io.github.gymbay.gelm.app.example.store.models.ExampleState
import io.github.gymbay.gelm.utils.GelmStore

fun createStore() = GelmStore(
    initialState = ExampleState(),
    externalReducer = ExampleExternalReducer()
)