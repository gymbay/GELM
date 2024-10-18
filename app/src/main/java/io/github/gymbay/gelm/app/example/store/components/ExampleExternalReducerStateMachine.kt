package io.github.gymbay.gelm.app.example.store.components

import io.github.gymbay.gelm.app.example.store.models.ExampleCommand
import io.github.gymbay.gelm.app.example.store.models.ExampleEffect
import io.github.gymbay.gelm.app.example.store.models.ExampleEvent
import io.github.gymbay.gelm.app.example.store.models.ExampleStateMachine
import io.github.gymbay.gelm.reducers.GelmExternalReducer
import io.github.gymbay.gelm.reducers.Modifier

private typealias StateModifier = Modifier<ExampleStateMachine, ExampleEffect, ExampleCommand>

// With State machine
class ExampleExternalReducerStateMachine :
    GelmExternalReducer<ExampleEvent, ExampleStateMachine, ExampleEffect, ExampleCommand>() {

    override fun Modifier<ExampleStateMachine, ExampleEffect, ExampleCommand>.processEvent(
        currentState: ExampleStateMachine,
        event: ExampleEvent
    ) {
        when (currentState) {
            is ExampleStateMachine.Idle -> processIdleState(currentState, event)
            is ExampleStateMachine.Fetching -> processFetchingState(currentState, event)
        }
    }

    private fun StateModifier.processIdleState(
        idleState: ExampleStateMachine.Idle,
        event: ExampleEvent
    ) {
        when (event) {
            ExampleEvent.Reload -> {
                state {
                    ExampleStateMachine.Fetching(
                        title = idleState.title,
                        editField = idleState.editField
                    )
                }
                command(ExampleCommand.StartLoading(text = idleState.editField))
            }

            ExampleEvent.Next -> {
                effect(ExampleEffect.NavigateToScreen)
            }

            is ExampleEvent.TypeText -> {
                state {
                    ExampleStateMachine.Fetching(
                        title = idleState.title,
                        editField = event.text
                    )
                }
                cancelCommand(ExampleCommand.StartLoading(text = idleState.editField))
                command(ExampleCommand.StartLoading(text = event.text))
            }
        }
    }

    private fun StateModifier.processFetchingState(
        fetchingState: ExampleStateMachine.Fetching,
        event: ExampleEvent
    ) {
        when (event) {
            ExampleEvent.Next -> {
                cancelCommand(ExampleCommand.StartLoading(text = fetchingState.editField))
                effect(ExampleEffect.NavigateToScreen)
            }

            is ExampleEvent.TypeText -> {
                cancelCommand(ExampleCommand.StartLoading(text = fetchingState.editField))
                command(ExampleCommand.StartLoading(text = event.text))
                state { fetchingState.copy(editField = event.text) }
            }

            else -> return
        }
    }

}