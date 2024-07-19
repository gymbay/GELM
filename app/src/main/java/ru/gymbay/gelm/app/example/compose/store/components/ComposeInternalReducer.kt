package ru.gymbay.gelm.app.example.compose.store.components

import ru.gymbay.gelm.app.example.compose.store.models.ComposeCommand
import ru.gymbay.gelm.app.example.compose.store.models.ComposeEffect
import ru.gymbay.gelm.app.example.compose.store.models.ComposeInternalEvent
import ru.gymbay.gelm.app.example.compose.store.models.ComposeState
import ru.gymbay.gelm.reducers.GelmInternalReducer
import ru.gymbay.gelm.reducers.Modifier

class ComposeInternalReducer :
    GelmInternalReducer<ComposeInternalEvent, ComposeState, ComposeEffect, ComposeCommand>() {
    override fun Modifier<ComposeState, ComposeEffect, ComposeCommand>.processInternalEvent(
        currentState: ComposeState,
        internalEvent: ComposeInternalEvent
    ) {
        when (internalEvent) {
            is ComposeInternalEvent.LoadedData -> state {
                copy(
                    isLoading = false,
                    items = internalEvent.list
                )
            }
        }
    }
}