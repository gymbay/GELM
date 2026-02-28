package io.github.gymbay.gelm.observer

/**
 * For objects that sending events to [GelmObserver]s.
 */
interface GelmSubject : GelmSubjectActions {
    val observers: MutableList<GelmObserver<*>>

    /**
     * Subscribe observer.
     *
     * @param observer Observer for handling events.
     */
    override fun subscribe(observer: GelmObserver<*>) {
        observers.add(observer)
    }

    /**
     * Unsubscribe observer.
     *
     * @param observer Observer for removing from watch list.
     */
    override fun unsubscribe(observer: GelmObserver<*>) {
        observers.remove(observer)
    }

    /**
     * Notify all observers in watch list.
     *
     * @param event Event for notifying.
     */
    fun <T> notify(event: T) {
        for (observer in observers) {
            (observer as? GelmObserver<T>)?.sendEvent(event)
        }
    }
}

/**
 * Represent actions to Subject.
 */
interface GelmSubjectActions {
    fun subscribe(observer: GelmObserver<*>)
    fun unsubscribe(observer: GelmObserver<*>)
}