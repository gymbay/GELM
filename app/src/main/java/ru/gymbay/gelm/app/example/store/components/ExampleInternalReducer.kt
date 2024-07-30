package ru.gymbay.gelm.app.example.store.components

import ru.gymbay.gelm.app.example.store.models.ExampleCommand
import ru.gymbay.gelm.app.example.store.models.ExampleEffect
import ru.gymbay.gelm.app.example.store.models.ExampleInternalEvent
import ru.gymbay.gelm.app.example.store.models.ExampleState
import ru.gymbay.gelm.reducers.GelmInternalReducer
import ru.gymbay.gelm.reducers.Modifier

class ExampleInternalReducer :
    GelmInternalReducer<ExampleInternalEvent, ExampleState, ExampleEffect, ExampleCommand>() {
    override fun Modifier<ExampleState, ExampleEffect, ExampleCommand>.processInternalEvent(
        currentState: ExampleState,
        internalEvent: ExampleInternalEvent
    ) {
        when (internalEvent) {
            is ExampleInternalEvent.LoadedData -> state {
                copy(
                    isLoading = false,
                    items = internalEvent.list
                )
            }
        }
    }
}