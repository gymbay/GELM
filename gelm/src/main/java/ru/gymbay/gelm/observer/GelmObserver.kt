package ru.gymbay.gelm.observer

/**
 * For objects that might be observer.
 * Example, [ru.gymbay.gelm.GelmStore].
 */
interface GelmObserver<Event> {
    /**
     * Sent events to handle.
     *
     * @param event Event for handling.
     */
    fun sendEvent(event: Event)
}