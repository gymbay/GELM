package io.github.gymbay.gelm.app.example.store

import android.util.Log
import io.github.gymbay.gelm.GelmStore
import io.github.gymbay.gelm.app.example.store.components.ExampleActor
import io.github.gymbay.gelm.app.example.store.components.ExampleExternalReducerStateMachine
import io.github.gymbay.gelm.app.example.store.components.ExampleInternalReducerStateMachine
import io.github.gymbay.gelm.app.example.store.models.ExampleCommand
import io.github.gymbay.gelm.app.example.store.models.ExampleEffect
import io.github.gymbay.gelm.app.example.store.models.ExampleEvent
import io.github.gymbay.gelm.app.example.store.models.ExampleInternalEvent
import io.github.gymbay.gelm.app.example.store.models.ExampleStateMachine
import io.github.gymbay.gelm.utils.GelmLogger

typealias ExampleStateMachineStore = GelmStore<ExampleStateMachine, ExampleEffect, ExampleEvent, ExampleInternalEvent, ExampleCommand>

fun createExampleStateMachineStore(): ExampleStateMachineStore =
    GelmStore(
        initialState = ExampleStateMachine.Idle.initial(),
        externalReducer = ExampleExternalReducerStateMachine(),
        actor = ExampleActor(),
        internalReducer = ExampleInternalReducerStateMachine(),
        logger = GelmLogger { eventType, message ->
            Log.d("COMPOSE_STORE", "$eventType|$message")
        }
    )