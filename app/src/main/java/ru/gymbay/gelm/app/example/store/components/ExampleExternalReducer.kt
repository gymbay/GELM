package ru.gymbay.gelm.app.example.store.components

import ru.gymbay.gelm.app.example.store.models.ExampleCommand
import ru.gymbay.gelm.app.example.store.models.ExampleEffect
import ru.gymbay.gelm.app.example.store.models.ExampleEvent
import ru.gymbay.gelm.app.example.store.models.ExampleState
import ru.gymbay.gelm.reducers.GelmExternalReducer
import ru.gymbay.gelm.reducers.Modifier

class ExampleExternalReducer :
    GelmExternalReducer<ExampleEvent, ExampleState, ExampleEffect, ExampleCommand>() {

    override fun Modifier<ExampleState, ExampleEffect, ExampleCommand>.processEvent(
        currentState: ExampleState,
        event: ExampleEvent
    ) {
        when (event) {
            ExampleEvent.Reload -> {
                state { copy(isLoading = true) }
                command(ExampleCommand.StartLoading(text = currentState.editField))
            }

            is ExampleEvent.TypeText -> {
                state { copy(isLoading = true, editField = event.text) }
                cancelCommand(ExampleCommand.StartLoading(text = currentState.editField))
                command(ExampleCommand.StartLoading(text = event.text))
            }

            is ExampleEvent.Next -> effect(ExampleEffect.NavigateToScreen)
        }
    }

}