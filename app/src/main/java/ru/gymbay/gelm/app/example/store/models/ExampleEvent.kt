package ru.gymbay.gelm.app.example.store.models

sealed interface ExampleEvent {
    data class TypeText(val text: String) : ExampleEvent
    data object Reload : ExampleEvent
    data object Next : ExampleEvent
}