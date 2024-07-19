package ru.gymbay.gelm.app.example.compose.store

import android.util.Log
import ru.gymbay.gelm.GelmLogger
import ru.gymbay.gelm.GelmStore
import ru.gymbay.gelm.app.example.compose.store.components.ComposeActor
import ru.gymbay.gelm.app.example.compose.store.components.ComposeExternalReducer
import ru.gymbay.gelm.app.example.compose.store.components.ComposeInternalReducer
import ru.gymbay.gelm.app.example.compose.store.models.ComposeCommand
import ru.gymbay.gelm.app.example.compose.store.models.ComposeEffect
import ru.gymbay.gelm.app.example.compose.store.models.ComposeEvent
import ru.gymbay.gelm.app.example.compose.store.models.ComposeInternalEvent
import ru.gymbay.gelm.app.example.compose.store.models.ComposeState

typealias ComposeStore = GelmStore<ComposeState, ComposeEffect, ComposeEvent, ComposeInternalEvent, ComposeCommand>

fun createComposeStore(): ComposeStore = GelmStore(
    initialState = ComposeState(),
    externalReducer = ComposeExternalReducer(),
    actor = ComposeActor(),
    internalReducer = ComposeInternalReducer(),
    logger = GelmLogger { eventType, message ->
        Log.d("COMPOSE_STORE", "$eventType|$message")
    }
)