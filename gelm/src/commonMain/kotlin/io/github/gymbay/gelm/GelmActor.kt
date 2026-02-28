package io.github.gymbay.gelm

import kotlinx.coroutines.flow.Flow

/**
 * Optional entity responsible for handling async [Command].
 *
 * @param Command Async command to execute. Required.
 * @param InternalEvent Event from running async command. Required.
 */
abstract class GelmActor<Command, InternalEvent> {

    /**
     * Invokes from [GelmStore] for run async command.
     *
     * Function will be invoke on Dispatchers.Main.Immediate.
     * You must manually switch Dispatcher to execute command from another thread.
     *
     * @param command Async command to execute
     * @return flow of [InternalEvent]
     */
    abstract suspend fun execute(command: Command): Flow<InternalEvent>

}