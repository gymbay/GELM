package ru.gymbay.gelm.app.example.store

import android.util.Log
import ru.gymbay.gelm.GelmLogger
import ru.gymbay.gelm.GelmStore
import ru.gymbay.gelm.app.example.store.components.ExampleActor
import ru.gymbay.gelm.app.example.store.components.ExampleExternalReducer
import ru.gymbay.gelm.app.example.store.components.ExampleInternalReducer
import ru.gymbay.gelm.app.example.store.models.ExampleCommand
import ru.gymbay.gelm.app.example.store.models.ExampleEffect
import ru.gymbay.gelm.app.example.store.models.ExampleEvent
import ru.gymbay.gelm.app.example.store.models.ExampleInternalEvent
import ru.gymbay.gelm.app.example.store.models.ExampleState

typealias ExampleStore = GelmStore<ExampleState, ExampleEffect, ExampleEvent, ExampleInternalEvent, ExampleCommand>

fun createExampleStore(): ExampleStore = GelmStore(
    initialState = ExampleState(),
    externalReducer = ExampleExternalReducer(),
    actor = ExampleActor(),
    internalReducer = ExampleInternalReducer(),
    logger = GelmLogger { eventType, message ->
        Log.d("COMPOSE_STORE", "$eventType|$message")
    }
)