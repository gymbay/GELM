package ru.gymbay.gelm.app.example.compose.store.models

sealed interface ComposeEvent {
    data class TypeText(val text: String) : ComposeEvent
    data object Reload : ComposeEvent
    data object Next : ComposeEvent
}