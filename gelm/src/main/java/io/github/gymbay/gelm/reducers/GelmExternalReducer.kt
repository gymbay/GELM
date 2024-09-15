package io.github.gymbay.gelm.reducers

/**
 * Required entity. Reducer an entity responsible for handling external (generally UI) events.
 * Might be stateless.
 *
 * @param Event - An external event in relation to the system. For example, UI events from the user. Required.
 * @param State - Representation of the current state. Required.
 * @param Effect - One shot event to handel on UI. For example, showing alert or navigate to another screen. Optional.
 * @param Command - Command for run long living async job. Optional.
 *
 * Optional parameters might be replaced to Nothing.
 */
abstract class GelmExternalReducer<Event, State, Effect, Command> {

    /**
     * Override to handle [GelmStore] init phase
     *
     * @param currentState state at the time of the start of event processing
     */
    protected open fun Modifier<State, Effect, Command>.processInit(
        currentState: State
    ) = Unit

    /**
     * Handle [Event] from UI or another [GelmStore]
     *
     * @param currentState state at the time of the start of event processing
     * @param event event from UI or another [GelmStore]
     */
    protected open fun Modifier<State, Effect, Command>.processEvent(
        currentState: State,
        event: Event
    ) = Unit

    /**
     * Start point for processing init phase
     *
     * To test init phase invoke that method manually
     *
     * @param state state at the time of the start of event processing
     * @return result of processing
     */
    fun startProcessing(state: State): ReducerResult<State, Effect, Command> {
        return Modifier<State, Effect, Command>(
            state
        ).apply {
            processInit(state)
        }.result
    }

    /**
     * Start point for processing external events
     *
     * To test event handling invoke that method manually
     *
     * @param state state at the time of the start of event processing
     * @param event event for handling
     * @return result of processing
     */
    fun startProcessing(state: State, event: Event): ReducerResult<State, Effect, Command> {
        return Modifier<State, Effect, Command>(
            state
        ).apply {
            processEvent(state, event)
        }.result
    }

}