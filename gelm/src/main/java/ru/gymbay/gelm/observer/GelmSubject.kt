package ru.gymbay.gelm.observer

/**
 * For objects that sending events to [GelmObserver]s.
 */
abstract class GelmSubject : GelmSubjectActions {
    private val observers: MutableList<GelmObserver<*>> = mutableListOf()

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
    protected fun <T> notify(event: T) {
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