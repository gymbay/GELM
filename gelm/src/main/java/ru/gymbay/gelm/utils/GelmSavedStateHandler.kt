package ru.gymbay.gelm.utils

/**
 * Interface for handling saving state actions
 */
interface GelmSavedStateHandler<State> {
    fun saveState(state: State)
    fun restoreState(initialState: State): State?
}