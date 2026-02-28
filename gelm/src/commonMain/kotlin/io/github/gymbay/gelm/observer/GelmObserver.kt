package io.github.gymbay.gelm.observer

/**
 * For objects that might be observer.
 * Example, [io.github.gymbay.gelm.GelmStore].
 */
interface GelmObserver<Event> {
    /**
     * Sent events to handle.
     *
     * @param event Event for handling.
     */
    fun sendEvent(event: Event)
}