package io.github.gymbay.gelm.utils

fun interface GelmLogger {
    fun log(eventType: EventType, message: String)
}

enum class EventType {
    InitialInvoked,
    SendEventInvoked,
    HandleResultInvoked,
    StateEmitted,
    EffectEmitted,
    CommandCancelled,
    CommandSkipped,
    CommandCompleted,
    CommandStarted,
    ObserverEventSent,
}