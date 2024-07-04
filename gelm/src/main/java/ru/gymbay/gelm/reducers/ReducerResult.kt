package ru.gymbay.gelm.reducers

/**
 * Contains a snapshot of the new state, a list of effects and commands to execute or cancel.
 */
data class ReducerResult<State, Effect, Command>(
    val state: State,
    val effects: List<Effect> = emptyList(),
    val commands: List<Command> = emptyList(),
    val cancelledCommands: List<Command> = emptyList()
)