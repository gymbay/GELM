package ru.gymbay.gelm.app.example.compose.store.models

sealed interface ComposeEffect {
    data object NavigateToScreen : ComposeEffect
}