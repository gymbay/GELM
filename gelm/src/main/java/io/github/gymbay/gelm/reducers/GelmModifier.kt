package io.github.gymbay.gelm.reducers

/**
 * Class for making and accumulating changes to the current [State],
 * accumulating list of [Effect], list of [Command] and list of [ObserverEvent].
 *
 * All accumulating changes will be applied at the end of reducer work.
 */
class Modifier<State, Effect, Command>(
    state: State
) {

    val result
        get() = ReducerResult(
            internalState,
            internalEffects,
            internalCommands,
            internalCancelledCommands,
            observersEvents
        )

    private var internalState: State = state
    private val internalEffects: MutableList<Effect> = mutableListOf()
    private val internalCommands: MutableList<Command> = mutableListOf()
    private val internalCancelledCommands: MutableList<Command> = mutableListOf()
    private val observersEvents: MutableList<Any> = mutableListOf()

    /**
     * Replaced current state to new [State].
     * Might be used for modifying fields of current [State].
     * State be applied at the end of reducer work.
     *
     * Example: state { copy(isLoading = true) }
     *
     * @param modify block for modifying new state
     */
    fun state(modify: State.() -> State) {
        internalState = internalState.modify()
    }

    /**
     * Added new [Effect] at the list of effects.
     *
     * The effects will be applied in the order they were added.
     *
     * @param newEffect one-shot event
     */
    fun effect(newEffect: Effect) {
        internalEffects.add(newEffect)
    }

    /**
     * Added new [Command] at the list of commands.
     *
     * The commands will be applied in the order they were added.
     * Duplicated or active commands will be filtered.
     *
     * @param newCommand async command
     */
    fun command(newCommand: Command) {
        internalCommands.add(newCommand)
    }

    /**
     * Cancelled running command if exists
     *
     * Old commands will be canceling before starting new commands.
     *
     * @param command async command for cancelling
     */
    fun cancelCommand(command: Command) {
        internalCancelledCommands.add(command)
    }

    /**
     * Event for observers subscribed on own [GelmStore]
     *
     * @param event The event will be sent to the observer.
     */
    fun event(event: Any) {
        observersEvents.add(event)
    }

}