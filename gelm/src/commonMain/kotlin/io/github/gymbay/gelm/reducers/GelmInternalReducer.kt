package io.github.gymbay.gelm.reducers

/**
 * Optional entity. Internal reducer an entity responsible for handling internal (from [GelmActor]) events.
 * Might be stateless.
 *
 * @param InternalEvent - An internal event. Required.
 * @param State - Representation of the current state. Required.
 * @param Effect - One shot event to handel on UI. For example, showing alert or navigate to another screen. Optional
 * @param Command - Command for run long living async job. Optional.
 *
 * Optional parameters might be replaced to Nothing.
 */
abstract class GelmInternalReducer<InternalEvent, State, Effect, Command> {

    /**
     * Override to handle [GelmActor] internal event
     *
     * @param currentState state at the time of the start of event processing
     * @param internalEvent event from [GelmActor]
     */
    abstract fun Modifier<State, Effect, Command>.processInternalEvent(
        currentState: State,
        internalEvent: InternalEvent
    )

    /**
     * Start point for processing internal events
     *
     * To test event handling invoke that method manually
     *
     * @param state state at the time of the start of event processing
     * @param event internal event for handling
     * @return result of processing
     */
    fun startProcessing(state: State, event: InternalEvent): ReducerResult<State, Effect, Command> {
        return Modifier<State, Effect, Command>(
            state
        ).apply {
            processInternalEvent(state, event)
        }.result
    }

}