package io.github.gymbay.gelm.reducers

/**
 * Contains a snapshot of the new state, a list of effects and commands to execute or cancel.
 */
data class ReducerResult<State, Effect, Command>(
    val state: State,
    val effects: List<Effect>,
    val commands: List<Command>,
    val cancelledCommands: List<Command>,
    val observersEvents: List<Any>
)