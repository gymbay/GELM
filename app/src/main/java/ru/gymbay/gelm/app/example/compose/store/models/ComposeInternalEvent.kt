package ru.gymbay.gelm.app.example.compose.store.models

sealed interface ComposeInternalEvent {
    data class LoadedData(val list: List<String>) : ComposeInternalEvent
}