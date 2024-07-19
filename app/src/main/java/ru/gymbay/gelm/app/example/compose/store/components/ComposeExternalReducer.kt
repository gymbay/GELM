package ru.gymbay.gelm.app.example.compose.store.components

import ru.gymbay.gelm.app.example.compose.store.models.ComposeCommand
import ru.gymbay.gelm.app.example.compose.store.models.ComposeEffect
import ru.gymbay.gelm.app.example.compose.store.models.ComposeEvent
import ru.gymbay.gelm.app.example.compose.store.models.ComposeState
import ru.gymbay.gelm.reducers.GelmExternalReducer
import ru.gymbay.gelm.reducers.Modifier

class ComposeExternalReducer :
    GelmExternalReducer<ComposeEvent, ComposeState, ComposeEffect, ComposeCommand>() {

    override fun Modifier<ComposeState, ComposeEffect, ComposeCommand>.processEvent(
        currentState: ComposeState,
        event: ComposeEvent
    ) {
        when (event) {
            ComposeEvent.Reload -> {
                state { copy(isLoading = true) }
                command(ComposeCommand.StartLoading(text = currentState.editField))
            }

            is ComposeEvent.TypeText -> {
                state { copy(isLoading = true, editField = event.text) }
                cancelCommand(ComposeCommand.StartLoading(text = currentState.editField))
                command(ComposeCommand.StartLoading(text = event.text))
            }

            is ComposeEvent.Next -> effect(ComposeEffect.NavigateToScreen)
        }
    }

}