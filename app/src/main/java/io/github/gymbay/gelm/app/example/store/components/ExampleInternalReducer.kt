package io.github.gymbay.gelm.app.example.store.components

import io.github.gymbay.gelm.app.example.store.models.ExampleCommand
import io.github.gymbay.gelm.app.example.store.models.ExampleEffect
import io.github.gymbay.gelm.app.example.store.models.ExampleInternalEvent
import io.github.gymbay.gelm.app.example.store.models.ExampleState
import io.github.gymbay.gelm.app.example.store.models.ExampleStateMachine
import io.github.gymbay.gelm.reducers.GelmInternalReducer
import io.github.gymbay.gelm.reducers.Modifier

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

private typealias InternalStateModifier = Modifier<ExampleStateMachine, ExampleEffect, ExampleCommand>

class ExampleInternalReducerStateMachine :
    GelmInternalReducer<ExampleInternalEvent, ExampleStateMachine, ExampleEffect, ExampleCommand>() {

    override fun Modifier<ExampleStateMachine, ExampleEffect, ExampleCommand>.processInternalEvent(
        currentState: ExampleStateMachine,
        internalEvent: ExampleInternalEvent
    ) {
        when (currentState) {
            is ExampleStateMachine.Idle -> Unit
            is ExampleStateMachine.Fetching -> processFetchingState(currentState, internalEvent)
        }
    }

    private fun InternalStateModifier.processFetchingState(
        fetchingState: ExampleStateMachine.Fetching,
        event: ExampleInternalEvent
    ) {
        when (event) {
            is ExampleInternalEvent.LoadedData -> {
                state {
                    ExampleStateMachine.Idle(
                        title = fetchingState.title,
                        editField = fetchingState.editField,
                        items = event.list
                    )
                }
            }
        }
    }

}