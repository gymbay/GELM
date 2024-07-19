package ru.gymbay.gelm.app.example.compose.store.models

sealed interface ComposeCommand {
    data class StartLoading(val text: String) : ComposeCommand
}